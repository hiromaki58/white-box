# はじめに
この記事では、これまで5回に渡って記事にしてきた spot instance でも途切れずにサービスを続ける方法の最終回として、 Elastic Kubernetes Service を使い、Web Application をブラウザで表示する方法を説明します。
# この記事で実施する内容
1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
2, Spring Boot 用 Secret 作成
3, EKS クラスター作成
4, RDS 接続用の Security Group 作成
5, RDS 接続用の Security Group の動作確認
6, deployment ファイルの作成と適用
7, アプリケーションをブラウザで表示する

# 1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
前回までの記事では minikube や ECS を使っていましたが、今回は EKS を使うため ECR にイメージをアップロードできるように .circleci/config.yaml ファイルを変更します。
```yaml : config.yaml
- run:
    name: Push to Amazon ECR
    command: |
        aws --version
        aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest
        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest
```

# 2, Spring Boot 用 Secret 作成
これまでの作業で使っていた CircleCI ではテスト用DBを CircleCI 内部で使っていました。
しかし今回はプライベートサブネットに設定した RDS に接続します。
そのため secret を作成して、Spring Boot に RDS の接続情報を渡します。
```yaml : secret.yaml
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
```properties : application.properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```
これによってKubernetesから環境変数を受け取ることができます。
（Kubernetesは「環境変数」で値を渡します。）
（CircleCI には「CircleCI で実行するテストに必要な DB 情報」だけを置く、RDS 本番用の ID / PASSWORD は CircleCI に置かなくて大丈夫です。）

もしもすでにクラスターが動作している場合、以下のコマンドを実行して、接続に必要な機密情報（secret）をKubernetesに保存します。
（パスはコマンドを実行する場所によって変更してください。）
```bash
kubectl apply -f ./secrets/secret.yaml
```

# 3, EKS クラスター作成
### 環境
次にAWSでの環境の構築を行います。
このステップではクラスターの作成ですので、あくまで「アプリ」を投入する「箱」を作るだけです。

また今回は試験環境を構築するだけなので、必要なのはこれだけです。
- VPC
- Public subnet x1
- Private subnet x1
- Internet gateway x1
- Route table

Route table の構成は
public subnet
Destination: VPCのCIDR → local
Destination: 0.0.0.0/0 → Internet Gateway
private subnet
Destination: 172.31.0.0/16 → local

## クラスター作成
クラスターの作成は CLI からコマンドを使って行います。
eksctl のローカルへのインストールと確認
```shell
brew install eksctl
brew install kubectl
eksctl version
kubectl version --client
```
cluster.yaml はローカルで作成し、ローカルで eksctl create cluster を実行します。
なぜローカルというと eksctl は 管理者権限が必要で、クラスター作成は一度きりの作業であるため CI から実行する必要がありません。

ローカルから AWS にアクセスするための設定を行います。
```shell
brew install awscli
aws configure
AWS Access Key ID [None]: xxxxxxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: yyyyyyyyyyyyyyyyyyyyyyyyyy
Default region name [None]: us-east-1
Default output format [None]: json
aws sts get-caller-identity
```
ローカルから AWS にアクセスする方法はこちらの記事で紹介しています。(以前の記事で紹介した内容)

対象のAWSアカウントにアクセスできることが分かれば、以下のコマンドから yaml ファイルを実行してクラスター作成します。
```bash
aws sts get-caller-identity
{
    "UserId": "AIDAxxxxxxxxxxxx",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/your-user"
}
eksctl create cluster -f cluster.yaml
```
今回作成した yaml ファイルは以下の内容です。
```yaml : cluster.yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: webgame-cluster
  region: region
  version: "1.30"

# Activate OIDC
iam:
  withOIDC: true

# use existing VPC / Subnet
vpc:
  id: ${VPC_ID}
  subnets:
    public:
      us-east-1b:
        id: ${PUBLIC_SUBNET_1b}
      us-east-1a:
        id: ${PUBLIC_SUBNET_1a}

# EKS node（Spot Instance）
managedNodeGroups:
  - name: node-group
    instanceTypes:
      - t3.medium
    desiredCapacity: 1
    minSize: 1
    maxSize: 2

    # Not to create the EKS nodes in the private subnet
    privateNetworking: false

    iam:
      instanceRoleARN: ${INSTANCE_ROLE_ARN}

    labels:
      role: worker
      lifecycle: spot
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

