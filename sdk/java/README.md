![Verify JAVA SDK](https://github.com/cncf/wg-serverless-workflow/workflows/Verify%20JAVA%20SDK/badge.svg)
# Serverless Workflow Specification JAVA SDK

Provides the JAVA API/SPI for the [Serverless Workflow Specification](../../specification/README.md)

Allows you to parse Workflow types from JSON/YAML sources, as well as programmatically build your workflows using 
the provided builder API.

The SDK focus is to allow to quickly go from the workflow definition (JSON or YAML) to instances 
of Workflow type, as well as to provide a builder API to easily create a workflow programmatically.

Future versions will also include validation functionality.

This is **not** a workflow runtime implementation.

### Status

This SDK is considered work in progress. We intend to release versions which match the future releases 
of the Serverless Workflow specification. Currently the SDK features match those of the current 
"master" specification branch.

### Getting Started

#### Building locally

To build project and run tests locally:

```
git clone https://github.com/cncf/wg-serverless-workflow.git
cd sdk/java
mvn clean install
```

Then to use it in your project pom.xml add:

* API dependency

```xml
<dependency>
    <groupId>io.cncf</groupId>
    <artifactId>serverlessworkflow-api</artifactId>
    <version>0.2-SNAPSHOT</version>
</dependency>
```

* SPI dependency

```xml
<dependency>
    <groupId>io.cncf</groupId>
    <artifactId>serverlessworkflow-spi</artifactId>
    <version>0.2-SNAPSHOT</version>
</dependency>
```

### How to Use 

#### Creating from JSON/YAML source

You can create a Workflow instance from JSON/YAML source:

Let's say you have a simple YAML based workflow definition:

```yaml
id: greeting
version: '1.0'
name: Greeting Workflow
description: Greet Someone
functions:
- name: greetingFunction
  resource: functionResourse
states:
- name: Greet
  type: operation
  start:
    kind: default
  actionMode: sequential
  actions:
  - functionRef:
      refName: greetingFunction
      parameters:
        name: "$.greet.name"
    actionDataFilter:
      dataResultsPath: "$.payload.greeting"
  stateDataFilter:
    dataOutputPath: "$.greeting"
  end:
    kind: default
```

To parse it and create a Workflow intance you can do:

``` java
Workflow workflow = Workflow.fromSource(source);
```

where 'source' is the above mentioned YAML definition.

The fromSource static method can take in definitions in both JSON and YAML formats.

Once you have the Workflow instance you can use its API to inspect it, for example:

``` java
assertNotNull(workflow);
assertEquals("greeting", workflow.getId());
assertEquals("Greeting Workflow", workflow.getName());

assertNotNull(workflow.getFunctions());
assertEquals(1, workflow.getFunctions().size());
assertEquals("greetingFunction", workflow.getFunctions().get(0).getName());

assertNotNull(workflow.getStates());
assertEquals(1, workflow.getStates().size());
assertTrue(workflow.getStates().get(0) instanceof OperationState);

OperationState operationState = (OperationState) workflow.getStates().get(0);
assertEquals("Greet", operationState.getName());
assertEquals(DefaultState.Type.OPERATION, operationState.getType());

...
```

#### Using builder API

You can also programmatically create Workflow instances, for example:

``` java
Workflow testWorkflow = new Workflow().withId("test-workflow").withName("test-workflow-name").withVersion("1.0")
                .withEvents(Arrays.asList(
                        new EventDefinition().withName("testEvent").withSource("testSource").withType("testType"))
                )
                .withFunctions(Arrays.asList(
                        new Function().withName("testFunction").withResource("testResource").withType("testType"))
                )
                .withStates(Arrays.asList(
                        new DelayState().withName("delayState").withType(DELAY)
                                .withStart(
                                        new Start().withKind(Start.Kind.DEFAULT)
                                )
                                .withEnd(
                                        new End().withKind(End.Kind.DEFAULT)
                                )
                                .withTimeDelay("PT1M")
                        )
                );
```

This will create a test workflow that defines an event, a function and a single Delay State.

You can use the workflow instance to get its JSON/YAML definition as well:

``` java
assertNotNull(Workflow.toJson(testWorkflow));
assertNotNull(Workflow.toYaml(testWorkflow));
```
