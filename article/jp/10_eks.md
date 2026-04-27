# はじめに
この記事では、これまで5回に渡って記事にしてきた spot instance でも途切れずにサービスを続ける方法の最終回として、 Elastic Kubernetes Service を使い方を説明します。
# この記事で実施する内容
1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
2, EKS クラスター作成
3, RDS 接続用の Security Group 設計
4, Spring Boot 用 Secret / ConfigMap
5, deployment ファイルの作成と適用
6, k8s マニフェストを EKS 用に調整
7, kubectl apply
8, Service / Ingress で外部公開
# 1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
前回までの記事では minikube や ECS を使っていましたが、今回は EKS を使うため ECR にイメージをアップロードできるように .circleci/config.yaml ファイルを変更します。
```config.yaml
      - run:
          name: Push to Amazon ECR
          command: |
            aws --version
            aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
            docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:$IMAGE_TAG
            docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:$IMAGE_TAG
```
プラス、バージョンを latest からSHA1:0:7を使う方法に変更します。
これによりよってイメージのタグをユニークにして、ロールバックを可能にします。
# 2, EKS クラスター作成
## 環境
次にAWSでの環境の構築を行います。最終的な目標の構成は以下になります。
このステップではクラスターの作成ですので、あくまで「アプリ」を投入する「箱」を作るだけです。

また今回は試験環境を構築するだけなので、必要なのはこれだけです。
- VPC
- Public subnet x1
- Private subnet x1
- Internet gateway x1
- Route table

Route table の構成は
public subnet の場合、
Destination: VPCのCIDR → local
Destination: 0.0.0.0/0     → Internet Gateway

private subnet の場合、
Destination: 172.31.0.0/16 → local

## クラスター作成
クラスターの作成は CLI からコマンドを使って行います。
eksctl のインストール（ローカル）
brew install eksctl
brew install kubectl

eksctl version
kubectl version --client

cluster.yaml はローカルで作成し、ローカルで eksctl create cluster を実行します。

なぜローカルというと eksctl は 管理者権限が必要で、クラスター作成は一度きりの作業であるため CI から実行する必要がありません。

ローカルから AWS にアクセスするための設定を行います。
brew install awscli
aws configure
AWS Access Key ID [None]: xxxxxxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: yyyyyyyyyyyyyyyyyyyyyyyyyy
Default region name [None]: us-east-1
Default output format [None]: json
aws sts get-caller-identity
ローカルから AWS にアクセスする方法はこちらの記事で紹介しています。(以前の記事で紹介した内容)

このコマンドによって対象のAWSアカウントにアクセスできることが分かれば、以下のコマンドを実行してクラスター作成します。
```bash
eksctl create cluster -f cluster.yaml
```

コマンドを実行してから、クラスターが作成されるまで20分から30分かかります。
"kubectl get nodes" コマンドを実行して以下の結果が表示されれば、kubectl から EKS API への接続に成功して、Node が Control Plane に join しています。
```bash
% kubectl get nodes
NAME                             STATUS   ROLES    AGE     VERSION
ip-172-31-107-149.ec2.internal   Ready    <none>   4m43s   v1.30.14-eks-f69f56f
```

このコマンドは以下の内容を実施しています。
1, EC2 インスタンスが起動する。
2, EC2 内で kubelet が起動する。
3, kubelet が EKS Control Plane（API Server）に接続する。
4, 認証（IAM Role）を行う。
5, 自分を Node として登録（join）する。
6, 状態が Ready になる。

# 3, RDS 接続用の Security Group 設計
今回のデモでは、private subnet に RDS を配置し、public subnet に配置した
キーとなるアイデアは
- EKS 上の Pod だけが RDS に接続できる
- IP アドレスに依存しない（Pod / Node が変わっても接続できる）
-「0.0.0.0/0」は使わない

具体的には RDS に設定されている Security Group のインバウンドルールに、 EKS のクラスターに紐づいている EC2 インスタンスに紐づいている Security Group を追加します。

