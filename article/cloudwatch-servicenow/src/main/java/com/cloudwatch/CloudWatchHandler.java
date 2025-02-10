package com.cloudwatch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import okhttp3.*;

import java.io.IOException;
import java.util.logging.Logger;

public class CloudWatchHandler
{
    private static final Logger logger = Logger.getLogger(CloudWatchHandler.class.getName());
    private static final String SERVICENOW_URL = "https://your-instance.service-now.com/api/aws/cloudwatch/create_incident";
    private static final String SERVICENOW_USER = "admin";
    private static final String SERVICENOW_PASSWORD = "your_password";

    public void handleRequest(SNSEvent event, Context context) {
        event.getRecords().forEach(record -> {
            String message = record.getSNS().getMessage();
            logger.info("Received CloudWatch Alert: " + message);

            // ServiceNow にインシデントを作成
            createIncidentInServiceNow("AWS CloudWatch Alert", message);
        });
    }

    private void createIncidentInServiceNow(String shortDescription, String details) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String json = "{\"short_description\": \"" + shortDescription + "\", " +
                      "\"description\": \"" + details + "\"}";

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(SERVICENOW_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", Credentials.basic(SERVICENOW_USER, SERVICENOW_PASSWORD))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.warning("Failed to create incident in ServiceNow: " + response.body().string());
            } else {
                logger.info("Incident created successfully in ServiceNow.");
            }
        } catch (IOException e) {
            logger.severe("Error calling ServiceNow API: " + e.getMessage());
        }
    }
}
