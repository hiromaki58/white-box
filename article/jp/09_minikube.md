# はじめに
今回は minikube を使ってこれまでに作成した Docker イメージが kubernetes でも動作するかを確認したいと思います。
# この記事で実施する内容
1, 最初に
2, イメージをビルドして docker hub にアップロード
3, minikube を起動する
4, minikube の中で MySQL を起動する
5, minikube の中で backend を起動する
6, minikube の中で frontend を起動する
7, NodePort を使ってアプリをブラウザに表示する
# 1, 最初に
minikube は初期設定が複雑な kubernetes を使う際に、投入する設定を確認できるローカルで動作する試験環境です。
この youtube の動画をなぞって minikube を遊んで見ようと思ったのですが、イメージを自分で作成することにしました。
# 2, イメージをビルドして docker hub にアップロード
まずは前回の記事 (3, Set up CD portion of the CI/CD Pipeline) で作成したイメージを docker hub にアップロードします。
すでに docker へログインできるアカウントを持っていれば、ローカル環境から以下のコマンドでアップロードできます。
```bash
docker login

docker build -t thatisg/mysql-minikube-backend:1.0 ./mysql-minikube-backend
docker build -t thatisg/mysql-minikube-frontend:1.0 ./mysql-minikube-frontend/react-typescript

docker push thatisg/mysql-minikube-backend:1.0
docker push thatisg/mysql-minikube-frontend:1.0
```
# 3, minikube を起動する
こちらもローカルにインストールされていなければインストールを行ってください。(私は mac ユーザーなので Homebrew を使っています)
この時ローカルでdockerが起動している必要があります。
```bash
brew install minikube
minikube version
```
バージョン情報が示されればインストール完了です。

もしすでに使用したことがある場合、一度リセットするといいかもしれません。
```bash
minikube delete
minikube start --driver=docker --container-runtime=containerd
```

最後に以下のメッセージが表示されていれば、 minikube は問題なく動作しています。
```bash
Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
```
動作確認のコマンド
```bash
% minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```
# 4, minikube の中で MySQL を起動する
minikube が動くようになったら、docker hub にアップロードされているイメージ（アプリ）をどのように使用するか、個々のローカル環境で設定しなければなりません。
具体的には、どのイメージを使用するのか、DBへアクセスするためのIDとパスワード、バージョンやポート番号などになります。
今回 docker hub にアップロードしたイメージでは DB / Backend / Frontend の構成であるため、それぞれが必要な設定を行っていきます。

PersistentVolumeClaim (PVC) を使って、 イメージが使用するデータを保持したいのですが、 StorageClass が存在していないと自動で作れないため kubectl get storageclass コマンドで確認します。
```bash
% kubectl get storageclass
NAME                 PROVISIONER                RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
standard (default)   k8s.io/minikube-hostpath   Delete          Immediate           false                  22m
```
この結果から、 minikube がホストマシン上のディレクトリを使って自動でストレージを作ることが分かります。

次にdocker-compose.ymlがあるディレクトリに以下の4ファイルを作ります。
a, mysql-secret.yaml
b, PersistentVolumeClaim（PVC） mysql-pvc.yaml
c, MySQL Deployment mysql-deployment.yaml
d, MySQL Service mysql-service.yaml
```mysql-pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```
```mysql-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: mysql-root-password
            - name: MYSQL_DATABASE
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: mysql-database
            - name: MYSQL_USER
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: mysql-user
            - name: MYSQL_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: mysql-password
          volumeMounts:
            - name: mysql-storage
              mountPath: /var/lib/mysql
      volumes:
        - name: mysql-storage
          persistentVolumeClaim:
            claimName: mysql-pvc
```
```mysql-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  type: ClusterIP
  selector:
    app: mysql
  ports:
    - port: 3306
      targetPort: 3306
```
格納するディレクトリ名は慣例で k8s になります。

設定の適用
```bash
kubectl apply -f k8s/secrets/mysql-secret.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml
```

