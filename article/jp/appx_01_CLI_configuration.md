# はじめに
[前回]("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-3-set-up-cd-portion-of-the-ci-cd-pipeline-48f06fea00b7" 2, 3, Set up CD portion of the CI/CD Pipeline)の記事の中でAWS CLIを使っていろいろな設定を行いました。
今回はAWS CLIの設定方法を解説したいと思います。
# この記事で実施する内容
まずはCI/CD全体の具体的な流れは以下になります。
①Homebrew を使ってローカルに AWS CLI をインストール
   ↓
②IAM ユーザーの作成
   ↓
③アクセスキーの取得
   ↓
④ローカルでの設定
   ↓
⑤CLI から AWS に接続
#### 作業全体の流れ
1, AWS CLIが便利なのでローカル環境でも使えるようにします
2, CircleCI で Docker イメージを自動ビルドするために.circleci/config.ymlファイルを変更
3, Docker をインストール
4, イメージをビルドしたい EC2 に ECS エージェントをインストール
5, ローカルCLIから ECR レポジトリを作成
6, ECS クラスターとサービスの作成
7, CircleCI からデプロイ
#### 動作環境
今回はCircle CIとAWSのみです。
# 1, Homebrew を使ってローカルに AWS CLI をインストール
macユーザー限定にはなってしまいますが、以下のコマンドをローカルで実行してインストールを行ってください。
```bash
brew install awscli
```
終わったらバージョンの確認で、インストールが無事に終わったことを確認しましょう。
```bash
aws --version
```
# 2, IAM ユーザーの作成
ローカルから AWS 環境に接続するためにはアクセスキーとシークレットアクセスキーをローカル環境に設定してあげなければなりません。

# 3, アクセスキーの取得

# 4, ローカルでの設定

# 5, CLI から AWS に接続
