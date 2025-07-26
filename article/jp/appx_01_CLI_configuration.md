# はじめに
[前回]("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-3-set-up-cd-portion-of-the-ci-cd-pipeline-48f06fea00b7" 3, Set up CD portion of the CI/CD Pipeline)の記事の中でAWS CLIを使っていろいろな設定を行いました。
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
もし使えるユーザーがまだ設定されていなければ、作成してあげましょう。
AWS IAM ページに遷移して、Create User ボタンを押下。
適当な名前を設定し、次のページへ。

Set permissions では開発用なのでAttach policies directly から AdministratorAccess を選択します。
タグはあってもなくてもかまいませんが、プロジェクトごとに統一したタグを各 AWS サービスに設定して置くと、あとあと管理が簡単になります。
終了したプロジェクトに関係したサービスを停止する時に便利です。

作成時に以下のようなメッセージが表示されることがあります。
```
Provide user access to the AWS Management Console - optional
If you're providing console access to a person, it's a best practice to manage their access in IAM Identity Center.
```
これはブラウザからのアクセスのみに限定するため、今回はチェックを外してください。

また以下のようなメッセージが表示された場合は
```
Specify a user in Identity Center - Recommended
We recommend that you use Identity Center to provide console access to a person. With Identity Center, you can centrally manage user access to their AWS accounts and cloud applications.

I want to create an IAM user
We recommend that you create IAM users only if you need to enable programmatic access through access keys, service-specific credentials for AWS CodeCommit or Amazon Keyspaces, or a backup credential for emergency account access.
```
"I want to create an IAM user"を選択してください。
# 3, アクセスキーの取得
次に作成したユーザーからアクセスキーとシークレットアクセスキーを取得します。

CLI を選択して、適当なタグを設定すれば、アクセスキーが作成されます。
シークレットアクセスキーはこの時にしか表示されないので、注意してください。
# 4, ローカルでの設定
次にアクセスキーの内容をローカル環境に設定します。
```bash
aws configure
```
```bash
AWS Access Key ID [None]: xxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: abcdefghijklmnopqrstuvwxyz1234567890
Default region name [None]: us-east-1
Default output format [None]: json
```
# 5, CLI から AWS に接続
```bash
aws ec2 describe-instances
```
上記のようなコマンドを使ってみて接続を確認してください。
