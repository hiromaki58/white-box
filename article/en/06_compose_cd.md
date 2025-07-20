# Introduction
In the [previous article](("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-2-set-up-ci-cd-pipeline-feb646d2198b" 2, Set up CI)), we built the CI portion of the CI/CD pipeline.
In this article, we will focus on creating the CD (Continuous Deployment) part of the pipeline.
# What This Article Covers
Here’s the overall flow of the CI/CD pipeline:
1, Push to GitHub
↓
2, CircleCI runs tests (CI)
↓
3, Docker image is built
↓
4, Image is pushed to ECR (ECR login + docker push)
↓
5, SSH into EC2, pull the latest image, and restart (via docker-compose down/up or restart)
This article will cover steps 3 to 5.
#### Overall Workflow
1, Set up AWS CLI on the local environment for convenience
2, Modify the .circleci/config.yml file to automate Docker image builds with CircleCI
3, Install Docker
4, Install the ECS agent on the EC2 instance where the image will be run
5, Create an ECR repository using the local CLI
6, Create an ECS cluster and service
7, Deploy from CircleCI
#### Environment
We’ll be using only CircleCI and AWS in this setup.
# 1, Set up AWS CLI on the local environment for convenience
To get straight to the point — explaining the local environment setup would make this article too long, so I plan to cover that in a separate post.
(This includes IAM user configuration as well.)
# 2, Modify the .circleci/config.yml file to automate Docker image builds with CircleCI
The config file contains sensitive information such as passwords.
These values are specified using CircleCI environment variables.
To keep this article concise, I’ll also cover that setup in a separate post.

Below is the content of the config file.
Since we’re creating separate Docker images for the frontend and backend, the file is a bit lengthy.

In essence, the configuration says: "If the tests pass, build the image and push it to AWS."
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
# 3, Install Docker
You won’t be able to run anything if Docker isn’t installed on the EC2 instance.
```bash
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo usermod -aG docker ec2-user
```
# 4, Install the ECS agent on the EC2 instance where the image will be run
First, we need to create an IAM role for ECS and attach it to the EC2 instance.
However, since IAM-related topics can be quite extensive, I plan to cover them in a separate article.

Next, access the EC2 instance via CLI and install the ECS agent.
If you’re using Amazon Linux, you can use the following command:
```bash
sudo amazon-linux-extras install ecs -y
sudo systemctl enable --now ecs
```
Then, specify the ECS cluster you plan to create, or an existing one if you already have it.
```bash
echo "ECS_CLUSTER=web_game" | sudo tee -a /etc/ecs/ecs.config
sudo systemctl restart ecs
```
Sometimes, you might encounter the following error when running the command.
```bash
tee: /etc/ecs/ecs.config: No such file or directory
```
In that case, it’s likely because the directory to store the cluster name in the config file doesn’t exist — so just create it manually.
# 5, Create an ECR repository using the local CLI
Next, create an ECR repository to store your Docker images.
```bash
aws ecr create-repository \
  --repository-name web_game \
  --image-scanning-configuration scanOnPush=true \
  --region us-east-1
```
If successful, you should receive a JSON response like this.
```json
{
  "repository": {
      "repositoryArn": "arn:aws:ecr:ap-northeast-1:123456789012:repository/web-game",
      "registryId": "123456789012",
      "repositoryName": "web-game",
      "repositoryUri": "123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/web-game"
  }
}
# 6, Create an ECS cluster and service
6-1
Let’s start by creating a cluster.
In reality, you can configure details such as how many instances to launch, but we’ll skip those for now.
```bash
aws ecs create-cluster --cluster-name web_game
```
If successful, you should receive a JSON response like this.
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
Next, we’ll define a task definition.
A task definition is essentially a blueprint for the containers that ECS will run.
It includes information such as which Docker images to use, environment variables, ports, and log settings.
Since we’re defining 2 containers — one for the frontend and one for the backend — the configuration ended up being quite long.
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
Next, define a service.
A service is responsible for continuously running, maintaining, and monitoring tasks.
```bash
aws ecs create-service \
  --cluster web_game \
  --service-name web-game-service \
  --task-definition web-game-task \
  --desired-count 1 \
  --launch-type EC2 \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-abc123,subnet-def456],securityGroups=[sg-xxxxxx],assignPublicIp=ENABLED}"
```
# 7, Deploy from CircleCI
In my case, I’ve configured CircleCI to trigger a deployment whenever there’s a push to the main branch,
and I’ve set --force-new-deployment in the .circleci/config.yml file.
So I believe this setup effectively enables CI/CD.
