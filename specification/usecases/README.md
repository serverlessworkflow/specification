# Use Cases

Use cases for the Serverless Workflow Specification highly depend on the reference implementations
and the ecosystem available during workflow execution (available functions/services/events, etc).

As mentioned in the [main specification document](../README.md) one of the main benefits of Serverless Workflows
is that they provide clear separation of business and orchestration logic in your serverless apps.

Developers can focus on solving business logic inside functions and utilize workflows to define function invocations,
 react to events, as well as provide data management for different microservices.

So what can you automate with Serverless Workflows? You can get some ideas from the use cases below.

## Table of Contents

- [Online Vehicle Auction](#Online-Vehicle-Auction)
- [Payment Processing](#Payment-Processing)
- [Data Analysis](#Data-Analysis)
- [Error Notifications](#Error-Notifications)
- [Continuous Integration And Deployment](#Continuous-Integration-And-Deployment)

## Online Vehicle Auction

You can use Serverless Workflows to coordinate all of the steps of an Online Vehicle Auction.
These can include:

- Authentication of users making bids.
- Communication with Bidding and Inventory services
- Make decisions to start/end the auction under certain conditions

![Diagram showing the vehicle auction workflow](../media/usecases/usecase-vehicle-auction.png)

## Payment Processing

Servlerless Workflows are ideal for coordinating session-based apps such as e-commerce sites. You can
use Serverless Workflows to coordinate all steps of the checkout process allowing for example users to take a picture
of their credit card rather than having to type in the numbers and information.

![Diagram showing the payment processing workflow](../media/usecases/usecase-app-payment.png)

## Data Analysis

You can use Serverless Workflows to coordinate data analysis of Marketing and Sales information.
Analysis can be scheduled on a timely basis to trigger workflow coordination of different ETL services.

![Diagram showing the data analysis workflow](../media/usecases/usecase-data-analysis.png)

## Error Notifications

You can design Serverless Workflows that trigger notifications regarding their success or failure.
In conjunction with available messaging services you can notify developers on different platforms of such possible failures
 including error information and exactly the point in the execution the failure happened.
 At the same time you can log the workflow execution status to cloud storage services for further analysis.

![Diagram showing a workflow with error notifications](../media/usecases/usecase-error-notifications.png)

## Continuous Integration And Deployment

Serverless Workflows can help you build solid continuous integration and deployment solutions.
Code check-ins can trigger website builds and automatic redeploys. Pull requests can trigger
running automated tests to make sure code is well-tested before human reviews.

![Diagram showing the continuous integration workflow](../media/usecases/usecase-continuous-integration.png)
