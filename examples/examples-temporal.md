# Examples - Temporal

[Temporal](https://temporal.io/) is an open source microservice orchestration platform. Temporal apps are written
in code, and SDKs are currently available for Go, Java, PHP and Ruby. It provides two special functions, namely Workflow
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
in the repo mentioned above. 

We hope that these examples will give you a good start for comparing and contrasting the two workflow
languages.

## Note when reading provided examples

[Activities](https://docs.temporal.io/docs/activities) in temporal are comparable with 
[actions](https://github.com/serverlessworkflow/specification/blob/master/specification.md#Action-Definition) in Serverless Workflow
language, namely actions that reference [function](https://github.com/serverlessworkflow/specification/blob/master/specification.md#function-definition) 
definitions. Serverless Workflow action execution involves invoking distributed functions via REST or via events.

When looking at provided examples below, please note that the code defined in activities of
Temporal code is assumed to be stand-alone distributed functions/services accessible over REST API
and used in the compared Serverless Workflow DSL. 

Activities in Temporal seem to be a wrapper that can be used to add custom code to invoke RESTful functions for example,
where as in Serverless Workflow a wrapper is not used and RESTful function invocation is defined via the 
[OpenAPI specification](https://www.openapis.org/).

## Table of Contents
 
- [Single Activity](#Single-Activity)
- [Periodical Execution (Cron)](#Periodical-Execution)


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
public class HelloActivity {
    static final String TASK_QUEUE = "HelloActivity";
    
   // workflow interface
   @WorkflowInterface
   public interface GreetingWorkflow {
    @WorkflowMethod
        String getGreeting(String name);
    }
    // activity interface 
    @ActivityInterface
    public interface GreetingActivities {
        @ActivityMethod
        String composeGreeting(String greeting, String name);
    }
    
    public static class GreetingWorkflowImpl implements GreetingWorkflow {
        private final GreetingActivities activities =
            Workflow.newActivityStub(
                GreetingActivities.class,
                ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(2)).build());
    
        // workflow "getGreting" method that calls the activities methods
        @Override
        public String getGreeting(String name) {
          return activities.composeGreeting("Hello", name);
        }
    }
    
    // impl of the activities method
    static class GreetingActivitiesImpl implements GreetingActivities {
      @Override
      public String composeGreeting(String greeting, String name) {
        return greeting + " " + name + "!";
      }
    }
       
    // main method
    public static void main(String[] args) {
       WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
       WorkflowClient client = WorkflowClient.newInstance(service);
       WorkerFactory factory = WorkerFactory.newInstance(client);
       Worker worker = factory.newWorker(TASK_QUEUE);
  
       // create workflow instance
       worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
       // register activity
       worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
       factory.start();

       // start workflow exec
       GreetingWorkflow workflow =
        client.newWorkflowStub(
          GreetingWorkflow.class, 
          WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
       // exec workflow
       String greeting = workflow.getGreeting("World");

    }
}
```

</td>
<td valign="top">

```json
{
  "id": "HelloActivity",
  "name": "Hello Activity Workflow",
  "version": "1.0",
  "states": [
    {
      "name": "GreetingState",
      "type": "operation",
      "start": true,
      "actions": [
        {
          "name": "Greet",
          "functionRef": {
            "refName": "GreetingFunction",
            "parameters": {
               "name": "World"
            }
          },
          "timeout": "PT2S"
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
public class HelloActivity {
    static final String TASK_QUEUE = "HelloCron";
    static final String CRON_WORKFLOW_ID = "HelloCron";
    
   // workflow interface
   @WorkflowInterface
   public interface GreetingWorkflow {
    @WorkflowMethod
        String getGreeting(String name);
    }
    // activity interface 
    @ActivityInterface
    public interface GreetingActivities {
        @ActivityMethod
        String greet(String greeting);
    }
    
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
    
    // impl of the activities method
    static class GreetingActivitiesImpl implements GreetingActivities {
      @Override
      public String greet(String greeting) {
        System.out.println(
                  "From " + Activity.getExecutionContext().getInfo().getWorkflowId() + ": " + greeting);
      }
    }
       
    // main method
    public static void main(String[] args) {
       WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
       WorkflowClient client = WorkflowClient.newInstance(service);
       WorkerFactory factory = WorkerFactory.newInstance(client);
       Worker worker = factory.newWorker(TASK_QUEUE);
  
       // create workflow instance
       worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
       // register activity
       worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
       factory.start();
    
       WorkflowOptions workflowOptions =
               WorkflowOptions.newBuilder()
                   .setWorkflowId(CRON_WORKFLOW_ID)
                   .setTaskQueue(TASK_QUEUE)
                   .setCronSchedule("* * * * *")
                   .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                   .setWorkflowRunTimeout(Duration.ofMinutes(1))
                   .build();

        
       // start workflow exec
       GreetingWorkflow workflow =
        client.newWorkflowStub(
          GreetingWorkflow.class, 
          workflowOptions);
       // exec workflow
       try {
         WorkflowExecution execution = WorkflowClient.start(workflow::greet, "World");
         System.out.println("Started " + execution);
       } catch (WorkflowExecutionAlreadyStarted e) {
         System.out.println("Already running as " + e.getExecution());
       } catch (Throwable e) {
         e.printStackTrace();
         System.exit(1);
       }

    }
}
```

</td>
<td valign="top">

```json

```

</td>
</tr>
</table>