設定の確認
```bash
% kubectl get pods
NAME                    READY   STATUS    RESTARTS        AGE
mysql-6d667f556-kwmzd   1/1     Running   1 (4m14s ago)   7h4m

% kubectl get pvc
NAME        STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
mysql-pvc   Bound    pvc-c6b60292-fee5-4647-b780-3f86109ac7a5   1Gi        RWO            standard       <unset>                 32s

% kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP    48m
mysql        ClusterIP   10.105.118.9   <none>        3306/TCP   20s
```
# 5, minikube の中で backend を起動する
docker-compose.ymlがあるディレクトリに以下の3ファイルを作ります。
a, backend-secret.yaml
b, Backend Deployment backend-deployment.yaml
c, Backend Service backend-service.yaml
```backend-deployment.yaml
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
          image: thatisg/mysql-minikube-backend:1.1
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_DATASOURCE_URL
              value: jdbc:mysql://mysql:3306/webgame
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: backend-secret
                  key: backend-user
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: backend-secret
                  key: backend-password
```
```backend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
spec:
  type: ClusterIP
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
```
設定の適用
```bash
kubectl apply -f k8s/secrets/backend-secret.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
```
「deployment.apps/backend created」といった内容のメッセージが表示されればOKです。
設定の確認
```bash
% kubectl get pods
NAME                       READY   STATUS    RESTARTS      AGE
backend-6d9f4997fb-7d7rf   1/1     Running   0             7m30s
mysql-6d667f556-kwmzd      1/1     Running   1 (86m ago)   8h

% kubectl get svc
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
backend      ClusterIP   10.110.167.182   <none>        8080/TCP   43m
kubernetes   ClusterIP   10.96.0.1        <none>        443/TCP    9h
mysql        ClusterIP   10.105.118.9     <none>        3306/TCP   8h
```
もしも問題が発生してイメージをもう一度作り直す必要がある場合の修正方法は、

まずはPodを直接止めずに、Deploymentを止めます。
```bash
kubectl delete deployment backend
```
これでbackend Podは全て停止しますが、MySQLはそのままになります。

逆にやってはいけない方法が
```bash
kubectl delete pod backend-xxxxx
```
Kubernetesが即座に新しいPodを作り直します
（Deployment管理下なので意味がない）

変更を加えたらbackendを再ビルド
```basg
docker build -t thatisg/mysql-minikube-backend:1.1 ./mysql-minikube-backend
docker push thatisg/mysql-minikube-backend:1.1
```
backend-deployment.yamlのバージョンを変更してから以下のコマンドを実行して適用します。
```basg
% kubectl apply -f k8s/mysql-deployment.yaml
deployment.apps/mysql configured
```
「kubectl get pods」コマンドを使って実行できているかを確認しましょう。
# 6, minikube の中で frontend を起動する
docker-compose.ymlがあるディレクトリに以下の2ファイルを作ります。
a, Frontend Deployment frontend-deployment.yaml
b, Frontend Service frontend-service.yaml
```frontend-deployment.yaml
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
          image: thatisg/mysql-minikube-frontend:1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 80
```
```frontend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend
spec:
  type: NodePort
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 80
      nodePort: 30080
```
設定の適用
```bash
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml
```
設定の確認
```bash
% kubectl get pods
NAME                        READY   STATUS    RESTARTS       AGE
backend-6d9f4997fb-7d7rf    1/1     Running   0              33m
frontend-784c6d77d5-7jnb5   1/1     Running   0              63s
mysql-6d667f556-kwmzd       1/1     Running   1 (112m ago)   8h

% kubectl get svc
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
backend      ClusterIP   10.110.167.182   <none>        8080/TCP   63m
frontend     ClusterIP   10.101.166.253   <none>        80/TCP     80s
kubernetes   ClusterIP   10.96.0.1        <none>        443/TCP    9h
mysql        ClusterIP   10.105.118.9     <none>        3306/TCP   8h
```
# 7, NodePort を使ってアプリをブラウザに表示する
最後にNodePort を使ってイメージから作成したアプリが動作しているか、確認します。
NodePortの確認
```bash
% kubectl get svc frontend
NAME       TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
frontend   NodePort   10.101.166.253   <none>        80:30080/TCP   22m
```
MinikubeのIPを確認
```bash
% minikube ip
192.168.49.2
```
この結果から、本来であれば http://192.168.49.2:30080 でアプリにブラウザからアクセスできるはずなのですが、mac のローカル環境では「minikube service frontend」コマンドを実行すると見れました。
