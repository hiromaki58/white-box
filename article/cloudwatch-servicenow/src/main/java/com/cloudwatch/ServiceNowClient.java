package com.cloudwatch;

import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

public class ServiceNowClient {
    private static final Logger logger = Logger.getLogger(ServiceNowClient.class.getName());

    // ServiceNow API のエンドポイント (your-instance は自身のインスタンスに変更)
    private static final String SERVICENOW_URL = "https://your-instance.service-now.com/api/aws/cloudwatch/create_incident";

    // 認証情報
    private static final String SERVICENOW_USER = System.getenv("SERVICENOW_USER");
    private static final String SERVICENOW_PASSWORD = System.getenv("SERVICENOW_PASSWORD");

    private final OkHttpClient client;

    public ServiceNowClient() {
        this.client = new OkHttpClient();
    }

    public String createIncident(String shortDescription, String details) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // インシデント作成のリクエストボディ
        String json = String.format("{\"short_description\": \"%s\", \"description\": \"%s\"}", shortDescription, details);
        RequestBody body = RequestBody.create(json, JSON);

        // Basic 認証のヘッダー
        String credentials = SERVICENOW_USER + ":" + SERVICENOW_PASSWORD;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // HTTP リクエストの作成
        Request request = new Request.Builder()
                .url(SERVICENOW_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .build();

        // リクエストの実行
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.warning("Failed to create incident in ServiceNow: " + response.body().string());
                return "Error: " + response.body().string();
            }
            logger.info("Incident created successfully in ServiceNow.");
            return response.body().string();
        } catch (IOException e) {
            logger.severe("Error calling ServiceNow API: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
