# はじめに
Webアプリケーションの依存関係を一つのパッケージにカプセル化し、自動スケーリングに対応するためDockerコンテナを作成します。
またmavenプロジェクトとして構築していたので、それをgradleへ切り替えたときの方法も追加しています。
#### 作業全体の流れ
1, Docker Desktopのインストールと環境変数の設定
2, Backend用Dockerfileファイルの作成
3, Frontend用Dockerfileファイルの作成
4, docker-compose.ymlファイルの作成
5, 実行したコマンドとエラーが発生した場合の対応方法
6, mavenからgradleへの変更
7, AWS RDSへのBastionを使った接続方法
#### 動作環境
バックエンドはJavaを、フロントエンドではReact + TypeScriptを使いました。
プロジェクトのルートディレクトリにdocker-compose.ymlファイルを配置し、フロントエンドとバックエンドのサービスを一緒に管理します。
このWebアプリケーションは最終的にAWSで構築するためイメージが接続するDBはAWS RDSへBastionを使って接続します。
#### Directory Structure
root
├─ frontend
│   └─ react-typescript
│       └─ Dockerfile
├─ backend
│   └─ Dockerfile
└─ docker-compose.yml
# 2, Backend用Dockerfileファイルの作成
Dockerイメージの作成に必要な設定ファイルを作成します。
以下の内容はmavenからgradleへ切り替えたあとの設定ファイルです。
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
FROM openjdk:23

# Copy the built JAR file from the build stage
COPY --from=build /home/app/build/libs/*.jar /usr/local/lib/XXX-XXX.jar

# Specify the port the application uses
EXPOSE 8080

# Command to run the application when the container starts
ENTRYPOINT ["java","-jar","/usr/local/lib/XXX-XXX.jar"]
```
# 3, Frontend用Dockerfileファイルの作成
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
# 4, docker-compose.ymlファイルの作成
フロントとバックエンドを一緒に管理します。
```yml:docker-compose.yml
version: '3.8'

services:
    frontend:
        build:
            context: ./frontend/react-typescript
        ports:
            - '3000:3000'
        volumes:
            - ./frontend/react-typescript:/app
            - /app/node_modules
        environment:
            - NODE_ENV=development
        stdin_open: true
        tty: true

    backend:
        build: ./backend
        volumes:
            - ./backend:/app
        ports:
            - '8080:8080'
        environment:
            - SPRING_PROFILES_ACTIVE=development
        command: ['./mvnw', 'spring-boot:run']

volumes:
    frontend_node_modules:
    backend_maven:
```
# 5, 実行したコマンドとエラーが発生した場合の対応方法
Dockerfileとdocker-compose.ymlを作成したあとで、docker compose up --buildを実行しますが、このとき以下のエラーが発生しました。

=> ERROR [backend internal] load metadata for docker.io/library/openjdk:23であれば
docker pull openjdk:23を使用すればDockerイメージのメタデータをロードすることができます。

この方法では解決できない問題がありました。
 => ERROR [backend internal] load metadata for docker.io/library/maven:3.8.4-openjdk-23

これまでjavaのバージョンが低かったこともあり、ずっとmavenをプロジェクト管理に使っていました。
バージョンを23に変えたところ、それに対応するmaven:3.8.4-openjdk-23がDocker Hubに存在しなかったのです。

そのため私はmavenをあきらめ、gradleへ切り替えることにしました。
# 6, mavenからgradleへの変更
バックエンド側のDockerfileをgradle用に変更し、以下のコマンドを実行したました。
```bash
gradle clean build
```
# 7, AWS RDSへのBastionを使った接続方法
