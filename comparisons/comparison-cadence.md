# Comparisons - Cadence

[Cadence](https://cadenceworkflow.io/) describes it solution as a fault-oblivious stateful programming model 
that obscures most of the complexities of building scalable distributed applications.
Cadence apps are written in code, and SDKs are available for Go, Java.
Its core abstraction is a fault-oblivious stateful workflow which by default encapsulates state, processing threads,
durable timers and event handlers. 

Cadence also has support for [non-programming-language workflow DSLs](https://cadenceworkflow.io/docs/use-cases/dsl/).
It says that any application that interprets a workflow DSL definition can be written using the Cadence SDK, so for example
BPMN, AWS Step Functions, etc. Since Cadence does not provide its own non-programming-language workflow DSL,
we will only take a look at its examples written in their provided programming language support.
 
The purpose of this document is to compare and contrast the Cadence workflow code and the equivalent
Serverless Workflow DSL. 
This can hopefully help compare and contrast the two workflow languages and give a better understanding of both.

Given that Cadence provides SDKs in multiple programming languages, in this document we will focus only on Cadence workflows
written in Java.

All Cadence examples used in this document are available in their [cadence-samples](https://github.com/uber-common/cadence-samples)

We hope that these examples will give you a good start for comparing and contrasting the two workflow
languages.

## Note when reading provided examples

Note that we do not include all the Cadence Java code from the examples, but just the main parts.
Each example includes a link to the Cadence example GitHub repo where you can see all the code 
for the particular example.

## Table of Contents

- [Saga pattern (compensation)](#Saga-Pattern)
- [File Processing](#File-Processing)
- [Sub Workflow Greeting](#Sub-Workflow-Greeting)

### Saga Pattern

[Full Cadence Example](https://github.com/uber/cadence-java-samples/tree/master/src/main/java/com/uber/cadence/samples/bookingsaga)

<table>
<tr>
    <th>Cadence</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
public class TripBookingWorkflowImpl implements TripBookingWorkflow {

  private final ActivityOptions options =
      new ActivityOptions.Builder().setScheduleToCloseTimeout(Duration.ofHours(1)).build();
  private final TripBookingActivities activities =
      Workflow.newActivityStub(TripBookingActivities.class, options);

  @Override
  public void bookTrip(String name) {
    Saga.Options sagaOptions = new Saga.Options.Builder().setParallelCompensation(true).build();
    Saga saga = new Saga(sagaOptions);
    try {
      String carReservationID = activities.reserveCar(name);
      saga.addCompensation(activities::cancelCar, carReservationID, name);

      String hotelReservationID = activities.bookHotel(name);
      saga.addCompensation(activities::cancelHotel, hotelReservationID, name);

      String flightReservationID = activities.bookFlight(name);
      saga.addCompensation(activities::cancelFlight, flightReservationID, name);
    } catch (ActivityException e) {
      saga.compensate();
      throw e;
    }
  }
}
```

</td>
<td valign="top">

```yaml
id: tripbookingwithcompensation
name: Trip Booking With Compensation
version: '1.0'
specVersion: '0.7'
start: BookTrip
states:
  - name: BookTrip
    type: operation
    compensatedBy: CancelTrip
    actions:
      - functionRef: reservecarfunction
      - functionRef: reservehotelfunction
      - functionRef: reserveflightfunction
    onErrors:
      - errorRef: Activity Error
        end:
          compensate: true
    end: true
  - name: CancelTrip
    type: operation
    usedForCompensation: true
    actionMode: parallel
    actions:
      - functionRef: cancelcarreservationfunction
      - functionRef: cancelhotelreservationfunction
      - functionRef: cancelflightreservationfunction
errors:
  - name: Activity Error
    code: '123'
functions:
  - name: reservecarfunction
    operation: myactionsapi.json#reservecar
  - name: reservehotelfunction
    operation: myactionsapi.json#reservehotel
  - name: reserveflightfunction
    operation: myactionsapi.json#reserveflight
  - name: cancelcarreservationfunction
    operation: myactionsapi.json#cancelcar
  - name: cancelhotelreservationfunction
    operation: myactionsapi.json#cancelhotel
  - name: cancelflightreservationfunction
    operation: myactionsapi.json#cancelflight
```

</td>
</tr>
</table>

### File Processing

[Full Cadence Example](https://github.com/uber/cadence-java-samples/tree/master/src/main/java/com/uber/cadence/samples/fileprocessing)

<table>
<tr>
    <th>Cadence</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
public class FileProcessingWorkflowImpl implements FileProcessingWorkflow {

  // Uses the default task list shared by the pool of workers.
  private final StoreActivities defaultTaskListStore;

  public FileProcessingWorkflowImpl() {
    // Create activity clients.
    ActivityOptions ao =
        new ActivityOptions.Builder()
            .setScheduleToCloseTimeout(Duration.ofSeconds(10))
            .setTaskList(FileProcessingWorker.TASK_LIST)
            .build();
    this.defaultTaskListStore = Workflow.newActivityStub(StoreActivities.class, ao);
  }

  @Override
  public void processFile(URL source, URL destination) {
    RetryOptions retryOptions =
        new RetryOptions.Builder()
            .setExpiration(Duration.ofSeconds(10))
            .setInitialInterval(Duration.ofSeconds(1))
            .build();
    // Retries the whole sequence on any failure, potentially on a different host.
    Workflow.retry(retryOptions, () -> processFileImpl(source, destination));
  }

  private void processFileImpl(URL source, URL destination) {
    StoreActivities.TaskListFileNamePair downloaded = defaultTaskListStore.download(source);

    // Now initialize stubs that are specific to the returned task list.
    ActivityOptions hostActivityOptions =
        new ActivityOptions.Builder()
            .setTaskList(downloaded.getHostTaskList())
            .setScheduleToCloseTimeout(Duration.ofSeconds(10))
            .build();
    StoreActivities hostSpecificStore =
        Workflow.newActivityStub(StoreActivities.class, hostActivityOptions);

    // Call processFile activity to zip the file.
    // Call the activity to process the file using worker-specific task list.
    String processed = hostSpecificStore.process(downloaded.getFileName());
    // Call upload activity to upload the zipped file.
    hostSpecificStore.upload(processed, destination);
  }
}
```

</td>
<td valign="top">

```yaml
id: fileprocessingwithretries
name: File Processing Workflow With Retries
version: '1.0'
specVersion: '0.7'
autoRetries: true
start: ProcessAndUpload
states:
  - name: ProcessAndUpload
    type: operation
    actions:
      - functionRef:
          refName: processfilefunction
          arguments:
            filename: "${ .file.name }"
        retryRef: Processing Retry Policy
        actionDataFilter:
          results: "${ .processed }"
      - functionRef:
          refName: uploadfunction
          arguments:
            file: "${ .processed }"
        retryRef: Processing Retry Policy
    end: true
functions:
  - name: processfilefunction
    operation: myactionsapi.json#process
  - name: uploadfilefunction
    operation: myactionsapi.json#upload
retries:
  - name: Processing Retry Policy
    maxAttempts: 10
    delay: PT1S
```

</td>
</tr>
</table>


### Sub Workflow Greeting

[Full Cadence Example](https://github.com/uber/cadence-java-samples/blob/master/src/main/java/com/uber/cadence/samples/hello/HelloChild.java)

<table>
<tr>
    <th>Cadence</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```java
public static class GreetingWorkflowImpl implements GreetingWorkflow {

    @Override
    public String getGreeting(String name) {
      // Workflows are stateful. So a new stub must be created for each new child.
      GreetingChild child = Workflow.newChildWorkflowStub(GreetingChild.class);

      // This is a non blocking call that returns immediately.
      // Use child.composeGreeting("Hello", name) to call synchronously.
      Promise<String> greeting = Async.function(child::composeGreeting, "Hello", name);
      // Do something else here.
      return greeting.get(); // blocks waiting for the child to complete.
    }

    // This example shows how parent workflow return right after starting a child workflow,
    // and let the child run itself.
    private String demoAsyncChildRun(String name) {
      GreetingChild child = Workflow.newChildWorkflowStub(GreetingChild.class);
      // non blocking call that initiated child workflow
      Async.function(child::composeGreeting, "Hello", name);
      // instead of using greeting.get() to block till child complete,
      // sometimes we just want to return parent immediately and keep child running
      Promise<WorkflowExecution> childPromise = Workflow.getWorkflowExecution(child);
      childPromise.get(); // block until child started,
      // otherwise child may not start because parent complete first.
      return "let child run, parent just return";
    }

  public static void main(String[] args) {
      // Start a worker that hosts both parent and child workflow implementations.
      Worker.Factory factory = new Worker.Factory(DOMAIN);
      Worker worker = factory.newWorker(TASK_LIST);
      worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class, GreetingChildImpl.class);
      // Start listening to the workflow task list.
      factory.start();
  
      // Start a workflow execution. Usually this is done from another program.
      WorkflowClient workflowClient = WorkflowClient.newInstance(DOMAIN);
      // Get a workflow stub using the same task list the worker uses.
      GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class);
      // Execute a workflow waiting for it to complete.
      String greeting = workflow.getGreeting("World");
      System.out.println(greeting);
      System.exit(0);
}
}
```

</td>
<td valign="top">

```yaml
id: subflowgreeting
name: SubFlow Greeting Workflow
version: '1.0'
specVersion: '0.7'
start: GreetingSubFlow
states:
- name: GreetingSubFlow
  type: operation
  actions:
  - subFlowRef: "subflowgreet"
  end: true
functions:
- name: greetingfunction
  operation: myactionsapi.json#greet
```

</td>
</tr>
</table>

#### Note

The Serverless Workflow example does not include the simple definition of the "subflowgreet", which would 
include just a starting operation state that executes the "greetingfunction". 