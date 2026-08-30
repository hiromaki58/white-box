package com.cloudwatch;

import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class ServiceNowClient {
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
}
