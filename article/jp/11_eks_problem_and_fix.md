# はじめに
この記事では、AWS EKS で Web application が動作する環境を構築するときに発生した問題と、解決した方法を紹介します。
# この記事で実施する内容
1, vpc-cni でエラーメッセージ
2, NAT gateway の設定漏れ
3, Instances failed to join the kubernetes cluster
4, kubectl describe コマンド実行時に Status: Pending が表示される
5, eksctl create cluster -f cluster.yamlコマンド実行時にエラー
6, secret が反映されない
7, パスワードがPodに反映されずDBに接続できない
8, アーキテクチャーの不一致
9, AWS RDS にあるデータベースが認識されず接続できない
10, curl http://localhost:8080 でアクセスできない
# 1, vpc-cni でエラーメッセージ
問題
```shell
recommended policies for vpc-cni addon but OIDC disabled
```
原因
Kubernetes では本来、 Pod は「仮想ネットワーク」の IP を持つクラウドの VPC とは切り離されているのですが、EKS では Pod を VPC に直接つなぎたいという要望がありました。
そのため Amazon では VPC CNI plugin を使っています。
vpc-cni は EC2 の ENI（ネットワークカード）を使い、 Pod に VPC CIDR の IP を直接割り当てます。
解決策
cluster.yaml ファイルに以下の設定を追加します。
```yaml
iam:
withOIDC: true
```

# 2, NAT gateway の設定漏れ
問題
```shell
Resource handler returned message: "[Issue(Code=Ec2SubnetInvalidConfiguration, Message=One or more Amazon EC2 Subnets of [subnet-xxx] for node group spot-ng does not automatically assign public IP addresses to instances launched into it.
If you want your instances to be assigned a public IP address, then you need to enable auto-assign public IP address for the subnet."
```
原因
Private Subnet の Node / Pod は自分では Public IP を持たないが、
ECR から image を pull し、AWS API にアクセスし、OS パッケージ取得するためにインターネットへの出口が必要で、そのためには NAT Gateway が必要になります。
解決策
NAT gateway を public subnet に配置、type は public で elastic IP アドレスの設定を行います。

# 3, Instances failed to join the kubernetes cluster
問題
```shell
Instances failed to join the kubernetes cluster
```
調査
「kubectl get nodes」コマンドを実行しても「Instances failed to join the kubernetes cluster」が表示されました。
解決策
Node instance role を明示的に作り、Node Group に使わせるように設定します。
１．Node 用 IAM ロールを手動で作成します。
AmazonEC2ContainerRegistryReadOnly
AmazonEKS_CNI_Policy
AmazonEKSWorkerNodePolicy
を設定します。
２，cluster.yaml でそのロールを明示指定
iam:instanceRoleARN: arn:aws:iam::000000000000:role/webgame-eks-node-instance-role
３，Network ACL をデフォルトに戻す
HTTPS で通信する必要があるがポート443を閉じていた＋戻り通信（非常に重要）Ephemeral ports（1024–65535）も閉じていた
４．一度「eksctl delete cluster --region=us-east-1 --name=クラスター名」コマンドを実行後に
再度以下のコマンドを実行します。
```shell
% source eks/cluster-values.yaml
% eksctl create cluster -f cluster.yaml

% kubectl get svc
NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.100.0.1   <none>        443/TCP   11m
```
kubernetes サービスを返せば、接続はOKです。
```shell
% kubectl get nodes
NAME                             STATUS   ROLES    AGE     VERSION
ip-172-31-107-149.ec2.internal   Ready    <none>   4m43s   v1.29.15-eks-ecaa3a6
```
kubectl → EKS API の接続に成功して、Node が Control Plane に join していることが確認できました。

# 4, kubectl describe コマンド実行時に Status: Pending が表示される
問題
```shell
% kubectl get node
NAME                           STATUS   ROLES    AGE     VERSION
ip-172-31-29-74.ec2.internal   Pending    <none>   2m22s   v1.30.14-eks-3385e9b
```
原因
インスタンスサイズが EKS を扱うには小さすぎました。
解決策
インスタンスサイズをより大きなサイズに変更します。

# 5, eksctl create cluster -f cluster.yamlコマンド実行時にエラー
原因
cluster.yaml ファイルでは変数を指定して secret から値を取得することができません。
解決策
cluster.yaml ファイルに値を直書きする。

# 6, secret が反映されない
問題
```shell
using password: NO
```
原因
新しい Pod が作られた時にのみ環境変数が再注入されるため、作成後に secret を変更しても反映されない。
解決策
以下のコマンドを実行して再度 Pod を作成します。
```yaml
kubectl apply -f secret.yaml
kubectl rollout restart deployment backend
```

