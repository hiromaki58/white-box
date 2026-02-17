# はじめに
この記事では、これまで5回に渡って記事にしてきた spot instance でも途切れずにサービスを続ける方法論の最終回として、 Elastic Kubernetes Service を使い方を説明します。
# この記事で実施する内容
1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
2, EKS クラスター作成
3, RDS 接続用の Security Group 設計
4, Spring Boot 用 Secret / ConfigMap
5, k8s マニフェストを EKS 用に調整
6, kubectl apply
7, Service / Ingress で外部公開
# 1, .circleci/config.yml ファイルを変更してイメージを ECR に push する。
前回までの記事では minikube や ECS を使っていましたが、今回は EKS を使うため ECR にイメージをアップロードできるように .circleci/config.yaml ファイルを変更します。
```yaml
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

ローカルから AWS にアクセスします。
brew install awscli
aws configure
AWS Access Key ID [None]: xxxxxxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: yyyyyyyyyyyyyyyyyyyyyyyyyy
Default region name [None]: us-east-1
Default output format [None]: json
aws sts get-caller-identity
ローカルから AWS にアクセスする方法はこちらの記事で紹介しています。(以前の記事で紹介した内容)

このコマンドによって対象のAWSアカウントにアクセスできることが分かれば「eksctl create cluster -f cluster.yaml」を実行してクラスター作成します。

コマンドを実行してから、クラスターが作成されるまで20分から30分かかります。
"kubectl get nodes" コマンドを実行して以下の結果が表示されれば、kubectl から EKS API への接続に成功して、Node が Control Plane に join しています。
% kubectl get nodes
NAME                             STATUS   ROLES    AGE     VERSION
ip-172-31-107-149.ec2.internal   Ready    <none>   4m43s   v1.29.15-eks-ecaa3a6

つまりはこういうことです。
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
- IP アドレスに依存しない（Pod / Node が変わっても壊れない）
-「0.0.0.0/0」は使わない