# 4, RDS 接続用の Security Group 作成
今回のデモでは、private subnet に RDS を配置し、public subnet に配置しています。
キーとなるアイデアは
- EKS 上の Pod だけが RDS に接続できる
- IP アドレスに依存しない（Pod / Node が変わっても接続できる）
  -「0.0.0.0/0」は使わない

具体的にはEKS クラスターにある Pod (EC2 インスタンス)に紐づいている Security Group を RDS インバウンドルールに追加します。

# 5, RDS 接続用の Security Group の動作確認
次に設定した Security Group が正しく動作するかを確認します。
### 1, テスト用 Pod を作る
```shell
% kubectl run test --image=public.ecr.aws/amazonlinux/amazonlinux:2 --restart=Never --command -- sleep 3600 pod/test created
```
### 2, Pod の動作確認
```shell
% kubectl get pod test -o wide
NAME   READY   STATUS    RESTARTS   AGE   IP              NODE                           NOMINATED NODE   READINESS GATES
test   1/1     Running   0          20s   172.31.10.245   ip-172-31-5-126.ec2.internal   <none>           <none>
```
### 3, Pod にアクセス
```shell
kubectl exec -it test -- bash
```
(この test がテスト用 Pod の名前になります。)
### 4, Podの中で MySQL クライアントを入れて接続
Amazon linux は MySQL パッケージを持たないため、同じプロトコルを持つ mariadb で代用します。
```shell
yum install -y mariadb
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
```
### 5, 最後にテスト用の Pod を削除します。
```shell
exit
kubectl delete pod test
```

# 6, deployment ファイルの作成と適用
次に spring boot の application.properties ファイルにある secret を Deployment が参照できるようにします。
そのために以下に記述した一連のファイルを作成します。
- backend-deployment.yaml
- backend-service.yaml
- frontend-deployment.yaml
- frontend-service.yaml

```yaml : backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: ${IMAGE_ID}
          ports:
            - containerPort: 8080
          envFrom:
            - secretRef:
                name: spring-db-secret
```
```yaml : backend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
spec:
  type: ClusterIP
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
```
```yaml : frontend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: frontend
          image: ${FRONT_IMAGE_ID}
          ports:
            - containerPort: 80
```
```yaml : frontend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
spec:
  type: LoadBalancer
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 80
```
この時、deployment が secret を使用するように、以下の形で指定します。
```yaml : backend-development.yaml
envFrom:
  - secretRef:
      name: spring-db-secret
```
これを以下の順番で適用・反映します。
```bash
kubectl apply -f ./secrets/secret.yaml
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

# 7,アプリケーションをブラウザで表示する 
### 1, まずはサービスが動いていることを確認しましょう。
```shell
% kubectl get svc
NAME               TYPE           CLUSTER-IP       EXTERNAL-IP                                                               PORT(S)        AGE
backend-service    ClusterIP      10.100.219.184   <none>                                                                    8080/TCP       6h48m
frontend-service   LoadBalancer   10.100.39.47     123456789-123456789.us-east-1.elb.amazonaws.com   80:31984/TCP   6h48m
kubernetes         ClusterIP      10.100.0.1       <none>                                                                    443/TCP        7h22m
```
### 2, 次に Pod と Pod で疎通ができるか確認しましょう。
フロントエンド側の pod からバックエンド側の pod に入れるかを確認します。
```shell
kubectl exec -it <frontend-pod> -- curl http://backend-service:8080
```
### 3, フロントエンドへのアクセス方法を確認しましょう。
```shell
% kubectl get svc frontend-service
NAME               TYPE           CLUSTER-IP     EXTERNAL-IP                                                               PORT(S)        AGE
frontend-service   LoadBalancer   10.100.39.47   123456789-123456789.us-east-1.elb.amazonaws.com   80:31984/TCP   6h52m
```
これにより Kubernetes に frontend-service という Service オブジェクトが作成されていること、
ロードバランサーが作成され、DNSによる名前の割り当てが行われていることが分かりました。
### 4, 5, 取得できた EXTERNAL-IP を使ってブラウザでアクセスします。
```shell
http://<EXTERNAL-IP>
```
最後に費用が高いため、不要になった場合 EKS クラスターを削除します。
```bash
eksctl delete cluster -f cluster.yaml
aws eks list-clusters
```
