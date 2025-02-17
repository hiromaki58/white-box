# はじめに
AWSで発生するアラートをServiceNowを使って管理しているのですが、チケット作成を自動化できないかと思ってやってみました。
#### 全体の動き
1, CloudWatchで監視する対象とアラートを設定
2, アラートが発生するとSNSを経由してLambdaを起動
3, LambdaがServiceNowのAPIをコール
4, ServiceNowがチケットを作成する
#### 動作環境
LambdaコードはJavaを、ServiceNowではJavaScriptを使いました。
# CloudWatchで監視する対象とアラートを設定
1, 監視対象を設定
![01-aws-setTargetMetric.png](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/521759/2e525637-f611-4500-8eb6-0eb995a5a3eb.png)
2, SNS用にトピックを設定
![02-aws-setNotificationType.png](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
# アラートが発生するとSNSを経由してLambdaを起動
1, SNSを準備
![03-aws-setSns.png](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
2, Labmdaを作成
![04-aws-setLambdaFunc](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
3, SNSをLambdaのトリガーに設定
![05-aws-setLambdaTrigger](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
# LambdaがServiceNowのAPIをコール
1, ServiceNowでAPIを準備
![06-SN-setScriptedRestApi](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
2, チケットを起票するコードを作成
![07-SN-setApiScript](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
```javascript:ServiceNowCode.js
(function processRequest(request, response) {
    var requestBody = request.body.data;

    // createt incident
    var incident = new GlideRecord('incident');
    incident.initialize();
    incident.short_description = requestBody.short_description;
    incident.description = requestBody.description;
    incident.category = 'AWS';
    incident.impact = '2';
    incident.urgency = '2';
    incident.insert();

    // return the response
    response.setStatus(201);
    response.setBody({ message: "Incident created", sys_id: incident.sys_id });
})(request, response);
```
3, 作成したServiceNow APIをLambdaのコードに設定
![08-SN-apiPath](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
4, Lambda用のコードを作成
```java:ServiceNowClient.java
private static final Logger logger = Logger.getLogger(ServiceNowClient.class.getName());

private String serviceNowUrl;

private final OkHttpClient client;

public ServiceNowClient() {
    Properties properties = new Properties();

    try(FileInputStream fis = new FileInputStream("src/main/resources/config.properties")){
        properties.load(fis);
        this.serviceNowUrl = properties.getProperty("SERVICENOW_URL");
    }
    catch(IOException e) {
        throw new RuntimeException("Configraton file is not found.");
    }

    this.client = new OkHttpClient();
}

public String createIncident(String shortDescription, String details) {
    MediaType contentType = MediaType.parse("application/json; charset=utf-8");

    // Request body
    String json = String.format("{\"short_description\": \"%s\", \"description\": \"%s\"}", shortDescription, details);
    RequestBody body = RequestBody.create(json, contentType);

    // Create HTTP request
    Request request = new Request.Builder()
            .url(serviceNowUrl)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

    // Excecute the request
    try(Response response = client.newCall(request).execute()){
        if(!response.isSuccessful()){
            logger.warning("Failed to create incident in ServiceNow: " + response.body().string());
            return "Error: " + response.body().string();
        }

        logger.info("Incident created successfully in ServiceNow.");
        return response.body().string();
    }
    catch (IOException e) {
        logger.severe("Error calling ServiceNow API: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}
```
```java:CloudWatchHandler.java
private static final Logger logger = Logger.getLogger(CloudWatchHandler.class.getName());
private final ServiceNowClient serviceNowClient = new ServiceNowClient();

public void handleRequest(SNSEvent event, Context context) {
    event.getRecords().forEach(record -> {
        String msg = record.getSNS().getMessage();
        logger.info("Received CloudWatch Alert: " + msg);

        String res = serviceNowClient.createIncident("AWS CloudWatch Alert", msg);
        logger.info("ServiceNow Response: " + res);
    });
}
```
5, 作成したコードをjarとしてLambdaにアップロード
![09-aws-uploadJarFile](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
6, 作成したコードを呼び出すためのハンドラーを設定
![10-aws-setHandler](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)
6, 作成したコードを呼び出すためのハンドラーを設定
# ServiceNowがチケットを作成する
あとはアラートが発生するとチケットが作成される
![14-SN-incidentTicket](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com)