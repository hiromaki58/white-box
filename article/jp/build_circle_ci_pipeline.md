# はじめに
[前回]("https://medium.com/p/5bc8e6e3b551" 1, Set up the development environment using Docker.)はDockerを使った開発環境を構築しました。
今回も環境構築の一環としてCI/CDパイプラインをCircle CIを使って作成したいと思います。
# この記事で実施する内容
Spring Bootのapplication-test.propertiesファイルの設定変更。
Circle CIとGitHubの連携。
.circleci/config.ymlファイルの作成。
#### 作業全体の流れ
1, Circle CIとGitHubの連携
2, Spring Bootのapplication-test.propertiesファイルの設定変更
3, .circleci/config.ymlファイルの作成
4, build.gradleへの設定追加
5, テストコード追加
6, 最後に
#### 動作環境
バックエンドはSpring Boot。
テストに用いるDBは本番用にAWS RDS、CIではH2(in-memory)。
Circle CIを使い、ビルドにはgradle。
# 1, Circle CIとGitHubの連携
まずはCircleCIのダッシュボードに移動し、左メニューの「Projects」をクリックします。
GitHubリポジトリ一覧が表示されたら、対象リポジトリの右側にある「Set Up Project」をクリック。
使用する言語やビルド設定（テンプレート）を選択したのち、.circleci/config.yml をリポジトリに追加してプッシュします。
# 2, Spring Bootのapplication-test.propertiesファイルの設定変更
```properties:application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```
# 3, .circleci/config.ymlファイルの作成
```yml:config.yml
version: 2.1

executors:
  java-executor:
    docker:
      - image: gradle:8.13-jdk23
    working_directory: ~/project

jobs:
  test-java:
    executor: java-executor
    steps:
      - checkout:
          path: ~/project

      - run:
          name: Make gradlew executable
          command: chmod +x ./web_game/gradlew

      - run:
          name: Run tests
          command: |
            ./web_game/gradlew test \
              --project-dir ./web_game \
              -i \

      - store_test_results:
          path: web_game/build/test-results

      - store_artifacts:
          path: web_game/build/reports

workflows:
  build-and-test:
    jobs:
      - test-java
```
# 4, build.gradleへの設定追加
```gradle:build.gradle
dependencies {
    // Spring Boot Dependencies
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-mail'

    // Testing Dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.h2database:h2'
}
```
# 5, テストコード追加
```java:PlayerControllerTest.java
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    public void setUp() {
        playerRepository.deleteAll(); // clear the existing data

        PlayerModel player = new PlayerModel();
        player.setFirstName("John");
        player.setFamilyName("Doe");
        player.setEmailAddr("test@example.com");
        player.setPassword("password123");
        playerRepository.save(player);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/player/login")
                .with(csrf())
                .content("{\"emailAddr\": \"test@example.com\", \"password\": \"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loginTry").value(true))
                .andExpect(jsonPath("$.msg").value("Succeed to login"));
    }
}
```
# 6, 最後に
リモートレポジトリにプッシュをするごとに、テストが実行されます。