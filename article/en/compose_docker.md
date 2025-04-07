# Introduction
AWS Spot Instances are cost-effective, but they have the drawback of potentially being interrupted.
To address this, I want to explore whether it's possible to maintain continuous operation of a web application even if an instance is stopped, using Kubernetes, Weave Flux, and Docker.
Since this involves a lot of work, I plan to divide it into several articles.
# What This Article Covers
As we'll be creating Docker images, the first step is to adapt the development environment to support Docker.
Since this is a development environment, a Bastion EC2 instance will be set up to connect to an AWS RDS instance using port forwarding for the required database connections.
Additionally, since the project was initially built as a Maven project, we will switch to Gradle.
#### Overall Workflow
1, Install Docker Desktop
2, Switch from Maven to Gradle
3, Create a Dockerfile for the backend
4, Create a Dockerfile for the frontend
5, Create a docker-compose.yml file
6, Configure Visual Studio Code to work with Docker containers
7, Set up a Bastion EC2 instance to connect to AWS RDS with port forwarding
8, Build Docker images and start containers
9, Connect to the running Docker containers from Visual Studio Code
#### Environment
The backend is developed in Java, and the frontend uses React + TypeScript.
The docker-compose.yml file is placed in the root directory of the project to manage both frontend and backend services together.
# 1, Install Docker Desktop
Since there are many articles available online about how to install Docker Desktop, this article will not cover that part. Please refer to those resources.
# 2, Switch from Maven to Gradle
Install Gradle.
```bash
brew install gradle
```
Rewrite pom.xml into build.gradle.
```json:build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.0'
    id 'io.spring.dependency-management' version '1.1.3'
}

group = 'com.spring'
version = '0.0.1-SNAPSHOT'
description = 'XXX XXX'
sourceCompatibility = '23'

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Dependencies
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-mail'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // MySQL Connector
    runtimeOnly 'com.mysql:mysql-connector-j'

    // Development Tools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'

    // Testing Dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```
When I ran the "gradle -v" command, an error occurred
```bash
ERROR: JAVA_HOME is set to an invalid directory: /usr/local/Cellar/openjdk/23.0.1/libexec/openjdk.jdk/Contents/Home

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```
so I reconfigured the Java environment variables.
```bash
echo 'export JAVA_HOME="/usr/local/Cellar/openjdk/23.0.2/libexec/openjdk.jdk/Contents/Home"' >> ~/.zshrc
source ~/.zshrc
```
I ran the build command to confirm that there were no issues with Gradle.
```bash
gradle clean build
```
# 3, Create a Dockerfile for the backend
```Dockerfile:Dockerfile
# 1. Base image to build Java application using Maven
FROM gradle:8.13-jdk23 AS build

# Set the working directory in the Docker image
WORKDIR /home/app

# Copy application source code into the working directory in the Docker image
COPY src /home/app/src
COPY build.gradle /home/app
COPY settings.gradle /home/app

# Use Gradle to build the project
RUN gradle clean build --no-daemon

# 2. Base image to run the Java JAR file
FROM fedora:latest

# Install the required packages.
RUN dnf update -y
RUN dnf install -y java-latest-openjdk-devel
RUN sudo dnf install -y openssh
RUN sudo dnf install -y openssh-clients

# Set environment variable PATH
ENV PATH="${PATH}:/usr/bin/ssh"

COPY ../../XXX-XXX.pem /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa

# A script to set up an SSH tunnel and then launch the Java application.
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

# Copy the built JAR file from the build stage
COPY --from=build /home/app/build/libs/*.jar /usr/local/lib/XXX-XXX.jar

# Specify the port the application uses
EXPOSE 8080
```

