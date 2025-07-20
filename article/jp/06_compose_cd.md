# はじめに
[前回]("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-2-set-up-ci-cd-pipeline-feb646d2198b" 2, Set up CI)はCI/CDパイプラインのCI側を構築しました。
今回はCI/CDパイプラインのCD側を作成したいと思います。
# この記事で実施する内容
まずはCI/CD全体の具体的な流れは以下になります。
①GitHubにpush
   ↓
②CircleCIがテストを実行（CI）
   ↓
③ Dockerイメージをビルド
   ↓
④ ECRにpush（ECR login + docker push）
   ↓
⑤ EC2にSSH接続し、最新イメージpull + 再起動（docker-compose down/up or restart）
この記事ではそのうちの③から⑤を説明していきます。
#### 作業全体の流れ
1, AWS CLIが便利なのでローカル環境でも使えるようにします
2, CircleCI で Docker イメージを自動ビルドするために.circleci/config.ymlファイルを変更
3, Docker をインストール
4, イメージをビルドしたい EC2 に ECS エージェントをインストール
5, ローカルCLIから ECR レポジトリを作成
6, ECS クラスターとサービスの作成
7, CircleCI からデプロイ
#### 動作環境
今回はCircle CIとAWSのみです。
# 1, AWS CLIが便利なのでローカル環境でも使えるようにします
いきなりですが、ローカル環境での設定を説明すると記事が長くなってしまうので、別記事を作成したいとおもいます。
(IAMユーザーの設定もありますしね。)
# 2, CircleCI で Docker イメージを自動ビルドするために.circleci/config.ymlファイルを変更
config ファイルにはパスワードなど機微な情報が含まれています。
それらは Circle Ci の環境変数で指定しています。
こちらも記事が冗長になるのを防ぐため、別の記事で紹介したいとおもいます。

設定ファイルの中身は以下になります。
フロントとバックエンドを分けてイメージを作成するので、長くなってしまっています。

内容としてはテストに成功したらイメージを作成して、AWSに送ってね、になっています。
```.circleci/config.yml
version: 2.1

executors:
  java-executor:
    docker:
      - image: gradle:8.13-jdk23
        environment:
          SPRING_PROFILES_ACTIVE: test
          DB_NAME: webgame
          DB_USER: $LOCAL_DB_USER
          DB_PASS: $LOCAL_DB_PASSWORD
      - image: mysql:8.0
        environment:
          MYSQL_DATABASE: webgame
          MYSQL_ROOT_PASSWORD: $LOCAL_DB_PASSWORD
        command: >-
          mysqld --character-set-server=utf8mb4
                 --collation-server=utf8mb4_unicode_ci
    working_directory: ~/project

jobs:
  test-java:
    executor: java-executor
    steps:
      - checkout:
          path: ~/project

      - run:
          name: Wait for MySQL
          command: |
            for i in `seq 1 20`; do
              mysql -h 127.0.0.1 -P 3306 -u root -e "SELECT 1" && break
              echo "Waiting for MySQL..."
              sleep 3
            done

      - run:
          name: Make gradlew executable
          command: chmod +x ./web_game/gradlew

      - run:
          name: Run tests
          command: |
            ./web_game/gradlew test \
              --project-dir ./web_game \
              -i \

      - store_test_results:
          path: web_game/build/test-results

      - store_artifacts:
          path: web_game/build/reports

  build-and-deploy:
    docker:
      - image: cimg/base:stable
    environment:
      AWS_REGION: us-east-1
    steps:
      - checkout
      - setup_remote_docker
      - run:
          name: Install AWS CLI
          command: |
            apt-get update && apt-get install -y unzip curl
            curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
            unzip awscliv2.zip
            ./aws/install --install-dir $HOME/aws-cli --bin-dir $HOME/bin
            echo 'export PATH=$HOME/bin:$PATH' >> $BASH_ENV
            source $BASH_ENV
            aws --version
      - run:
          name: Build Docker Images
          command: |
            docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest ./web_game
            docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest ./web_game_interface/react-typescript
      - run:
          name: Push to Amazon ECR
          command: |
            aws --version
            aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
            docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest
            docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest
      - run:
          name: Deploy to ECS
          command: |
            aws ecs update-service \
              --cluster web_game \
              --service web-game-service \
              --force-new-deployment \
              --region $AWS_REGION

workflows:
  ci-cd-workflow:
    jobs:
      - test-java
      - build-and-deploy:
          requires:
            - test-java
```
# 3, Docker をインストール
EC2に Docker がないと動かないですからね。
```bash
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo usermod -aG docker ec2-user
```
# 4, イメージをビルドしたい EC2 に ECS エージェントをインストール
まず最初に ECS用 IAM ロールを作成し EC2 にアタッチします。
しかしこれも別の記事でIAM関連はまとめて紹介したいと思います。

