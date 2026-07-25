# Introduction
This article is the final installment in a six-part series on how to build a highly available web application that continues running even when using Spot Instances.
In this article, you'll learn how to deploy a web application on Amazon Elastic Kubernetes Service (EKS) and make it accessible through a web browser.
# Topics Covered in This Article
1, Modify the .circleci/config.yml file to build and push Docker images to Amazon ECR.
2, Create a Kubernetes Secret for the Spring Boot application.
3, Create an Amazon EKS cluster.
4, Create a Security Group to allow access from EKS to Amazon RDS.
5, Verify connectivity through the RDS Security Group.
6, Create and apply the Kubernetes Deployment and Service manifests.
7, Access the web application from a browser.

# 1, Modify the .circleci/config.yml file to build and push Docker images to Amazon ECR.
In the previous articles, the application was deployed using Minikube and Amazon ECS. In this article, the deployment target is Amazon EKS, so the .circleci/config.yml file must be updated to build Docker images and push them to Amazon ECR.
```yaml : config.yaml
- run:
    name: Push to Amazon ECR
    command: |
        aws --version
        aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest
        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest
```

# 2, Create a Kubernetes Secret for the Spring Boot application.
In the previous setup, CircleCI used an internal database for running integration tests.
This time, however, the application will connect to an Amazon RDS instance deployed in a private subnet.
To provide the database connection information securely, create a Kubernetes Secret and pass the RDS connection settings to the Spring Boot application.
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
To continue running database-backed tests in CircleCI while allowing the application running on Amazon EKS to connect to Amazon RDS, the configuration strategy needs to be updated.
Configure the Spring Boot application to read its database settings exclusively from environment variables.
```properties : application.properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```
Note:
Kubernetes passes configuration values to containers as environment variables.
Only the database credentials required for the CircleCI test environment need to be stored in CircleCI. The production Amazon RDS username and password do not need to be stored in CircleCI, since they are managed securely by Kubernetes Secrets.

If your EKS cluster is already running, execute the following command to store the database credentials in Kubernetes as a Secret.
Note: 
Adjust the file path as necessary, depending on your current working directory when running the command.
```bash
kubectl apply -f ./secrets/secret.yaml
```

# 3, Create an Amazon EKS cluster.
### Build the AWS infrastructure
The next step is to build the AWS infrastructure.
At this stage, the goal is simply to create the Amazon EKS cluster.
Think of the cluster as the "box" that will later host your application.
No application is deployed in this step; only the infrastructure required to run Kubernetes workloads is created.

Since this guide focuses on setting up a development and testing environment, only the minimum required resources are provisioned.
- VPC
- Public subnet x1
- Private subnet x1
- Internet gateway x1
- Route table

The route table configuration is as follows:
public subnet
Destination: VPC CIDR → local
Destination: 0.0.0.0/0 → Internet Gateway
private subnet
Destination: 172.31.0.0/16 → local

## Create the cluster
The EKS cluster is created from the command line using eksctl.
First, install eksctl on your local machine and verify that the installation was successful.
```shell
brew install eksctl
brew install kubectl
eksctl version
kubectl version --client
```
The cluster.yaml file is created locally, and the cluster is provisioned from your local environment
The cluster is created from the local machine for two reasons:
- Creating an EKS cluster requires AWS administrative privileges.
- Cluster creation is a one-time infrastructure provisioning task rather than part of the CI/CD pipeline.
Because of this, there is no need to execute eksctl from CircleCI or any other CI system.

Before creating the EKS cluster, configure your local environment so that it can access the target AWS account.
```shell
brew install awscli
aws configure
AWS Access Key ID [None]: xxxxxxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: yyyyyyyyyyyyyyyyyyyyyyyyyy
Default region name [None]: us-east-1
Default output format [None]: json
aws sts get-caller-identity
```

Once you have confirmed that your local machine can successfully authenticate and access the target AWS account, create the cluster by executing the following command:
```bash
aws sts get-caller-identity
{
    "UserId": "AIDAxxxxxxxxxxxx",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/your-user"
}
eksctl create cluster -f cluster.yaml
```
The cluster.yaml file used in this guide is shown below.
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
After running the command, it typically takes 20 to 30 minutes for the EKS cluster to be fully provisioned.
Once the process is complete, run the following command.
If the output shows that the node is in the Ready state, the following has been successfully completed:
- kubectl can communicate with the Amazon EKS API server.
- The worker node has successfully joined the EKS control plane.
```bash
% kubectl get nodes
NAME                             STATUS   ROLES    AGE     VERSION
ip-172-31-107-149.ec2.internal   Ready    <none>   4m43s   v1.30.14-eks-f69f56f
```
When the cluster is created, the following sequence of events takes place:
1, An Amazon EC2 instance is launched.
2, The kubelet service starts on the EC2 instance.
3, kubelet connects to the Amazon EKS control plane (Kubernetes API server).
4, The worker node is authenticated using its IAM role.
5, The node registers itself with the Kubernetes cluster.
6, Once initialization is complete, the node status changes to Ready.