# 4, Create a Dockerfile for the frontend
```Dockerfile:Dockerfile
# Build stage
FROM node:16-alpine as builder

# Set the working directory in the Docker image
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock) to leverage Docker cache
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the application source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Set the working directory to nginx asset directory
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy static assets from builder stage
COPY --from=builder /app/build .

# Containers run nginx with global directives and daemon off
ENTRYPOINT ["nginx", "-g", "daemon off;"]
```
# 5, Create a docker-compose.yml file
Since the frontend and backend are stored in separate directories, we will create a docker-compose.yml file.
```yml:docker-compose.yml
services:
    frontend:
        build:
            context: ./XXX_XXX_interface/react-typescript
        image:
            XXX_XXX_front:latest
        ports:
            - '3000:3000'
        volumes:
            - ./XXX_XXX_interface/react-typescript:/app
            - /app/node_modules
        environment:
            - NODE_ENV=development
        stdin_open: true
        tty: true

    backend:
        build: ./XXX_XXX
        volumes:
            - ./XXX_XXX:/app
        image:
            XXX_XXX:latest
        ports:
            - '8080:8080'
        environment:
            - SPRING_PROFILES_ACTIVE=development
        command: ['./mvnw', 'spring-boot:run']

volumes:
    frontend_node_modules:
    backend_maven:
```
# 6, Configure Visual Studio Code to work with Docker containers
The setup will follow the instructions provided in the Microsoft learning site linked below.
[Use a Docker container as a development environment with Visual Studio Code](https://learn.microsoft.com/en-us/training/modules/use-docker-container-dev-env-vs-code/)
Following the steps will generate the following files.
```json:devcontainer.json
// For format details, see https://aka.ms/devcontainer.json. For config options, see the
// README at: https://github.com/devcontainers/templates/tree/main/src/java
{
	"name": "Java",
	// Or use a Dockerfile or Docker Compose file. More info: https://containers.dev/guide/dockerfile
	"image": "mcr.microsoft.com/devcontainers/java:1-21-bullseye",

	"features": {
		"ghcr.io/devcontainers/features/java:1": {
			"version": "none",
			"installMaven": "false",
			"installGradle": "true"
		}
	}

	// Use 'forwardPorts' to make a list of ports inside the container available locally.
	// "forwardPorts": [],

	// Use 'postCreateCommand' to run commands after the container is created.
	// "postCreateCommand": "java -version",

	// Configure tool-specific properties.
	// "customizations": {},

	// Uncomment to connect as root instead. More info: https://aka.ms/dev-containers-non-root.
	// "remoteUser": "root"
}
```
# 7, Set up a Bastion EC2 instance to connect to AWS RDS with port forwarding
The AWS setup uses a combination of a VPC, public subnet with a bastion server, and a private subnet with an RDS instance.
1, VPC
There was no particular preference for the CIDR block, so I used 172.31.0.0/16.
2, Public Subnet
The CIDR block is set to 172.31.64.0/20.
In the route table, traffic within the VPC's IP range is routed locally, and all other traffic is directed to the internet gateway.
3, Bastion Server
The OS is Amazon Linux, and the instance type is the smallest available.
A key pair is created for SSH access, and the security group allows inbound traffic only on port 22.
4, Private Subnet
The CIDR block is 172.31.80.0/20.
Since it’s within the same VPC, the route table is set up similarly to the public subnet, routing the VPC’s IP range locally.
5, RDS
MySQL is selected and deployed in the private subnet.
The security group allows inbound access on port 3306 from the security group assigned to the bastion server.
An outbound rule is also added to the bastion server's security group to allow access to RDS on port 3306.

To set up port forwarding and connect to RDS, the following command was used:
(Adjust the port number as needed.)
```bash
ssh -i my-key.pem -N -L 33066:<RDSエンドポイント>:3306 ec2-user@<Bastion HostのパブリックIP>
mysql -h 127.0.0.1 -P 33066 -u admin -p
```
# 8, Build Docker images and start containers
First, build the Docker image.
Run the following command in the directory where the docker-compose.yml file is located.
```bash
docker compose build
```
Start the container from the built image.
```bash
docker run -d --name containerName -p 8080:8080
```

# 9, Connect to the running Docker containers from Visual Studio Code
Install Dev Containers as an extension.

Open the command palette.
Search for and select Dev Containers: Reopen in Container.
If the blue connection indicator appears in the lower-left corner, the setup was successful.
