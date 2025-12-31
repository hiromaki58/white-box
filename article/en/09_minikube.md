# Introduction
This time, I would like to use Minikube to verify whether the Docker images we have created so far can run correctly on Kubernetes.
# What This Article Covers
1, First
2, Build the images and upload them to Docker Hub
3, Start Minikube
4, Start MySQL inside Minikube
5, Start the backend inside Minikube
6, Start the frontend inside Minikube
7, Use NodePort to access the application from the browser
# 1, First
Minikube is a local test environment that allows you to run Kubernetes and verify configuration settings, which can be complex in a full Kubernetes setup.
I initially considered following along with a YouTube video to experiment with Minikube, but I decided to build the Docker images myself instead.
# 2, Build the images and upload them to Docker Hub
First, I will upload the images created in the previous article (3. Set up the CD portion of the CI/CD Pipeline) to Docker Hub.
If you already have a Docker account and are logged in, you can upload the images from your local environment using the following commands.
```bash
docker login

docker build -t thatisg/mysql-minikube-backend:1.0 ./mysql-minikube-backend
docker build -t thatisg/mysql-minikube-frontend:1.0 ./mysql-minikube-frontend/react-typescript

docker push thatisg/mysql-minikube-backend:1.0
docker push thatisg/mysql-minikube-frontend:1.0
```
# 3, Start Minikube
If Docker is not installed locally, please install it first.(I am using Homebrew since I am a Mac user.)
At this point, Docker must be running on your local machine.
```bash
brew install minikube
minikube version
```
If the version information is displayed, the installation is complete.

If you have used minikube before, it may be a good idea to reset it once.
```bash
minikube delete
minikube start --driver=docker --container-runtime=containerd
```
Finally, if the following message is displayed, Minikube is running correctly.
```bash
Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
```
Command to verify operation
```bash
% minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```
# 4, Start MySQL inside Minikube
Once Minikube is up and running, each local environment must be configured to use the images (applications) uploaded to Docker Hub.
Specifically, this includes settings such as:
- Which images to use,
- The ID and password for accessing the database,
- Version numbers, and port numbers.
Since the images uploaded to Docker Hub this time consist of a DB / Backend / Frontend architecture, we will configure each component accordingly.

I would like to use a PersistentVolumeClaim (PVC) to persist the data used by the images. However, since a PVC cannot be created automatically unless a StorageClass exists, I first check it using the kubectl get storageclass command.
```bash
% kubectl get storageclass
NAME                 PROVISIONER                RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
standard (default)   k8s.io/minikube-hostpath   Delete          Immediate           false                  22m
```
From this result, we can see that Minikube automatically provisions storage using a directory on the host machine.

Next, in the directory where the docker-compose.yml file is located, create the following 4 files.
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
By convention, the directory name used for storing these files is k8s.

Applying the configuration
```bash
kubectl apply -f k8s/secrets/mysql-secret.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml
```

Command to verify operation
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
# 5, Start the backend inside Minikube
Next, in the directory where the docker-compose.yml file is located, create the following 3 files.
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
Applying the configuration
```bash
kubectl apply -f k8s/secrets/backend-secret.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
```
If you see messages such as “deployment.apps/backend created”, the configuration has been applied successfully.

Command to verify operation
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
If an issue occurs and you need to rebuild the image, the correct way to handle it is not to stop the Pods directly, but to stop the Deployment first.
```bash
kubectl delete deployment backend
```
This will stop all backend Pods, while MySQL will continue running.

On the other hand, the approach you should not take is:
```bash
kubectl delete pod backend-xxxxx
```
Kubernetes will immediately recreate the Pod
(Since it is managed by a Deployment, this approach is ineffective).

After making the changes, rebuild the backend.
```basg
docker build -t thatisg/mysql-minikube-backend:1.1 ./mysql-minikube-backend
docker push thatisg/mysql-minikube-backend:1.1
```
After updating the version in backend-deployment.yaml, run the following command to apply the changes.
```basg
% kubectl apply -f k8s/mysql-deployment.yaml
deployment.apps/mysql configured
```
Use the kubectl get pods command to verify that everything is running correctly.
# 6, Start the frontend inside Minikube
Next, in the directory where the docker-compose.yml file is located, create the following 2 files.
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
Applying the configuration
```bash
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml
```
Command to verify operation
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
# 7, Use NodePort to access the application from the browser
Finally, we will verify that the application created from the image is running correctly using NodePort.
Command to verify NodePort
```bash
% kubectl get svc frontend
NAME       TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
frontend   NodePort   10.101.166.253   <none>        80:30080/TCP   22m
```
Check the Minikube IP
```bash
% minikube ip
192.168.49.2
```
Based on this result, in theory, the application should be accessible from a browser at http://192.168.49.2:30080.
However, in the local macOS environment, I was able to access it by running the minikube service frontend command.