# 4, Create a Security Group to allow access from EKS to Amazon RDS.
In this demonstration, the Amazon RDS instance is deployed in a private subnet, while the Amazon EKS worker nodes are deployed in a public subnet.
The key design goals are:
- Only Pods running on Amazon EKS should be able to access the RDS instance.
- The configuration should not depend on IP addresses, allowing Pods and worker nodes to be replaced without requiring changes to the RDS security rules.
- Avoid using 0.0.0.0/0, which would allow access from any IP address.

To achieve this, add the Security Group associated with the EKS worker nodes (the EC2 instances in the node group) to the inbound rules of the Amazon RDS Security Group.

# 5, Verify connectivity through the RDS Security Group.
The next step is to verify that the Security Group configuration is working correctly.
### 1, Create a test pod
```shell
% kubectl run test --image=public.ecr.aws/amazonlinux/amazonlinux:2 --restart=Never --command -- sleep 3600 pod/test created
```
### 2, Verify that pod is running
```shell
% kubectl get pod test -o wide
NAME   READY   STATUS    RESTARTS   AGE   IP              NODE                           NOMINATED NODE   READINESS GATES
test   1/1     Running   0          20s   172.31.10.245   ip-172-31-5-126.ec2.internal   <none>           <none>
```
### 3, Access the test pod
```shell
kubectl exec -it test -- bash
```
In this example, test is the name of the temporary Pod.
### 4, Install a database client and connect to Amazon RDS
Once inside the Pod, install a database client and connect to the Amazon RDS instance.

Amazon Linux does not include the MySQL client package by default. Instead, install the MariaDB client, which is fully compatible with the MySQL protocol and can be used to connect to a MySQL-compatible Amazon RDS instance.
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
### 5, Clean Up the Test Pod
```shell
exit
kubectl delete pod test
```

# 6, Create and apply the Kubernetes Deployment and Service manifests.
Next, configure the Deployment so that it can access the secrets defined in the Spring Boot application.properties file.
To do this, create the following Kubernetes manifest files:
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
Reference the Secret in the Deployment manifest as shown below:
```yaml : backend-development.yaml
envFrom:
  - secretRef:
      name: spring-db-secret
```
Apply the Kubernetes manifests in the following order.
```bash
kubectl apply -f ./secrets/secret.yaml
kubectl apply -f ./backend/backend-deployment.yaml
kubectl apply -f ./backend/backend-service.yaml
kubectl apply -f ./frontend/frontend-deployment.yaml
kubectl apply -f ./frontend/frontend-service.yaml
```
After applying the manifests, verify that the Pods have been created successfully.
If the Pod status is Running, the Deployment has been applied successfully and the application is running with the updated configuration.
```bash
% kubectl get pods
NAME                        READY   STATUS    RESTARTS      AGE
backend-8db6d49fd-9skv5     1/1     Running   3 (38s ago)   81s
frontend-65d6f8487f-b7qrp   1/1     Running   0             70s
```

# 7, Access the web application from a browser.
### 1, Verify that the services are running
```shell
% kubectl get svc
NAME               TYPE           CLUSTER-IP       EXTERNAL-IP                                                               PORT(S)        AGE
backend-service    ClusterIP      10.100.219.184   <none>                                                                    8080/TCP       6h48m
frontend-service   LoadBalancer   10.100.39.47     123456789-123456789.us-east-1.elb.amazonaws.com   80:31984/TCP   6h48m
kubernetes         ClusterIP      10.100.0.1       <none>                                                                    443/TCP        7h22m
```
### 2, Verify that communication between Pods is working correctly
Check whether the frontend Pod can communicate with the backend Pod.
```shell
kubectl exec -it <frontend-pod> -- curl http://backend-service:8080
```
### 3, Verify access to the frontend
```shell
% kubectl get svc frontend-service
NAME               TYPE           CLUSTER-IP     EXTERNAL-IP                                                               PORT(S)        AGE
frontend-service   LoadBalancer   10.100.39.47   123456789-123456789.us-east-1.elb.amazonaws.com   80:31984/TCP   6h52m
```
From the result, we can confirm that:
- A Service object named frontend-service has been created in Kubernetes.
- A Load Balancer has been provisioned.
- A DNS name has been assigned to the Load Balancer.
### 4, Access the application in a web browser using the retrieved EXTERNAL-IP.
```shell
http://<EXTERNAL-IP>
```
Finally, because Amazon EKS can be expensive to operate, delete the EKS cluster when it is no longer needed.
```bash
eksctl delete cluster -f cluster.yaml
aws eks list-clusters
```
