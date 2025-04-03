# はじめに
AWSのスポットインスタンスは費用が安いのですが、停止するという弱点があります。
そこでKubernetes, Weave Flux, Dockerを使ってインスタンスが停止してもWebアプリケーションを継続運用できないか検証したいと思います。
作業量が多いことが予想されるので、いくつかに分けて記事にしたいと思います。
# この記事で実施する内容
Dockerイメージを作成することになるので、まずは作業環境をDockerに対応できるように変更します。
開発環境なので必要なDB接続ははAWS RDSへBastion EC2 インスタンスを設定し、ポートフォワーディングを行って接続します。
また最初mavenプロジェクトとして構築していたので、それをgradleへ切り替えたときの方法も、ここに書きましたしています。
#### 作業全体の流れ
1, Docker Desktopのインストール
2, mavenからgradleへの変更
3, Backend用Dockerfileファイルの作成
4, Frontend用Dockerfileファイルの作成
5, docker-compose.ymlファイルの作成
6, Dockerコンテナに対するVisual Studio Codeの設定
7, AWS RDSへBastion EC2 インスタンスを設定し、ポートフォワーディングを行って接続するための設定
8, Dockerイメージの作成とDockerコンテナを起動
9, 起動したDockerコンテナにVisual Code Studioから接続する
#### 動作環境
バックエンドはJavaを、フロントエンドではReact + TypeScriptを使いました。
プロジェクトのルートディレクトリにdocker-compose.ymlファイルを配置し、フロントエンドとバックエンドのサービスを一緒に管理します。
# 1, Docker Desktopのインストール
Docker Desktopのインストール方法はここで紹介せずとも、多くの記事が公開されているので、そちらを参考にしてください。
# 2, mavenからgradleへの変更
gradleをインストール
```bash
brew install gradle
```
pom.xmlをbuild.gradleへ書き直す。
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
"gradle -v"コマンドを実行すると、エラーが発生したので、
```bash
ERROR: JAVA_HOME is set to an invalid directory: /usr/local/Cellar/openjdk/23.0.1/libexec/openjdk.jdk/Contents/Home

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```
Javaの環境変数を再設定
```bash
echo 'export JAVA_HOME="/usr/local/Cellar/openjdk/23.0.2/libexec/openjdk.jdk/Contents/Home"' >> ~/.zshrc
source ~/.zshrc
```
ビルドコマンドを実行してgradleに問題ががないことを確認
```bash
gradle clean build
```
# 3, Backend用Dockerfileファイルの作成
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

# 必要なパッケージをインストール
RUN dnf update -y
RUN dnf install -y java-latest-openjdk-devel
RUN sudo dnf install -y openssh
RUN sudo dnf install -y openssh-clients

# Set environment variable PATH
ENV PATH="${PATH}:/usr/bin/ssh"

COPY ../../XXX-XXX.pem /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa

# SSHトンネルを設定し、その後Javaアプリを起動するスクリプト
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

# Copy the built JAR file from the build stage
COPY --from=build /home/app/build/libs/*.jar /usr/local/lib/XXX-XXX.jar

# Specify the port the application uses
EXPOSE 8080
```

# 4, Frontend用Dockerfileファイルの作成
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
# 5, docker-compose.ymlファイルの作成
フロントとバックエンドが別のディレクトリに格納されているのでdocker-compose.ymlを用意します。
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
# 6, Dockerコンテナに対するVisual Studio Codeの設定
以下に紹介するリンク先のMicrosoftの学習サイトの内容に沿って設定をします。
[Use a Docker container as a development environment with Visual Studio Code](https://learn.microsoft.com/en-us/training/modules/use-docker-container-dev-env-vs-code/)
手順にしたがうと、以下のファイルが作成されます。
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
# 7, AWS RDSへBastion EC2 インスタンスを設定し、ポートフォワーディングを行って接続するための設定
AWSはVPC・パブリックサブネットと踏み台サーバー・プライベートサブネットとRDSを使った構成です。
1, VPC
CIDRに特にこだわりはなかったので172.31.0.0/16とします。
2, パブリックサブネット
CIDRは172.31.64.0/20としました。
ルートテーブルでVPCのIPアドレスの範囲をローカルに、それ以外をインターネットゲートウェイに振り向けます。
本当はアクセスコントロールリストも設定するべきですが、サボります。
3, 踏み台サーバー
OSはAmazon Linux。インスタンスタイプは最小にしました。
SSH接続をするためのキーペアを作成し、セキュリティグループはインバウンドは22のみとします。
4, プライベートサブネット
CIDRは172.31.80.0/20、ルートテーブルも同じVPC内なので、パブリックサブネットと同じくVPCのIPアドレスの範囲をローカルに設定。
5, RDS
MySQLを選択肢、プライベートサブネットに配置します。
セキュリティグループは、踏み台サーバーに設定したセキュリティグループからの3306へのアクセス許可をインバウンドに、また踏み台サーバーのセキュリティグループへ向けた3306のアウトバウンドルールを設定しました。

ポートフォワードとRDSへの接続は以下のコマンドを使用しました。
ポート番号は必要に応じて変更してください。
```bash
ssh -i my-key.pem -N -L 33066:<RDSエンドポイント>:3306 ec2-user@<Bastion HostのパブリックIP>
mysql -h 127.0.0.1 -P 33066 -u admin -p
```
# 8, Dockerイメージの作成とDockerコンテナを起動
まずはイメージをビルドします。
以下のコマンドはdocker-compose.ymlファイルがあるディレクトリで実行してください。
```bash
docker compose build
```
ビルドしたイメージからコンテナを起動します。
```bash
docker run -d --name containerName -p 8080:8080
```

# 9, 起動したDockerコンテナにVisual Code Studioから接続する
まずは確証機能からDev Containersをインストールします。

コマンドパレットを開きます。
Dev Containers: Reopen in Containerを検索して選択します。
左下に青く接続が表示されれば成功です。
