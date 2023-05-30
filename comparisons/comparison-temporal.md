# Comparisons - Temporal

[Temporal](https://temporal.io/) is an open source microservice orchestration platform. Temporal apps are written
in code, and SDKs are currently available for Go, Java, PHP and TypeScript. It provides two special functions, namely Workflow
and Activity functions.

Workflows in Temporal are cohesive functions with added support for retries, Saga pattern support
rollbacks, and human intervention steps in case of failure. Overall Temporal promotes the "Workflows as Code"
paradigm which might feel natural to developers. Workflows in Temporal cannot call external APIs directly, but
rather orchestrate executions of Activities.

Activities are object methods written in one of the supported languages. They can contain any code without restrictions,
meaning they can be used to communicate with databases, call external APIs, etc.

The purpose of this document is to compare and contrast the Temporal workflow code and the equivalent
Serverless Workflow DSL.
This can hopefully help compare and contrast the two workflow languages and give a better understanding of both.

Given that Temporal provides SDKs in multiple languages, in this document we will focus only on Temporal workflows
written in Java.

All Temporal examples used in this document are available in their [samples-java](https://github.com/temporalio/samples-java)
Github repository. Note that in this document we only show the Temporal workflow Java code which is relevant to
the actual workflow implementation. Language constructs like imports are not included, but full examples can be found
in the repo mentioned above. The latest version of Temporal as of the time of writing this
document is [1.13.1](https://github.com/temporalio/temporal/releases/tag/v1.13.1).

We hope that these examples will give you a good start for comparing and contrasting the two workflow
languages.

## Note when reading provided examples

[Activities](https://docs.temporal.io/docs/concept-activities) in temporal are comparable with
[actions](https://github.com/serverlessworkflow/specification/blob/master/specification.md#Action-Definition) in Serverless Workflow
language, namely actions that reference [function](https://github.com/serverlessworkflow/specification/blob/master/specification.md#function-definition)
definitions. Serverless Workflow action execution involves invoking distributed functions via REST or via events.

When looking at provided examples below, please note that the code defined in activities of
Temporal code is assumed to be stand-alone distributed functions/services accessible over REST API
and used in the compared Serverless Workflow DSL.


## Table of Contents

- [Single Activity](#Single-Activity)
- [Periodical Execution (Cron)](#Periodical-Execution)
- [Compensation Logic (SAGA)](#Compensation-Logic)
- [Error Handling and Retries](#Error-Handling-and-Retries)

### Single Activity

[Full Temporal Example](https://github.com/temporalio/samples-java/blob/master/src/main/java/io/temporal/samples/hello/HelloActivity.java)

<table>
<tr>
    <th>Temporal</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
// Workflow implementation
public static class GreetingWorkflowImpl implements GreetingWorkflow {
    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(2)).build());

    // Workflow method
    @Override
    public String getGreeting(String name) {
        return activities.composeGreeting("Hello", name);
    }
}
```

</td>
<td valign="top">

```json
{
  "id": "greetingworkflow",
  "name": "Greeting Workflow",
  "version": "1.0.0",
  "specVersion": "0.8",
  "autoRetries": true,
  "states": [
    {
      "name": "Greet",
      "type": "operation",
      "actions": [
        {
          "name": "Greet Action",
          "functionRef": {
            "refName": "GreetingFunction",
            "arguments": {
               "prefix": "Hello",
               "name": "${ .name }"
            }
          }
        }
      ],
      "timeouts": {
          "actionExecTimeout": "PT2S"
      },
      "end": true
    }
  ],
  "functions": [
    {
      "name": "GreetingFunction",
      "operation": "myactionsapi.json#composeGreeting"
    }
  ]
}
```

</td>
</tr>
</table>

### Periodical Execution

[Full Temporal Example](https://github.com/temporalio/samples-java/blob/master/src/main/java/io/temporal/samples/hello/HelloCron.java)

<table>
<tr>
    <th>Temporal</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
// Workflow implementation
public static class GreetingWorkflowImpl implements GreetingWorkflow {
    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(10)).build());

    @Override
    public String greet(String name) {
        activities.greet("Hello " + name + "!");
    }
}

// Client code Workflow Options (cron)
WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
                .setWorkflowId(CRON_WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setCronSchedule("* * * * *")
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build();

```

</td>
<td valign="top">

```json
{
  "id": "greetingworkflow",
  "name": "Greeting Workflow",
  "version": "1.0.0",
  "specVersion": "0.8",
  "autoRetries": true,
  "timeouts": {
    "workflowExecTimeout": "PT10M"
  }
  "start": {
    "stateName": "GreetingState",
    "schedule": {
      "cron": {
        "expression": "* * * * *"
      }
    }
  },
  "states": [
    {
      "name": "GreetingState",
      "type": "operation",
      "actions": [
        {
          "name": "Greet",
          "functionRef": {
            "refName": "GreetingFunction",
            "arguments": {
              "prefix": "Hello",
              "name": "${ .name }"
            }
          }
        }
      ],
      "timeouts": {
          "actionExecTimeout": "PT2S"
      },
      "end": true
    }
  ],
  "functions": [
    {
      "name": "GreetingFunction",
      "operation": "myactionsapi.json#greet"
    }
  ]
}
```

</td>
</tr>
</table>

### Compensation Logic

[Full Temporal Example](https://github.com/temporalio/samples-java/blob/8218f4114e52417f8d04175b67027ff0af4fb73c/src/main/java/io/temporal/samples/hello/HelloSaga.java)

<table>
<tr>
    <th>Temporal</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
// Workflow implementation
    public static class SagaWorkflowImpl implements SagaWorkflow {
    ActivityOperation activity =
            Workflow.newActivityStub(
                    ActivityOperation.class,
                    ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(2)).build());

    // Workflow Method
    @Override
    public void execute() {
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        try {
            // The following demonstrate how to compensate sync invocations.
            ChildWorkflowOperation op1 = Workflow.newChildWorkflowStub(ChildWorkflowOperation.class);
            op1.execute(10);
            ChildWorkflowCompensation c1 =
                    Workflow.newChildWorkflowStub(ChildWorkflowCompensation.class);
            saga.addCompensation(c1::compensate, -10);

            // The following demonstrate how to compensate async invocations.
            Promise<Void> result = Async.procedure(activity::execute, 20);
            saga.addCompensation(activity::compensate, -20);
            result.get();

            saga.addCompensation(
                    () -> System.out.println("Other compensation logic in main workflow."));
            throw new RuntimeException("some error");

        } catch (Exception e) {
            saga.compensate();
        }
    }
}
```

</td>
<td valign="top">

```json
{
  "id": "HelloSaga",
  "name": "Hello SAGA compensation Workflow",
  "version": "1.0.0",
  "specVersion": "0.8",
  "states": [
    {
      "name": "ExecuteState",
      "type": "operation",
      "compensatedBy": "CompensateState",
      "actions": [
        {
          "name": "Execute",
          "functionRef": {
            "refName": "ExecuteFunction",
            "arguments": {
               "amount": 10
            }
          }
        }
      ],
      "end": {
        "compensate": true
      }
    },
    {
      "name": "CompensateState",
      "type": "operation",
      "usedForCompensation": true,
      "actions": [
        {
          "name": "Compensate",
          "functionRef": {
            "refName": "CompensateFunction",
            "arguments": {
               "amount": -10
            }
          }
        }
      ]
    }
  ],
  "functions": [
    {
      "name": "ExecuteFunction",
      "operation": "myactionsapi.json#execute"
    },
    {
      "name": "CompensateFunction",
      "operation": "myactionsapi.json#compensate"
    }
  ]
}
```

</td>
</tr>
</table>

#### Note

Serverless Workflow defines explicit compensation, meaning it has to be explicitly invoked
as part of the workflow control flow logic. For more information see the
[Workflow Compensation](../specification.md#Workflow-Compensation) section.

### Error Handling and Retries

[Full Temporal Example](https://github.com/temporalio/samples-java/blob/master/src/main/java/io/temporal/samples/hello/HelloActivityRetry.java)

<table>
<tr>
    <th>Temporal</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
// Workflow Implementation
public static class GreetingWorkflowImpl implements GreetingWorkflow {
    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder()
                            .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setInitialInterval(Duration.ofSeconds(1))
                                            .setDoNotRetry(IllegalArgumentException.class.getName())
                                            .build())
                            .build());
    @Override
    public String getGreeting(String name) {
        // This is a blocking call that returns only after activity is completed.
        return activities.composeGreeting("Hello", name);
    }
}

// Activity Implementation
static class GreetingActivitiesImpl implements GreetingActivities {
    private int callCount;
    private long lastInvocationTime;

    @Override
    public synchronized String composeGreeting(String greeting, String name) {
        if (lastInvocationTime != 0) {
            long timeSinceLastInvocation = System.currentTimeMillis() - lastInvocationTime;
            System.out.print(timeSinceLastInvocation + " milliseconds since last invocation. ");
        }
        lastInvocationTime = System.currentTimeMillis();
        if (++callCount < 4) {
            System.out.println("composeGreeting activity is going to fail");
            throw new IllegalStateException("not yet");
        }
        System.out.println("composeGreeting activity is going to complete");
        return greeting + " " + name + "!";
    }
}
```

</td>
<td valign="top">

```json
{
  "id": "HelloActivityRetry",
  "name": "Hello Activity with Retries Workflow",
  "version": "1.0.0",
  "specVersion": "0.8",
  "autoRetries": true,
  "start": "GreetingState",
  "states": [
    {
      "name": "GreetingState",
      "type": "operation",
      "actions": [
        {
          "name": "Greet",
          "functionRef": {
            "refName": "GreetingFunction",
            "arguments": {
               "name": "World"
            },
            "retryRef": "GreetingRetry",
            "nonRetryableErrors": ["IllegalArgumentException"]
          }
        }
      ],
      "timeouts": {
          "actionExecTimeout": "PT10S"
      },
      "onErrors": [
        {
          "errorRefs": ["IllegalStateException", "IllegalArgumentException"],
          "end": true
        }
      ],
      "end": true
    }
  ],
  "functions": [
    {
      "name": "GreetingFunction",
      "operation": "myactionsapi.json#composeGreeting"
    }
  ],
  "errors": [
    {
      "name": "IllegalStateException"
    },
    {
      "name": "IllegalArgumentException"
    }
  ],
  "retries": [
    {
      "name": "GreetingRetry",
      "delay": "PT1S"
    }
  ]
}
```

</td>
</tr>
</table>
