# Introduction
In the [previous article]("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-3-set-up-cd-portion-of-the-ci-cd-pipeline-48f06fea00b7" 3, Set up CD portion of the CI/CD Pipeline)we used the AWS CLI to configure various settings.
In this article, I’ll explain how to set up the AWS CLI itself.
# What This Article Covers
Here is the overall flow of the CI/CD setup process:

1, Install the AWS CLI locally using Homebrew
2, Create an IAM user
3, Obtain access keys
4, Configure AWS CLI on your local machine
5, Connect to AWS via the CLI
# 1, Install the AWS CLI locally using Homebrew
This step is limited to Mac users, but please run the following command locally to install the AWS CLI.
```bash
brew install awscli
```
Once completed, check the version to confirm that the installation was successful.
```bash
aws --version
```
# 2, Create an IAM user
To connect from your local machine to the AWS environment, you need to configure your access key and secret access key locally.
If you haven’t set up a usable user yet, go ahead and create one.

Go to the AWS IAM page and click the Create User button.
Enter an appropriate username and proceed to the next step.

For Set permissions, since this is for development purposes, choose Attach policies directly and select AdministratorAccess.

Tags are optional, but adding consistent tags for each project across your AWS services makes future management easier.
This is especially useful when you want to clean up services related to completed projects.

During the creation process, you may see a message like the following:
```
Provide user access to the AWS Management Console - optional
If you're providing console access to a person, it's a best practice to manage their access in IAM Identity Center.
```
This is intended to limit access to browser-based login only, so in our case, uncheck this option.

Also, if you see a message like:
```
Specify a user in Identity Center - Recommended
We recommend that you use Identity Center to provide console access to a person. With Identity Center, you can centrally manage user access to their AWS accounts and cloud applications.

I want to create an IAM user
We recommend that you create IAM users only if you need to enable programmatic access through access keys, service-specific credentials for AWS CodeCommit or Amazon Keyspaces, or a backup credential for emergency account access.
```
Please select "I want to create an IAM user."
# 3, Obtain access keys
Next, obtain the Access Key and Secret Access Key from the user you just created.

Choose CLI as the use case, and set any appropriate tags — this will generate the access key.
Please note that the Secret Access Key is only displayed at this time, so make sure to store it securely.
# 4, Configure AWS CLI on your local machine
Then, configure your local environment with the access key information.
```bash
aws configure
```
```bash
AWS Access Key ID [None]: xxxxxxxxxxxxxxxx
AWS Secret Access Key [None]: abcdefghijklmnopqrstuvwxyz1234567890
Default region name [None]: us-east-1
Default output format [None]: json
```
# 5, Connect to AWS via the CLI
```bash
aws ec2 describe-instances
```
Use a command like the one above to test the connection and ensure everything is working correctly.
