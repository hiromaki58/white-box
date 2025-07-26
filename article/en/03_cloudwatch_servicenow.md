# Introduction
I attempted to automate ticket creation using ServiceNow for AWS alerts.
#### Overall Workflow
1, Configure CloudWatch for monitoring and alerts.
2, Trigger a Lambda function via SNS upon an alert.
3, Lambda calls the ServiceNow API.
4, ServiceNow creates a ticket.
#### Environment
Lambda: Java
ServiceNow: JavaScript
# Configuring CloudWatch and Alerts
1, Set up monitoring targets.

2, Create an SNS topic for alerts.

# Triggering Lambda via SNS
1, Configure SNS

2, Create the Lambda function

3, Set SNS as a trigger for Lambda.

# Lambda Calls ServiceNow API
1, Prepare the ServiceNow API

2, Develop the ticket creation logic

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
3, Integrate the ServiceNow API into the Lambda code

4, Write the Lambda code
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
5, Package the code as a JAR and upload it to Lambda

6, Set the handler to execute the uploaded code

# ServiceNow Creates a Ticket
Once an alert occurs, a ticket is automatically created.