以下の方法で RDS へインスタンスから接続ができるかを確認できます。
1, テスト用Podを作る
kubectl run test \
--image=public.ecr.aws/amazonlinux/amazonlinux:2 \
--restart=Never \
--command -- sleep 3600
pod/test created
2, Pod の動作確認
% kubectl get pod test -o wide
NAME   READY   STATUS    RESTARTS   AGE   IP              NODE                           NOMINATED NODE   READINESS GATES
test   1/1     Running   0          20s   172.31.10.245   ip-172-31-5-126.ec2.internal   <none>           <none>
3, Pod にアクセス
kubectl exec -it test -- bash
(この test というのが Pod の名前になります。)
4, Podの中で MySQL クライアントを入れて接続
yum install -y mariadb
(Amazon linux は MySQL パッケージを持たないため、同じプロトコルを持つ mariadb で代用)
mysql -h <RDS-ENDPOINT> -u <user> -p

bash-4.2# mysql -h <RDS-ENDPOINT> -u <user> -p
Enter password:
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MySQL connection id is 250
Server version: 8.0.42 Source distribution

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MySQL [(none)]>
MySQL [(none)]>
MySQL [(none)]>
5, 最後にテスト用の Pod を削除
exit
kubectl delete pod test

# 4, Spring Boot 用 Secret
これまでの作業で使っていた CircleCI ではテスト用DBを CircleCI 内部で使っていました。
しかし今回はプライベートサブネットに設定した RDS に接続します。
そのため configmap と secret を作成して、Spring Boot に RDS の接続情報を渡します。
```secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: spring-db-secret
type: Opaque
stringData:
  SPRING_DATASOURCE_URL: jdbc:mysql://aaa.bbb.region.rds.amazonaws.com:3306/webgame?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  DB_USER: use_name
  DB_PASS: password
```
CircleCI では今まで通り DB を使ったテストを行うために、EKS では k8s から設定を注入します。
つまり Spring Boot では環境変数だけを見るようにし、その環境変数を CircleCI では CircleCI の environment variable から
EKS では Secret / ConfigMap から注入するように変更します。
```application.properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```
これによってKubernetesから環境変数を受け取ることができます。
（Kubernetesは「環境変数」で値を渡します。）
（CircleCI には「CircleCI で実行するテストに必要な DB 情報」だけを置く、RDS 本番用の ID / PASSWORD は CircleCI に置かなくて大丈夫です。）

以下のコマンドを実行して、接続に必要な機密情報（secret）をKubernetesに保存します。
（パスはコマンドを実行する場所によって変更してください。）
```bash
kubectl apply -f ./secrets/secret.yaml
```

# 5, deployment ファイルの作成と適用
次にこの secret を Deployment が参照できるようにします。
こうすることで最終的に Pod の中にあるアプリが secret を利用できるようになります。
そのために以下に記述した一連のファイルを作成します。
- backend-deployment.yaml
- backend-service.yaml
- frontend-deployment.yaml
- frontend-service.yaml
この時、deployment が secret を使用するように、以下の形で指定します。
```yaml
envFrom:
  - secretRef:
      name: spring-db-secret
```

これを以下の順番で適用・反映します。
```bash
kubectl apply -f ./backend/backend-deployment.yaml
kubectl apply -f ./backend/backend-service.yaml
kubectl apply -f ./frontend/frontend-deployment.yaml
kubectl apply -f ./frontend/frontend-service.yaml
```
kubectl get pods コマンドで反映されていることを確認します。
Status が Running であればOK。
```bash
% kubectl get pods                                    
NAME                        READY   STATUS    RESTARTS      AGE
backend-8db6d49fd-9skv5     1/1     Running   3 (38s ago)   81s
frontend-65d6f8487f-b7qrp   1/1     Running   0             70s
```

次にそれぞれの Pod のログを確認します。
```bash
kubectl logs <backend-pod>
```


費用が高いため、不要になった場合 EKS クラスターを削除します。
```bash
eksctl delete cluster -f cluster.yaml
aws eks list-clusters
```
このコマンドによって以下が実行されます。
① CloudFormation stack削除
② Node削除
③ EKS削除
