# Introduction
When I used Amazon EKS with my personal account, it cost me $190 USD in just 10 days.
In this article, I would like to explain the key cost factors involved when using EKS.
# What This Article Covers
1, The actual cost incurred by EKS
2, Why EKS is so expensive？
# 1, The Cost Incurred by EKS
This time, I used EKS under my personal account to build a test environment. I started using it on January 4th of this year.
After doing various tasks over the weekend, I checked Billing and Cost Management on Monday. The Total forecasted cost for the current month showed $50, and I thought, “Well, that sounds about right.”
However, after leaving the environment running for another 10 days, I had a bad feeling and decided to check again. At that point, the Month-to-date cost had already reached $190, and the projected cost by the end of the month was $2,500.
I immediately stopped and deleted the related services, but the payment for this month increased by +2800% compared to the previous month. It felt like the inflation rate of a country experiencing economic collapse.
# 2, Why Is EKS So Expensive?
One reason is that EKS itself is not cheap — it costs $0.10 per hour. However, the bigger issue is that EKS alone cannot accomplish much. To make it meaningful, you must combine it with various other AWS services.
Kubernetes helps avoid application failures and simplifies microservices configuration. However, when you consider what is required to run EKS, you inevitably need to use the following AWS services:
- EC2 instances managed by EKS
- Network configuration:
    - VPC
    - Load balancer
    - Subnet
    - Route table
    - Security group
    - Access control list
    - NAT Gateway
    - Elastic IP
- EKS itself
- ECR (To store container images used by Kubernetes)
- RDS
  Of course, some services have free tiers, and you can adjust the size and cost of EC2 and RDS. However, if you configure the system in a way that can withstand real-world usage, it will not be cheap.
  Additionally, some services such as NAT Gateway cost $0.045 per hour, but they also charge separately for data processing per GB.

No wonder that mainly large enterprises with sufficient budgets use EKS, while many customers start with ECS to achieve redundancy first.
It was an expensive lesson.
