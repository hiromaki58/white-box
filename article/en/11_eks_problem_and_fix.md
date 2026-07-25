# Introduction
This article introduces several issues encountered while building an environment to run a web application on AWS EKS, along with the solutions used to resolve them.
# Topics Covered in This Article
1, Error messages related to the VPC CNI plugin
2, Missing NAT Gateway configuration
3, "Instances failed to join the Kubernetes cluster" error
4, Status: Pending displayed when running the kubectl describe command
5, Errors encountered while running the eksctl create cluster -f cluster.yaml command
6, Kubernetes Secrets not being applied
7, Unable to connect to the database because passwords were not injected into Pods
8, Architecture mismatch issues
9, Unable to connect to AWS RDS because the database was not recognized
10, Unable to access the application using curl http://localhost:8080
# 1, Error messages related to the VPC CNI plugin
### Issue
```shell
recommended policies for vpc-cni addon but OIDC disabled
```
### Cause
In standard Kubernetes environments, Pods are assigned IP addresses from a virtual network that is separate from the cloud provider's VPC.
However, Amazon EKS was designed to allow Pods to communicate directly within the VPC network.
To achieve this, AWS uses the VPC CNI plugin. 
The VPC CNI plugin utilizes Elastic Network Interfaces (ENIs) attached to EC2 instances and assigns IP addresses from the VPC CIDR range directly to Pods.
As a result, additional configuration is required to allow the VPC CNI plugin to function correctly.
### Solution
Add the following configuration to the cluster.yaml file.
```yaml
iam:
withOIDC: true
```
# 2, Missing NAT Gateway configuration
### Issue
```shell
Resource handler returned message: "[Issue(Code=Ec2SubnetInvalidConfiguration, Message=One or more Amazon EC2 Subnets of [subnet-xxx] for node group spot-ng does not automatically assign public IP addresses to instances launched into it.
If you want your instances to be assigned a public IP address, then you need to enable auto-assign public IP address for the subnet."
```
### Cause
Nodes and Pods running in private subnets do not have public IP addresses. However, they still require outbound internet access to perform tasks.
To provide outbound internet connectivity while keeping the resources in private subnets, a NAT Gateway is required.
### Solution
Create a NAT Gateway in a public subnet and configure it as a Public NAT Gateway. 
An Elastic IP (EIP) must also be allocated and associated with the NAT Gateway.
Once the NAT Gateway is created, update the route table of the private subnet to direct internet-bound traffic through the NAT Gateway.
# 3, "Instances failed to join the Kubernetes cluster" error
### Issue
```shell
Instances failed to join the kubernetes cluster
```
### Investigation
Even "kubectl get nodes" command is executed, "Instances failed to join the kubernetes cluster" is shown.
### Cause
- Node IAM Role was not properly configured and assigned to the Node Group.
- Network ACL blocked HTTPS traffic on port 443 and the ephemeral port range (1024–65535).
### Solution
To resolve the issue, the node IAM role and network configuration were reviewed and corrected.
１．Create a Dedicated IAM Role for Worker Nodes
Manually create an IAM role for the EKS worker nodes and attach the following AWS-managed policies:
```shell
AmazonEC2ContainerRegistryReadOnly
AmazonEKS_CNI_Policy
AmazonEKSWorkerNodePolicy
```
２，Explicitly Specify the Node IAM Role in cluster.yaml
```shell
iam:
  instanceRoleARN: arn:aws:iam::000000000000:role/webgame-eks-node-instance-role
```
３，Restore the Network ACL to the Default Configuration
The following ports must be allowed:
- HTTPS (TCP 443) for communication with AWS services such as ECR and the EKS control plane
- Ephemeral ports (TCP/UDP 1024–65535) for return traffic
４．After executing"eksctl delete cluster --region=us-east-1 --name=<Claster_name>", execute these commands below
```shell
% source eks/cluster-values.yaml
% eksctl create cluster -f cluster.yaml

% kubectl get svc
NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.100.0.1   <none>        443/TCP   11m
```
If kubernetes returns the services, it is succeeded.
```shell
% kubectl get nodes
NAME                             STATUS   ROLES    AGE     VERSION
ip-172-31-107-149.ec2.internal   Ready    <none>   4m43s   v1.29.15-eks-ecaa3a6
```
Communication between kubectl and the EKS API server was successful, confirming that the worker node had successfully joined the EKS control plane.
# 4, Status: Pending displayed when running the kubectl describe command
### Issue
```shell
% kubectl get node
NAME                           STATUS   ROLES    AGE     VERSION
ip-172-31-29-74.ec2.internal   Pending    <none>   2m22s   v1.30.14-eks-3385e9b
```
### Cause
The node successfully joined the control plane, but EC2 instance type were not enough CPU and memory resources available to schedule Pods.
### Solution
Update the node group's EC2 instance type to a larger size with additional CPU and memory resources.
# 5, Errors encountered while running the "eksctl create cluster -f cluster.yaml" command
```shell
Error: operation error EC2: DescribeVpcs, https response error StatusCode: 400, RequestID: xxxxxxxxxxxxxx, api error InvalidVpcID.NotFound: The vpc ID '${vpc_id}' does not exist
```
### Cause
The cluster.yaml file used by eksctl does not support retrieving values directly from Kubernetes Secrets or environment variable references in the same way that Kubernetes manifests do.
Kubernetes Secrets cannot be referenced from cluster.yaml because cluster.yaml is processed by eksctl before any Kubernetes resources are created.
### Solution
Specify the required values directly in the cluster.yaml file instead of attempting to retrieve them from Secrets or environment variables.
# 6, Kubernetes Secrets not being applied
### Issue
```shell
using password: NO
```
### Cause
Environment variables from Kubernetes Secrets are injected only when a new Pod is created.
Therefore, even if the Secret is updated after the Pod has already been created, the running Pod will not automatically receive the updated values.
### Solution
Run the following command to restart the Deployment and create new Pods:
```yaml
kubectl apply -f secret.yaml
kubectl rollout restart deployment backend
```
# 7, Unable to connect to the database because passwords were not injected into Pods
### Issue
```shell
Access denied for user 'root' ... (using password: NO)
```
### Cause
Password mismatch
### Investigation
Using a temporary test Pod, it was confirmed that connectivity from the EKS cluster to Amazon RDS was working correctly.
(Since Amazon Linux does not include the MySQL client package by default, the MariaDB client was used instead.)
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
The following command was used to inspect the Secret.
```shell
kubectl get secret spring-db-secret -o yaml 
```
The encoded values were then decoded and compared with the expected credentials.
```shell
echo <password> | base64 --decode" command result.
```
If the decoded values do not match the expected username and password, the application may be running with outdated configuration.
The issue was ultimately caused by the application Pods running an older container image.
Although the Secret contained the correct credentials, the latest image was not being pulled from Amazon ECR. As a result, the application continued to use outdated configuration values.
The image tag was not explicitly specified, causing Kubernetes to reuse a previously cached image on the worker node.
### Solution
Explicitly specify the image version or use the latest tag in the config file.
This forces Kubernetes to pull the latest image from Amazon ECR instead of relying on a cached image stored on the node.
```yaml : circle/config.yaml
- run:
    name: Push to Amazon ECR
    command: |
      aws --version
      aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
      docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:latest
      docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game_front:latest
```
# 8, Architecture mismatch issues
### Issue
```shell
no match for platform in manifest
```
### Cause
My Docker images were built on a Mac with Apple Silicon, which uses the ARM64 architecture.
However, the Amazon EC2 instances used by the EKS node group were based on the AMD64 (x86_64) architecture.
As a result, Kubernetes was unable to run the container image and reported an architecture compatibility error.
### Solution
Build the Docker image explicitly for the AMD64 architecture before pushing it to Amazon ECR.
```yaml
- run:
  name: Build Docker Images
  command: |
    docker build --platform linux/amd64 -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/web_game:$IMAGE_TAG ./infra/minikube/mysql-minikube-backend
```
# 9, Unable to connect to AWS RDS because the database was not recognized
### Issue
```shell
Unknown database 'webgame'
```
### Cause
The database names did not match exactly.
### Investigation
A temporary test Pod was created to verify connectivity to Amazon RDS and to inspect the available databases.
During the investigation, the database name stored in the Kubernetes Secret was compared with the actual database name in Amazon RDS.
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
The following command was used to inspect the Secret configuration:
```shell
kubectl get secret spring-db-secret -o yaml 
```
It was discovered that the database names did not match exactly due to differences in letter casing (uppercase and lowercase characters).
### Solution
Since renaming an existing database in Amazon RDS could have significant side effects on applications and integrations, the Secret configuration was updated instead.
Update the Secret so that the database name exactly matches the database name configured in Amazon RDS, including letter casing.
After updating the Secret, apply the changes using:
```shell
kubectl apply -f ./secrets/secret.yaml
kubectl rollout restart deployment backend
```
# 10, Unable to access the application using curl http://localhost:8080
### Investigation
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
The following message was displayed in the AWS Management Console, indicating that the application was not responding correctly through the Load Balancer.
```shell
Status
0 of 1 instance in service
```
### Cause
The frontend container was configured to run Nginx, which listens on port 80 by default.
### Solution
Update the frontend Deployment and Service manifests so that the container port and service target port match the port used by Nginx.
```yaml : frontend-deployment.yaml
- containerPort: 80
```
```yaml : frontend-service.yaml
targetPort: 80
```
After updating the manifests, apply the changes to the cluster:
```shell
kubectl apply -f ./frontend/frontend-deployment.yaml
kubectl apply -f ./frontend/frontend-service.yaml
kubectl rollout restart deployment frontend
```
