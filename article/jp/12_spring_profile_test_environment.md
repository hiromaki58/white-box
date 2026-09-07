# はじめに
Spring Boot アプリケーションの CI/CD 環境を構築するにあたり、ローカル環境だけではなく CircleCI 上でも自動テストを実行できるようにしました。
今回実現したい構成は以下です。
```text
ローカル環境
  └─ Gradle Test
       └─ test profile
            └─ H2
CircleCI
  └─ Gradle Test
       └─ ci profile
            └─ MySQL
```
テストコードそのものをローカル用・CircleCI用に分けるのではなく、**Spring Profile を利用してデータベース設定を切り替える**ようにします。

# この記事で紹介する内容
1, なぜテスト環境を分けるのか
2, Spring Profile で設定を切り替える
3, ローカル環境では H2 を使用する
4, CircleCI では MySQL を使用する
5, build.gradle で Profile を切り替える
6, CircleCI では ci Profile を指定する
7, CircleCI で MySQL を起動する
8, MySQL の起動を待ってからテストする
9, CircleCI でテスト結果を確認する
10, CircleCI の設定例
11, 実際に遭遇したエラー
12, 最終的な構成
13, まとめ

# 1. なぜテスト環境を分けるのか
今回のアプリケーションでは、テストの実行時にもデータベースが必要です。
ローカル環境では、簡単に起動・破棄できる H2 を使用します。
一方、CircleCI では本番環境に近い MySQL を Service Container として起動し、テストを実行します。
そのため、
| 実行環境     | Spring Profile | Database |
| -------- | -------------- | -------- |
| ローカル     | `test`         | H2       |
| CircleCI | `ci`           | MySQL    |
という構成にしました。

# 2. Spring Profile で設定を切り替える
Spring Boot では Profile に応じて異なる properties ファイルを読み込むことができます。
今回は以下の2ファイルを用意します。
```text
src/test/resources/
├── application-test.properties
└── application-ci.properties
```
`test` Profile が有効なら、
```text
application-test.properties
```
が使用され、`ci` Profile が有効なら、
```text
application-ci.properties
```
が使用されます。
重要なのは、**Profile がテストコードそのものを切り替えているわけではない**という点です。
同じ `@Test` を実行しながら、Spring が読み込む設定を変更しています。

# 3. ローカル環境では H2 を使用する
ローカルテスト用の `application-test.properties` では H2 を設定します。
例：
```properties
# H2 datasource
spring.datasource.url=jdbc:h2:mem:webgame;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```
これによってローカルで、
```bash
./gradlew test
```
を実行した場合は H2 を使ってテストできます。

# 4. CircleCI では MySQL を使用する
CircleCI 用には `application-ci.properties` を作成します。
今回最終的に動作した設定は次のようなものです。
```properties
# Datasource
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webgame
spring.datasource.username=appuser
spring.datasource.password=apppass

# JPA & Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```
ここでは、
```text
Database : webgame
User     : appuser
Password : apppass
Host     : 127.0.0.1
Port     : 3306
```
として MySQL に接続します。

# 5. build.gradle で Profile を切り替える
ローカルと CircleCI の Profile 切り替えには `build.gradle` の `test` タスクを利用しました。
```groovy
tasks.named('test') {
    useJUnitPlatform()
    systemProperty 'spring.profiles.active',
            System.getenv('SPRING_PROFILES_ACTIVE') ?: 'test'
}
```
重要なのはこの部分です。
```groovy
System.getenv('SPRING_PROFILES_ACTIVE') ?: 'test'
```
これは、
```text
SPRING_PROFILES_ACTIVE が存在する
    ↓
その値を使用

存在しない
    ↓
test を使用
```
という意味になります。
したがって、ローカル環境で環境変数を指定しなければ、
```text
spring.profiles.active=test
```
となります。
結果として、
```text
application-test.properties
```
が読み込まれ、H2 が使用されます。