# 7, パスワードがPodに反映されずDBに接続できない
問題
Access denied for user 'root' ... (using password: NO)
調査
以下のコマンドを使ってテスト用 Pod から AWS RDS へは接続できました。
(Amazon linux は MySQL パッケージを持たないため、同じプロトコルを持つ mariadb で代用します。)
```shell
% kubectl run test --image=public.ecr.aws/amazonlinux/amazonlinux:2 --restart=Never --command -- sleep 3600 pod/test created
% kubectl get pod test -o wide
NAME   READY   STATUS    RESTARTS   AGE   IP              NODE                           NOMINATED NODE   READINESS GATES
test   1/1     Running   0          20s   172.31.10.245   ip-172-31-5-126.ec2.internal   <none>           <none>
% kubectl exec -it test -- bash
bash-4.2#  yum install -y mariadb
bash-4.2#  mysql -h <RDS-ENDPOINT> -u <user> -p
MySQL [(none)]> 
```
さらに以下のコマンドを使って表示されるパスワードと ID が echo <password> | base64 --decode コマンドの実行結果と合致するかを確認します。
```shell
kubectl get secret spring-db-secret -o yaml 
```
一致していない場合、最新のイメージが AWS CRS から取得できていない可能性があります。
原因
結局この問題は AWS CRS から最新のイメージが取得されていないためでした。
そして最新のイメージが取得できないのはバージョンの設定でした。
解決策
以下のような形で :latest を明示しなかったためローカル環境では正しいパスワードと ID が Docker の環境には更新されていませんでした。
Kubernetes Node は古いイメージのキャッシュを使いたがる傾向があるようですので、明示する必要があるようです。
```yaml : circle/config.yaml
- run:
    name: Push to Amazon ECR
    command: |
      aws --version
      aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
      docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest
      docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest
```

# 8, アーキテクチャーの不一致
問題
```shell
no match for platform in manifest
```
原因
単純に AWS EC2 のアーキテクチャーが amd64 (x86_64) であるのに対して、私のローカル環境が MAC で arm だったためです。
解決策
以下のように linux/amd64 でビルドするように明示します。
```yaml
- run:
  name: Build Docker Images
  command: |
    docker build --platform linux/amd64 -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:$IMAGE_TAG ./infra/minikube/mysql-minikube-backend
```

# 9, AWS RDS にあるデータベースが認識されず接続できない
問題
Unknown database 'webgame'
調査
テスト用のPodを作成しDB名を確認します。
```shell
% kubectl run test --image=public.ecr.aws/amazonlinux/amazonlinux:2 --restart=Never --command -- sleep 3600 pod/test created
% kubectl exec -it test -- bash
bash-4.2#  yum install -y mariadb
bash-4.2#  mysql -h <RDS-ENDPOINT> -u <user> -p
MySQL [(none)]> SHOW DATABASES;
+--------------------+
| Database           |
+--------------------+
| WebGame            |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
5 rows in set (0.03 sec)
```
原因
以下のコマンドを使って表示されるシークレットの DB の名称と大文字小文字が一致していませんでした。
```shell
kubectl get secret spring-db-secret -o yaml 
```
解決策
AWS RDS 側のDB名を変更すると弊害が多いためシークレットを変更します。
その後、以下のコマンドを実行して secret と AWS RDS にあるデータベースの名称を一致させます。
```shell
kubectl apply -f ./secrets/secret.yaml
kubectl rollout restart deployment backend
```

# 10, curl http://localhost:8080 でアクセスできない
調査
```shell
% curl -v http://localhost:8080
HTTP/1.1 403 
% kubectl get endpoints backend-service
NAME              ENDPOINTS          AGE
backend-service   172.31.24.6:8080   79m
% kubectl exec -it <frontend> -- bash
bash-4.2#  netstat -tulpn
0.0.0.0:80
```
AWSコンソールで以下の内容が表示されます。
```shell
Status
0 of 1 instance in service 
```
原因
enginx が port 80 で待機しているため
解決策
フロントエンドの deployment と service を以下の形に変更します。
```yaml : frontend-deployment.yaml
- containerPort: 80
```
```yaml : frontend-service.yaml
targetPort: 80
```
その後、以下のコマンドを実行して変更を反映します。
```shell
kubectl apply -f ./frontend/frontend-deployment.yaml
kubectl apply -f ./frontend/frontend-service.yaml
kubectl rollout restart deployment frontend
```
