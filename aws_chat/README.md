# Creating a Simple Chat Web App with AWS AppSync + Amazon DynamoDB + React
## AWS architecture

- Front-end development framework is React 18  
- WEB hosting with Amazon S3 + Amazon CloudFront  
- App login authentication with Amazon Cognito user pool  
- Chat data stored in Amazon DynamoDB  
- AppSync mediates between front-end UI and chat data  

## Chat function

- Since it is a chat application, users share messages with each other.  
- However, it is shared only between users who have the same "service ID".  
- When another user with the same "service ID" writes a message, his/her chat screen is automatically refreshed and the message written by others is immediately displayed, utilizing the AWS AppSync push notification function (Subscription).  
- The only function provided, other than "Read", is "Write". Once a message is written, it cannot be modified or deleted.  
- The number of messages displayed is the latest 30.  

## Amazon DynamoDB Design

| serviceid      | datetimeuser                           | datetime                         |message       |user       |
|----------------|----------------------------------------|----------------------------------|--------------|-----------|
| AAA            | 2024-06-15T07:36:38.175Z#user01        | 2024-06-15T07:36:38.175Z         | XXXX         | user001   |
| AAA            | 2024-06-15T07:37:28.495Z#user02        | 2024-06-15T07:37:28.495Z         | YYYY         | user002   |
| BBB            | 2024-06-15T07:38:18.005Z#user03        | 2024-06-15T07:38:18.005Z         | ZZZZ         | user003   |
- To retrieve data by service ID, use the service ID as a partition key.  
- To sort data by write date/time, but because data will not be unique if multiple users write at exactly the same time, use the write date/time with user names appended to it, separated by #, as the sort key.  
- The date/time data is in ISO8601 format, which is easy to handle in JavaScript.  

## React code

### `App.js`

In the above code, the integration settings with Amazon Cognito are also listed in Auth:. Since my environment has Amazon Cognito app authentication, and I am using Amazon Cognito to authenticate access to AWS AppSync, I set aws_appsync_authenticationType to a fixed value of "AMAZON _COGNITO_USER_POOLS" and aws_appsync_apiKey is fixed to "null" since we do not use API keys.  

The integration with Amazon Cognito is also configured in Amplify.configure. If you do that as a set, the authentication settings for AWS AppSync will be automatically handled behind the scenes.  