# 6. CircleCI では ci Profile を指定する
CircleCI の `config.yml` では Gradle/Java コンテナに、
```yaml
SPRING_PROFILES_ACTIVE: ci
```
を指定します。
```yaml
executors:
    java-executor:
        docker:
            # Main container（Gradle + Java）
            -   image: gradle:7.6-jdk17
                environment:
                    SPRING_PROFILES_ACTIVE: ci
                    DB_NAME: webgame
                    DB_USER: ${LOCAL_DB_USER}
                    DB_PASS: ${LOCAL_DB_PASSWORD}
```
CircleCI 上では環境変数が存在するので、`build.gradle` の
```groovy
System.getenv('SPRING_PROFILES_ACTIVE')
```
は、
```text
ci
```
を取得します。
そのため、
```text
spring.profiles.active=ci
```
となり、
```text
application-ci.properties
```
が使用されます。
つまり、
```text
ローカル
SPRING_PROFILES_ACTIVE なし
        ↓
test
        ↓
application-test.properties
        ↓
H2
```
に対して、
```text
CircleCI
SPRING_PROFILES_ACTIVE=ci
        ↓
ci
        ↓
application-ci.properties
        ↓
MySQL
```
という切り替えができます。

# 7. CircleCI で MySQL を起動する
CircleCI では Java/Gradle コンテナとは別に MySQL8.0 の Service Container を起動します。
```yaml
executors:
    java-executor:
        docker:
            # Main container（Gradle + Java）
            -   image: gradle:7.6-jdk17
                environment:
                    SPRING_PROFILES_ACTIVE: ci
                    DB_NAME: webgame
                    DB_USER: ${LOCAL_DB_USER}
                    DB_PASS: ${LOCAL_DB_PASSWORD}

            # Service container（MySQL）
            -   image: mysql:8.0
                environment:
                    MYSQL_DATABASE: webgame
                    MYSQL_USER: appuser
                    MYSQL_PASSWORD: apppass
                    MYSQL_ROOT_PASSWORD: password
                command: >
                    mysqld
                    --character-set-server=utf8mb4
                    --collation-server=utf8mb4_unicode_ci
```
ここで MySQL 側の、
```yaml
MYSQL_DATABASE: webgame
MYSQL_USER: appuser
MYSQL_PASSWORD: apppass
```
と Spring 側の、
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webgame
spring.datasource.username=appuser
spring.datasource.password=apppass
```
を一致させることが重要です。

# 8. MySQL の起動を待ってからテストする
MySQL コンテナを起動しても、すぐに接続可能になるとは限りません。
そのため CircleCI では MySQL が接続可能になるまで待機します。
```yaml
-   run:
        name: Wait for MySQL
        command: |
            for i in `seq 1 20`; do
              mysql -h 127.0.0.1 -P 3306 -u root -p password -e "SELECT 1" && break
              echo "Waiting for MySQL..."
              sleep 3
            done
```
接続に成功した後で Gradle Test を実行します。
```yaml
- run:
    name: Run backend tests
    command: |
        cd web_game
        ./gradlew test
```

# 9. CircleCI でテスト結果を確認する
`./gradlew test` を実行するだけでもテスト自体は実行されます。
しかし、CircleCI の `Tests` タブに結果を表示するには、テスト結果を保存する設定も必要です。
```yaml
- store_test_results:
    path: web_game/build/test-results/test
```
また、Gradle が生成した HTML のテストレポートを CircleCI の Artifacts から確認したい場合は、
```yaml
- store_artifacts:
    path: web_game/build/reports/tests/test
    destination: test-report
    when: always
```
を設定します。
これにより、テスト失敗時にも HTML レポートを確認しやすくなります。

# 10. CircleCI の設定例

今回のテスト部分をまとめると次のようになります。

```yaml
version: 2.1

executors:
    java-executor:
        docker:
            # Main container（Gradle + Java）
            -   image: gradle:7.6-jdk17
                environment:
                    SPRING_PROFILES_ACTIVE: ci
                    DB_NAME: webgame
                    DB_USER: ${LOCAL_DB_USER}
                    DB_PASS: ${LOCAL_DB_PASSWORD}

            # Service container（MySQL）
            -   image: mysql:8.0
                environment:
                    MYSQL_DATABASE: webgame
                    MYSQL_USER: appuser
                    MYSQL_PASSWORD: apppass
                    MYSQL_ROOT_PASSWORD: password
                command: >
                    mysqld
                    --character-set-server=utf8mb4
                    --collation-server=utf8mb4_unicode_ci

        working_directory: ~/project