次にEC2にCLIでアクセスしてエージェントをインストールします。
Amazon Linuxを使っている場合は以下のコマンドを使用してください
```bash
sudo amazon-linux-extras install ecs -y
sudo systemctl enable --now ecs
```
そして、これから作成することを想定している、あるいは既存のECSのクラスターを指定します。
```bash
echo "ECS_CLUSTER=web_game" | sudo tee -a /etc/ecs/ecs.config
sudo systemctl restart ecs
```
実行した時に以下のエラーが発生したことがあります。
```bash
tee: /etc/ecs/ecs.config: No such file or directory
```
この場合はクラスター名を設定ファイルに保存するために、ディレクトリを作ってあげましょう。
# 5, ローカルCLIから ECR レポジトリを作成
次に Docker イメージを格納する ECR レポジトリを作成します。
```bash
aws ecr create-repository \
  --repository-name web_game \
  --image-scanning-configuration scanOnPush=true \
  --region us-east-1
```
成功するとこんな json が返ってくるはずです。
```json
{
  "repository": {
      "repositoryArn": "arn:aws:ecr:ap-northeast-1:123456789012:repository/web-game",
      "registryId": "123456789012",
      "repositoryName": "web-game",
      "repositoryUri": "123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/web-game"
  }
}
```
# 6, ECS クラスターとサービスの作成
6-1
まずはクラスターを作りましょう。
本当はインスタンスをいくつ立ち上げるかなど、細かく設定できますが、一旦省略します。
```bash
aws ecs create-cluster --cluster-name web_game
```
成功するとこんな json が返ってくるはずです。
```json
{
    "cluster": {
        "clusterArn": "arn:aws:ecs:us-east-1:123456789012:cluster/web_game",
        "clusterName": "web_game",
        "status": "ACTIVE",
        "registeredContainerInstancesCount": 0,
        "runningTasksCount": 0,
        "pendingTasksCount": 0,
        "activeServicesCount": 0,
        "statistics": [],
        "tags": [],
        "settings": [
            {
                "name": "containerInsights",
                "value": "disabled"
            }
        ],
        "capacityProviders": [],
        "defaultCapacityProviderStrategy": []
    }
}
```
6-2
次にタスク定義をします。
タスク定義は ECS で起動するコンテナの設計図です。
どの Docker イメージを使うか・環境変数・ポート・ログ設定などをまとめます。
フロントとバックエンドのコンテナを2つ定義しているので長くなりました。
```json:task-definition.json
{
    "taskDefinition": {
        "taskDefinitionArn": "arn:aws:ecs:us-east-1:123456789012:task-definition/web-game-task:1",
        "containerDefinitions": [
            {
                "name": "frontend",
                "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/web_game_front:latest",
                "cpu": 0,
                "portMappings": [
                    {
                        "containerPort": 3000,
                        "hostPort": 3000,
                        "protocol": "tcp"
                    }
                ],
                "essential": true,
                "environment": [],
                "mountPoints": [],
                "volumesFrom": [],
                "systemControls": []
            },
            {
                "name": "backend",
                "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/web_game:latest",
                "cpu": 0,
                "portMappings": [
                    {
                        "containerPort": 8080,
                        "hostPort": 8080,
                        "protocol": "tcp"
                    }
                ],
                "essential": true,
                "environment": [],
                "mountPoints": [],
                "volumesFrom": [],
                "systemControls": []
            }
        ],
        "family": "web-game-task",
        "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
        "networkMode": "awsvpc",
        "revision": 1,
        "volumes": [],
        "status": "ACTIVE",
        "requiresAttributes": [
            {
                "name": "com.amazonaws.ecs.capability.ecr-auth"
            },
            {
                "name": "ecs.capability.execution-role-ecr-pull"
            },
            {
                "name": "com.amazonaws.ecs.capability.docker-remote-api.1.18"
            },
            {
                "name": "ecs.capability.task-eni"
            }
        ],
        "placementConstraints": [],
        "compatibilities": [
            "EC2",
            "FARGATE"
        ],
        "requiresCompatibilities": [
            "EC2"
        ],
        "cpu": "512",
        "memory": "1024",
        "registeredAt": "2025-00-0013T13:00:00.00-00:00",
        "registeredBy": "arn:aws:iam::123456789012:user/WebGame"
    }
}

```
6-3
次にサービスを定義します。
このサービスはタスクを継続的に起動・維持・監視 する仕組みです。
```bash
aws ecs create-service \
  --cluster web_game \
  --service-name web-game-service \
  --task-definition web-game-task \
  --desired-count 1 \
  --launch-type EC2 \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-abc123,subnet-def456],securityGroups=[sg-xxxxxx],assignPublicIp=ENABLED}"
```
# 7, CircleCI からデプロイ
私の場合は、Circle CIの設定でmainブランチがプッシュされた時をトリガーにして、
.circlci/config.ymlに"-force-new-deployment"を設定しているので、CI/CDしてるんじゃないかな。
