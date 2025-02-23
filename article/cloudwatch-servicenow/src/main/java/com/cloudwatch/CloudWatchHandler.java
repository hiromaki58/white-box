package com.cloudwatch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.util.logging.Logger;

public class CloudWatchHandler
{
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
}