jobs:
    test-backend:
        executor: java-executor
        steps:
            - checkout
            # Waiting for MySQL
            -   run:
                    name: Wait for MySQL
                    command: |
                        for i in `seq 1 20`; do
                          mysql -h 127.0.0.1 -P 3306 -u root -p password -e "SELECT 1" && break
                          echo "Waiting for MySQL..."
                          sleep 3
                        done

            # Move to backend directory
            -   run:
                    name: Run backend tests
                    command: |
                        cd web_game
                        ./gradlew test
            - store_test_results:
                  path: build/test-results/test
            -   store_artifacts:
                    path: web_game/build/reports/tests/test
                    destination: test-report
                    when: always
```

# 11. 実際に遭遇したエラー
## application-ci.properties が存在しない
最初はテスト対象の GitHub ブランチに、
```text
application-ci.properties
```
が存在していませんでした。
CircleCI では、
```yaml
SPRING_PROFILES_ACTIVE: ci
```
を指定しているため、CI 用設定ファイルもリポジトリに含める必要があります。

## JDBC URL の環境変数が解決されない
`application-ci.properties` を、
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
```
としていたところ、
```text
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl,
${SPRING_DATASOURCE_URL}
```
というエラーが発生しました。
`SPRING_DATASOURCE_URL` が実際の JDBC URL に解決されていなかったことが原因でした。
最終的には CI 専用設定として、
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webgame
```
と明示しました。

## MySQL の認証エラー
次に、
```text
Access denied for user 'root'@'127.0.0.1'
```
というエラーが発生しました。
Spring と MySQL で使用するユーザーを揃え、
```text
appuser / apppass
```
をテスト用ユーザーとして使用しました。

## Database 名が想定と違う
さらに、
```text
Access denied for user 'appuser'@'%' to database 'web_game'
```
というエラーも発生しました。
設定ファイル上では、
```text
webgame
```
を使用しているにもかかわらず `web_game` に接続していました。
原因は CircleCI に登録されていた、
```text
SPRING_DATASOURCE_URL
```
が `application-ci.properties` の設定を上書きしていたことでした。
Spring Boot では環境変数によって properties の設定が上書きされる場合があるため、**設定ファイルだけではなく CI/CD 側の Environment Variables も確認する必要があります。**
不要になった `SPRING_DATASOURCE_URL` を CircleCI 側から削除することで解決しました。

# 12. 最終的な構成
最終的には以下の構成になりました。
```text
                    ./gradlew test
                          │
              ┌───────────┴───────────┐
              │                       │
           Local                   CircleCI
              │                       │
 SPRING_PROFILES_ACTIVEなし   SPRING_PROFILES_ACTIVE=ci
              │                       │
           test                      ci
              │                       │
 application-test.properties  application-ci.properties
              │                       │
             H2                     MySQL
```
テストコードそのものは同じです。
環境によって Spring Profile を変更し、その Profile に対応する datasource 設定を読み込むことで、
* ローカルでは H2
* CircleCI では MySQL
というテスト環境を構築できました。

# 12. まとめ
今回のポイントは、**「テストコードを環境ごとに分ける」のではなく、「テスト時に使用する設定を Profile で切り替える」**ことです。
`build.gradle` では、
```groovy
System.getenv('SPRING_PROFILES_ACTIVE') ?: 'test'
```
とすることで、環境変数がなければローカル用の `test` Profile、CircleCI では `SPRING_PROFILES_ACTIVE=ci` によって `ci` Profile を使用できます。
その結果、同じ `@Test` を使いながら、
```text
Local    → H2
CircleCI → MySQL
```
というテスト環境を実現できました。

また、CircleCI 上で問題が発生した場合は、
1. どの Profile が有効なのか
2. どの `application-xxx.properties` が読み込まれているか
3. JDBC URL・DB名・ユーザー名・パスワードが MySQL 側と一致しているか
4. CircleCI の Environment Variables が properties の値を上書きしていないか
を確認することが、トラブルシューティングの重要なポイントになりました。
