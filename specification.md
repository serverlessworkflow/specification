# Serverless Workflow

## Abstract

A specification for defining declarative workflow models
that orchestrate event-driven, serverless applications.

## Status of this document

This document is a working draft.

## Table of Contents

- [Overview](#Overview)
- [Specification Goals](#Specification-Goals)
- [Specification Details](#Specification-Details)
  - [Workflow Model](#Workflow-Model)
  - [Workflow Data](#Workflow-Data)
  - [Workflow Data Expressions](#Workflow-Data-Expressions)
  - [Workflow Definition](#Workflow-Definition)
  - [Workflow Error Handling](#Workflow-Error-Handling)
    - [Defining Errors](#Defining-Errors)
    - [Defining Retries](#Defining-Retries)
  - [Workflow Compensation](#Workflow-Compensation)
  - [Workflow Metadata](#Workflow-Metadata)
- [Extensions](#Extensions)
- [Use Cases](#Use-Cases)
- [Examples](#Examples)
- [References](#References)
- [License](#License)

## Overview

Workflows have become a key component for orchestration and execution management of event-driven microservices
(serverless applications).

The lack of a common way to define and model workflows means that developers must constantly re-learn 
how to write them. This also limits the potential for common libraries, tooling and 
infrastructure to aid modeling and execution of workflows across different cloud/container platforms.
Portability as well as productivity that can be achieved from workflow orchestration is hindered overall.

Serverless Workflow specification focuses on defining a `vendor-neutral`, `platform-independent`, and
`declarative` workflow model which can be used across multiple cloud/container platforms. 
This allows users to move their workflow definitions from one cloud/container platform to another and 
expect the exact same, defined, execution steps to be performed on each. 

For more information on the history, development and design rationale behind the specification, see the [Serverless Workflow Wiki](https://github.com/serverlessworkflow/specification/wiki).

## Specification Goals

The specification provides a [Workflow JSON Schema](schema/workflow.json)
which defines the structure of the workflow model. This allows workflow models to be defined in both
[JSON](https://www.json.org/json-en.html) and [YAML](https://yaml.org/) formats. Both are considered specification compliant
if they validate against the defined schema.

The specification also provides Software Development Kits (SDKs) for both [Go](https://github.com/serverlessworkflow/sdk-go) and [Java](https://github.com/serverlessworkflow/sdk-java)
and we plan to add them for more languages in the future.

In addition, the specification provides a set of [Workflow Extensions](extensions/README.md) which 
allow users to define additional, non-execution-related workflow information. This information can be used to improve
workflow performance. 
Some example workflow extensions include Key Performance Indicators (KPIs), Simulation, Tracing, etc 

We are also working on a Technology Compatibility Kit (TCK) to be used as a specification conformance
tool for runtime implementations.

The specification relies on runtime implementations to adopt the defined workflow model and provide execution semantics.

<p align="center">
<img src="media/spec/spec-overview.png" height="400px" alt="Serverless Workflow Specification Overview"/>
</p>

With all this in place the specification goals focus on portability and vendor-neutrality. Using the workflow model
defined by the specification allows users to orchestrate event-driven microservices
on may different runtimes and cloud/container platforms.

<p align="center">
<img src="media/spec/spec-goals.png" height="400px" alt="Serverless Workflow Specification Goals"/>
</p>

## Specification Details

Following sections provide a detailed descriptions of all parts of the workflow model. 

### Workflow Model

The Serverless Workflow model is composed of: 

* [Function](#Function-Definition) definitions. Declare services that need to be invoked during workflow execution. 
* [Event](#Event-Definition) definitions. Declare events that need to be `consumed` to start or continue workflow instances, trigger function/service execution, or be `produced` during workflow execution.
* [State](#State-Definition) and [transition](#Transitions) definitions. States define the workflow `control flow logic`, manage workflow data, and can reference defined functions and events. 
Transitions define the connections between workflow states.

Serverless Workflow model defines a declarative language that can be used to model both simple and complex orchestrations
for event-driven, serverless applications.

### Workflow Data

Serverless Workflow data is represented in [JSON](https://www.json.org/json-en.html) format.
Data flow during workflow execution can be divided into:

- [Workfow data input](#Workflow-data-input)
- [Event data](#Event-data)
- [Action data](#Action-data)
- [Information passing between states](#Information-passing-Between-States)
- [State Data filtering](#State-data-filtering)
- [Workflow data output](#Workflow-data-output)

### Workflow Data Expressions

Workflow model parameters may use expressions to access the JSON data. 
Note that different data filters play a big role as to which parts of the states data are selected. Reference the 
[State Data Filtering](#State-Data-Filtering) section for more information about data state data filters.

All expressions must follow the [JsonPath](https://github.com/json-path/JsonPath) format and can be evaluated with a JsonPath expression evaluator. 
JsonPath expressions can select and extract the workflow JSON data. 

All expressions are written inside double braces:

```text
{{ expression }}
```

Expressions should be resolved and the results returned exactly where the expression is written.

To show some expression examples, let's say that we have the following JSON data:

```json
{
  "firstName": "John",
  "lastName" : "Doe",
  "age"      : 26,
  "address"  : {
    "streetAddress": "Naist street",
    "city"         : "Nara",
    "postalCode"   : "630-0192"
  },
  "phoneNumbers": [
    {
      "type"  : "iPhone",
      "number": "0123-4567-8888"
    },
    {
      "type"  : "home",
      "number": "0123-4567-8910"
    }
  ]
}
```

We can use an expression inside a string type parameter, for example:

```json
{
    "paramName": "Hello {{ $.firstName }} {{ $.lastName }}"
}
```

would set the value of `paramName` to `Hello John Doe`.

Expressions can also be used to select a portion of the JSON data, this is in particularly useful for data filters. For example:

```json
{
   "stateDataFilter": {
       "dataOutputPath": "{{ $.phoneNumbers }}"
   }
}
```

Would set the data output of the particular state to:

```json
[
   {
      "type" : "iPhone",
      "number" : "0123-4567-8888"
   },
   {
      "type" : "home",
      "number" : "0123-4567-8910"
   }
]
```

[Switch state](#Switch-State) [conditions](#switch-state-dataconditions) require for expressions to be resolved to a boolean value (true / false).
In this case JsonPath expressions can also be used. 
The expression should evaluate to true, if the result contains a subset of the JSON data, and false if it is empty. 
For example:

```json
{
      "name": "Eighteen or older",
      "condition": "{{ $.applicants[?(@.age >= 18)] }}",
      "transition": {
        "nextState": "StartApplication"
      }
}
```

In this case the condition would be evaluated to true if it returns a non-empty subset of the queried data.

JsonPath also includes a limited set of built-in functions that can be used inside expressions. For example the expression

```json
{
   "phoneNums" : "{{ $.phoneNumbers.length() }}"
}
```

would set the value of `phoneNums` to `2`.

As previously mentioned, expressions are evaluated against certain subsets of data. For example 
the `parameters` param of the [functionRef definition](#FunctionRef-Definition) can evaluate expressions 
only against the data that is available to the [action](#Action-Definition) it belongs to.
One thing to note here are the top-level [workflow definition](#Workflow-Definition) parameters. Expressions defined
in them can only be evaluated against the initial [workflow data input](#Workflow-Data-Input).

For example let's say that we have a workflow data input of:

```json
{
   "inputVersion" : "1.0.0"
}
```

we can then define a workflow definition:

```json
{
   "id": "MySampleWorkflow",
   "name": "Sample Workflow",
   "version": "{{ $.inputVersion }}"
}
```

which would set the workflow version to `1.0.0`.

### Workflow Definition

| Parameter | Description | Type | Required |
| --- | --- |  --- | --- |
| id | Workflow unique identifier | string | yes |
| name | Workflow name | string | yes |
| description | Workflow description | string | no |
| version | Workflow version | string | no |
| schemaVersion | Workflow schema version | string | no |
| [dataInputSchema](#Workflow-Data-Input) | URI to JSON Schema that workflow data input adheres to | string | no |
| [dataOutputSchema](#Workflow-data-output) | URI to JSON Schema that workflow data output adheres to | string | no |
| [events](#Event-Definition) | Workflow event definitions.  | array or string | no |
| [functions](#Function-Definition) | Workflow function definitions. Can be either inline function definitions (if array) or URI pointing to a resource containing json/yaml function definitions (if string) | array or string| no |
| [retries](#Retry-Definition) | Workflow retries definitions. Can be either inline retries definitions (if array) or URI pointing to a resource containing json/yaml retry definitions (if string) | array or string| no |
| [states](#State-Definition) | Workflow states | array | yes |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
   "id": "sampleWorkflow",
   "version": "1.0",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "states": [],
   "functions": [],
   "events": [],
   "retries":[]
}
```

</td>
<td valign="top">

```yaml
id: sampleWorkflow
version: '1.0'
name: Sample Workflow
description: Sample Workflow
states: []
functions: []
events: []
retries: []
```

</td>
</tr>
</table>

</details>

Defines the top-level structure of a serverless workflow model.
Following figure describes the main workflow definition blocks.

<p align="center">
<img src="media/spec/workflowdefinitionblocks.png" height="300px" alt="Serverless Workflow Definitions Blocks"/>
</p>

The `functions` property can be either an in-line [function](#Function-Definition) definition array, or an URI reference to
a resource containing an array of [functions](#Function-Definition) definition. 
Referenced resource can be used by multiple workflow definitions.

Here is an example of using external resource for function definitions:

1. Workflow definition:
```json
{  
   "id": "sampleWorkflow",
   "version": "1.0",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "functions": "http://myhost:8080/functiondefs.json",
   "states":[
     ...
   ]
}
```

2. Function definitions resource
```json
{
   "functions": [
      {
         "name":"HelloWorldFunction",
         "operation":"file://myapi.json#helloWorld"
      }
   ]
}
```

Referenced resource must conform to the specifications [Workflow Functions JSON Schema](schema/functions.json). 

The `events` property can be either an in-line [event](#Event-Definition) definition array, or an [URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) reference to
a resource containing an array of [event](#Event-Definition) definition. Referenced resource can be used by multiple workflow definitions.

Here is an example of using external resource for event definitions:

1. Workflow definition:
```json
{  
   "id": "sampleWorkflow",
   "version": "1.0",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "events": "http://myhost:8080/eventsdefs.json",
   "states":[
     ...
   ]
}
```

2. Event definitions resource
```json
{
   "events": [
      {  
         "name": "ApplicantInfo",
         "type": "org.application.info",
         "source": "applicationssource",
         "correlation": [
          { 
            "contextAttributeName": "applicantId"
          } 
         ]
      }
   ]
}
```

Referenced resource must conform to the specifications [Workflow Events JSON Schema](schema/events.json). 

The `retries` property can be either an in-line [retry](#Retry-Definition) definition array, or an URI reference to
a resource containing an array of [retry](#Retry-Definition) definition. 
Referenced resource can be used by multiple workflow definitions. For more information about 
using and referencing retry definitions see the [Workflow Error Handling](#Workflow-Error-Handling) section.

#### Function Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique function name | string | yes |
| operation | Combination of the function/service OpenAPI definition URI and the operationID of the operation that needs to be invoked, separated by a '#'. For example 'https://petstore.swagger.io/v2/swagger.json#getPetById' | string | no |
| [metadata](#Workflow-Metadata) | Metadata information. Can be used to define custom function information | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
   "name": "HelloWorldFunction",
   "operation": "https://hellworldservice.api.com/api.json#helloWorld"
}
```

</td>
<td valign="top">

```yaml
name: HelloWorldFunction
operation: https://hellworldservice.api.com/api.json#helloWorld
```

</td>
</tr>
</table>

</details>

Function definitions describe services and their operations that need to be invoked during workflow execution.
They can be referenced by states [action definitions](#Action-Definition)]. 

Note that function definitions focus on defining services which are not invoked via events. 
To define services that are invoked via events, please reference the [event definitions](#Event-Definition) section, as well as the [actions definitions](#Action-Definition) [eventRef](#EventRef-Definition) property.
 
Because of an overall lack of a common way to describe different services and their operations,
workflow markups typically chose to define custom function definitions.
This approach often runs into issues such as lack of markup portability, limited capabilities, as well as 
forces non-workflow-specific information such as service authentication to be added inside workflow markup.

To avoid these issues, the Serverless Workflow specification mandates that details about 
services and their operations be described using the [OpenAPI Specification](https://www.openapis.org/) specification.
OpenAPI is a language-agnostic standard that describes discovery of RESTful services. It is perfectly suited
for defining every detail about how RESTful services and their operations should be invoked.
It allows Servlerless Workflow markup to describe RESTful services in a portable 
way, as well as workflow runtimes to utilize OpenAPI tooling and APIs to invoke those services.

The workflow markup still supports defining non-restful services and their operations using the `metadata` property
of function definitions. It can however not guarantee its portability on the markup level.
Runtimes can utilize the function definition `metadata` information to invoke non-restful service operations.

The `name` property defines an unique name of the function definition.

The `operation` property is a combination of the function/service OpenAPI definition document URI and the particular service operation that needs to be invoked, separated by a '#'. 
For example `https://petstore.swagger.io/v2/swagger.json#getPetById`. 
In this example "getPetById" is the OpenAPI ["operationId"](https://swagger.io/docs/specification/paths-and-operations/)
property in the services OpenAPI definition document which uniquely identifies a specific service operation that needs to be invoked.

The [`metadata`](#Workflow-Metadata) property allows users to define custom information to the custom definitions.
This allows runtimes to define services and their operations that cannot be described via OpenAPI.
An example of such definition could be defining invocation of a command on an image, for example:

```yaml
functions:
- name: whalesayimage
  metadata:
    image: docker/whalesay
    command: cowsay
```

Function definitions themselves do not define data input parameters. Parameters can be 
defined via the `parameters` property in [function definitions](#FunctionRef-Definition) inside [actions](#Action-Definition).

#### Event Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique event name | string | yes if `names` is not used |
| names | List of unique event names that share the rest of the properties (source, type, kind, ...) | array | yes if `name` is not used |
| source | CloudEvent source | string | yes if kind is set to "consumed", otherwise no |
| type | CloudEvent type | string | yes |
| kind | Defines the event is either `consumed` or `produced` by the workflow. Default is `consumed` | enum | no |
| [correlation](#Correlation-Definition) | Define event correlation rules for this event. Only used for consumed events | array | no |
| [metadata](#Workflow-Metadata) | Metadata information | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
   "name": "ApplicantInfo",
   "type": "org.application.info",
   "source": "applicationssource",
   "kind": "consumed",
   "correlation": [
    { 
      "contextAttributeName": "applicantId"
    } 
   ]
}
```

</td>
<td valign="top">

```yaml
name: ApplicantInfo
type: org.application.info
source: applicationssource
kind: consumed
correlation:
- contextAttributeName: applicantId
```

</td>
</tr>
</table>

</details>

Used to define events and their correlations. These events can be either consumed or produced during workflow execution as well
as can be used to [trigger function/service invocations](#EventRef-Definition).

The Serverless Workflow specification mandates that all events conform to the [CloudEvents](https://github.com/cloudevents/spec) specification. 
This is to assure consistency and portability of the events format used.

The `name` property defines a single name of the event that is unique inside the workflow definition. This event name can be 
then referenced within [function](#Function-Definition) and [state](#State-Definition) definitions.

The `names` property can be used instead of `name` when you want to define multiple events that share
the same `source`, `type`, `kind`, and correlation rules. 
To give an example, the following two events definitions are considered to be equal:

<table>
<tr>
    <th>(1)</th>
    <th>(2)</th>
</tr>
<tr>
<td valign="top">

```json
{  
  "events": [
    {
      "name": "Event1",
      "type": "org.events",
      "source": "eventssource"
    },
    {
      "name": "Event2",
      "type": "org.events",
      "source": "eventssource"
    },
    {
      "name": "Event3",
      "type": "org.events",
      "source": "eventssource"
    }
  ]
}
```

</td>
<td valign="top">

```json
{  
  "events": [
    {
      "names": ["Event1", "Event2", "Event3"],
      "type": "org.events",
      "source": "eventssource"
    }
  ]
}
```

</td>
</tr>
</table>

The `source` property matches this event definition with the [source](https://github.com/cloudevents/spec/blob/master/spec.md#source-1)
property of the CloudEvent required attributes.

The `type` property matches this event definition with the [type](https://github.com/cloudevents/spec/blob/master/spec.md#type) property of the 
CloudEvent required attributes.

The `kind` property defines this event as either `consumed` or `produced`. In terms of the workflow, this means it is either an event 
that triggers workflow instance creation, or continuation of workflow instance execution (consumed), or an event 
that the workflow instance creates during its execution (produced).
The default value (if not specified) of the `kind` property is `consumed`. 
Note that for `produced` event definitions, implementations must provide the value of the CloudEvent source attribute. 
In this case (i.e., when the `kind` property is set to `produced`), the `source` property of the event definition is not required.
Otherwise (i.e., when the `kind` property is set to `consumed`), the `source` property must be defined in the event definition.


Event correlation plays a big role in large event-driven applications. Correlating one or more events with a particular workflow instance
can be done by defining the event correlation rules within the `correlation` property. 
This property is an array of [correlation](#Correlation-Definition) definitions.
The CloudEvents specification allows users to add [Extension Context Attributes](https://github.com/cloudevents/spec/blob/master/spec.md#extension-context-attributes)
and the correlation definitions can use these attributes to define clear matching event correlation rules.
Extension context attributes are not part of the event payload, so they are serialized the same way as other standard required attributes.
This means that the event payload does not have to be inspected by implementations in order to read and evaluate the defined correlation rules.


Let's take a look at an example. Here we have two events that have an extension context attribute called "patientId" (as well as "department", which 
will be used in further examples below):

```json
{
    "specversion" : "1.0",
    "type" : "com.hospital.patient.heartRateMonitor",
    "source" : "hospitalMonitorSystem",
    "subject" : "HeartRateReading",
    "id" : "A234-1234-1234",
    "time" : "2020-01-05T17:31:00Z",
    "patientId" : "PID-12345",
    "department": "UrgentCare",
    "data" : {
      "value": "80bpm"
    }
}
```

and

```json
{
    "specversion" : "1.0",
    "type" : "com.hospital.patient.bloodPressureMonitor",
    "source" : "hospitalMonitorSystem",
    "subject" : "BloodPressureReading",
    "id" : "B234-1234-1234",
    "time" : "2020-02-05T17:31:00Z",
    "patientId" : "PID-12345",
    "department": "UrgentCare",
    "data" : {
      "value": "110/70"
    }
}
```

We can then define a correlation rule, through which all consumed events with the "hospitalMonitorSystem", and the "com.hospital.patient.heartRateMonitor"
type that have the **same** value of the `patientId` property to be correlated to the created workflow instance:

```json
{
"events": [
 {
  "name": "HeartRateReadingEvent",
  "source": "hospitalMonitorSystem",
  "type": "com.hospital.patient.heartRateMonitor",
  "kind": "consumed",
  "correlation": [
    { 
      "contextAttributeName": "patientId"
    }
  ]
 }
]
}
```

If a workflow instance is created (e.g., via Event state) by consuming a "HeartRateReadingEvent" event, all other consumed events
from the defined source and with the defined type that have the same "patientId" as the event that triggered the workflow instance
should then also be associated with the same instance.

You can also correlate multiple events together. In the following example, we assume that the workflow consumes two different event types
and we want to make sure that both are correlated, as in the above example, with the same "patientId": 


```json
{
"events": [
 {
  "name": "HeartRateReadingEvent",
  "source": "hospitalMonitorSystem",
  "type": "com.hospital.patient.heartRateMonitor",
  "kind": "consumed",
  "correlation": [
    { 
      "contextAttributeName": "patientId"
    }
  ]
 },
 {
   "name": "BloodPressureReadingEvent",
   "source": "hospitalMonitorSystem",
   "type": "com.hospital.patient.bloodPressureMonitor",
   "kind": "consumed",
   "correlation": [
       { 
         "contextAttributeName": "patientId"
       }
     ]
  }
]
}
```

Event correlation can be based on equality (values of the defined "contextAttributeName" must be equal), but it can also be based
on comparing it to custom defined values (string, or expression). For example:

```json
{
"events": [
 {
  "name": "HeartRateReadingEvent",
  "source": "hospitalMonitorSystem",
  "type": "com.hospital.patient.heartRateMonitor",
  "kind": "consumed",
  "correlation": [
    { 
      "contextAttributeName": "patientId"
    },
    {
      "contextAttributeName": "department",
      "contextAttributeValue" : "UrgentCare"
    }
  ]
 }
]
}
```

In this example, we have two correlation rules defined: The first one is on the "patientId" CloudEvent context attribute, meaning again that 
all consumed events from this source and type must have the same "patientId" to be considered. The second rule
says that these events must all have a context attribute named "department" with the value of "UrgentCare".

This allows developers to write orchestration workflows that are specifically targeted to patients that are in the hospital urgent care unit, 
for example.


#### Correlation Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| contextAttributeName | CloudEvent Extension Context Attribute name | string | yes |
| contextAttributeValue | CloudEvent Extension Context Attribute name | string  | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
   "correlation": [
       { 
         "contextAttributeName": "patientId"
       },
       {
         "contextAttributeName": "department",
         "contextAttributeValue" : "UrgentCare"
       }
     ]
}
```

</td>
<td valign="top">

```yaml
correlation:
- contextAttributeName: patientId
- contextAttributeName: department
  contextAttributeValue: UrgentCare
```

</td>
</tr>
</table>

</details>

Used to define event correlation rules. Only usable for `consumed` event definitions.

The `contextAttributeName` property defines the name of the CloudEvent [extension context attribute](https://github.com/cloudevents/spec/blob/master/spec.md#extension-context-attributes).
The `contextAttributeValue` property defines the value of the defined the CloudEvent [extension context attribute](https://github.com/cloudevents/spec/blob/master/spec.md#extension-context-attributes).

#### State Definition

States define building blocks of the Serverless Workflow. The specification defines the following states:

| Name | Description | Consumes events? | Produces events? | Executes actions? | Handles errors/retries? | Allows parallel execution? | Makes data-based transitions? | Can be workflow start state? | Can be workflow end state? |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **[Event](#Event-State)** | Define events that trigger action execution | yes | yes | yes | yes | yes | no | yes | yes |
| **[Operation](#Operation-State)** | Execute one or more actions | no | yes | yes | yes | yes | no | yes | yes |
| **[Switch](#Switch-State)** | Define data-based or event-based workflow transitions | no | yes | no | yes | no | yes | yes | no |
| **[Delay](#Delay-State)** | Delay workflow execution | no | yes | no | yes | no | no | yes | yes |
| **[Parallel](#Parallel-State)** | Causes parallel execution of branches (set of states) | no | yes | no | yes | yes | no | yes | yes |
| **[SubFlow](#SubFlow-State)** | Represents the invocation of another workflow from within a workflow | no | yes | no | yes | no | no | yes | yes |
| **[Inject](#Inject-State)** | Inject static data into state data | no | yes | no | yes | no | no | yes | yes |
| **[ForEach](#ForEach-State)** | Parallel execution of states for each element of a data array | no | yes | no | yes | yes | no | yes | yes |
| **[Callback](#Callback-State)** | Manual decision step. Executes a function and waits for callback event that indicates completion of the manual decision | yes | yes | yes | yes | no | no | yes | yes |

The following is a detailed description of each of the defined states.

#### Event State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| exclusive | If "true", consuming one of the defined events causes its associated actions to be performed. If "false", all of the defined events must be consumed in order for actions to be performed. Default is "true"  | boolean | no |
| [onEvents](#eventstate-onevents) | Define the events to be consumed and one or more actions to be performed | array | yes |
| [timeout](#eventstate-timeout) | Time period to wait for incoming events (ISO 8601 format). For example: "PT15M" (wait 15 minutes), or "P2DT3H4M" (wait 2 days, 3 hours and 4 minutes)| string | no |
| [stateDataFilter](#state-data-filter) | State data filter definition| object | no |
| [dataInputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data input adheres to | string | no |
| [dataOutputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data output adheres to | string | no |
| [transition](#Transitions) | Next transition of the workflow after all the actions have been performed | object | yes |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | Is this state an end state | object | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
"name": "MonitorVitals",
"type": "event",
"start": {
    "kind": "default"
},
"exclusive": true,
"onEvents": [{
        "eventRefs": ["HighBodyTemperature"],
        "actions": [{
            "functionRef": {
                "refName": "sendTylenolOrder",
                "parameters": {
                    "patientid": "{{ $.patientId }}"
                }
            }
        }]
    },
    {
        "eventRefs": ["HighBloodPressure"],
        "actions": [{
            "functionRef": {
                "refName": "callNurse",
                "parameters": {
                    "patientid": "{{ $.patientId }}"
                }
            }
        }]
    },
    {
        "eventRefs": ["HighRespirationRate"],
        "actions": [{
            "functionRef": {
                "refName": "callPulmonologist",
                "parameters": {
                    "patientid": "{{ $.patientId }}"
                }
            }
        }]
    }
],
"end": {
    "kind": "terminate"
}
}
```

</td>
<td valign="top">

```yaml
name: MonitorVitals
type: event
start:
  kind: default
exclusive: true
onEvents:
- eventRefs:
  - HighBodyTemperature
  actions:
  - functionRef:
      refName: sendTylenolOrder
      parameters:
        patientid: "{{ $.patientId }}"
- eventRefs:
  - HighBloodPressure
  actions:
  - functionRef:
      refName: callNurse
      parameters:
        patientid: "{{ $.patientId }}"
- eventRefs:
  - HighRespirationRate
  actions:
  - functionRef:
      refName: callPulmonologist
      parameters:
        patientid: "{{ $.patientId }}"
end:
  kind: terminate
```

</td>
</tr>
</table>

</details>

Event states await one or more events and perform actions when they are received.
If defined as the workflow starting state, the event state definition controls when the workflow
instances should be created.

The `exclusive` property determines if the state should wait for any of the defined events in the `onEvents` array, or 
if all defined events must be present for their associated actions to be performed.

Following two figures illustrate the `exclusive` property:

<p align="center">
<img src="media/spec/event-state-exclusive-true.png" height="300px" alt="Event state with exclusive set to true"/>
</p>

If the Event state in this case is a starting state, the occurrence of *any* of the defined events would start a new workflow instance.

<p align="center">
<img src="media/spec/event-state-exclusive-false.png" height="300px" alt="Event state with exclusive set to false"/>
</p>

If the Event state in this case is a starting state, the occurrence of *all* defined events would start a new
 workflow instance.
  
In order to consider only events that are related to each other, we need to set the `correlation` property in the workflow
 [events definitions](#Event-Definition). This allows us to set up event correlation rules against the events 
 extension context attributes.

If the Event state is not a starting state, the `timeout` property can be used to define the time duration from the 
invocation of the event state. If the defined event, or events have not been received during this time, 
the state should transition to the next state or can end the workflow execution (if it is an end state).

#### <a name="eventstate-onevents"></a> Event State: onEvents Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| eventRefs | References one or more unique event names in the defined workflow [events](#Event-Definition) | array | yes |
| actionMode | Specifies how actions are to be performed (in sequence of parallel). Default is "sequential" | string | no |
| [actions](#Action-Definition) | Actions to be performed | array | yes |
| [eventDataFilter](#event-data-filter) | Event data filter definition | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "eventRefs": ["HighBodyTemperature"],
    "actions": [{
        "functionRef": {
            "refName": "sendTylenolOrder",
            "parameters": {
                "patientid": "{{ $.patientId }}"
            }
        }
    }]
}
```

</td>
<td valign="top">

```yaml
eventRefs:
- HighBodyTemperature
actions:
- functionRef:
    refName: sendTylenolOrder
    parameters:
      patientid: "{{ $.patientId }}"
```

</td>
</tr>
</table>

</details>

OnEvent definition allow you to define which [actions](#Action-Definition) are to be performed 
for the one or more [events definitions](#Event-Definition) defined in the `eventRefs` property.

The `actionMode` property defines if the defined actions need to be performed sequentially or in parallel.

The `actions` property defines a list of actions to be performed.

When specifying the `onEvents` definition it is important to consider the Event states `exclusive` property, 
because it determines how 'onEvents' is interpreted.
Let's look at the following JSON definition of 'onEvents' to show this:

```json
{
 "eventsActions": [
    {
        "eventRefs": [
            "HighBodyTemperature",
            "HighBloodPressure"
        ],
        "actions": [
            {
                "functionRef": {
                    "refName": "SendTylenolOrder",
                    "parameters": {
                       "patient": "{{ $.patientId }}"
                    }
                }
            },
            {
                "functionRef": {
                    "refName": "CallNurse",
                    "parameters": {
                       "patient": "{{ $.patientId }}"
                    }
                }
            } 
        ]
    }
]
}
```

Depending on the value of the Event states `exclusive` property, this definition can mean two different things:

1. If `exclusive` is set to "true", the consumption of **either** the `HighBodyTemperature` or `HighBloodPressure` events will trigger action execution.

2. If `exclusive` is set to "false", the consumption of **both** the `HighBodyTemperature` and `HighBloodPressure` events will trigger action execution. 

This is visualized in the diagram below:

<p align="center">
<img src="media/spec/event-state-onevents-example.png" height="300px" alt="Event onEvents example"/>
</p>

#### <a name="eventstate-timeout"></a> Event State: Timeout

The event state timeout period is described in the ISO 8601 data and time format.
You can specify for example "PT15M" to represent 15 minutes or "P2DT3H4M" to represent 2 days, 3 hours and 4 minutes.
Timeout values should always be represented as durations and not as time/repeating intervals.

The timeout property needs to be described in detail as it depends on whether or not the Event state is a starting workflow
state or not.

If the Event state is a starting state, incoming events may trigger workflow instances. In this case,
if the `exclusive` property is set to true, the timeout property should be ignored.

If the `exclusive` property is set to false, in this case, the defined timeout represents the time
between arrival of specified events. To give an example, consider the following:

```json
{
"states": [
{
    "name": "ExampleEventState",
    "type": "event",
    "exclusive": false,
    "timeout": "PT2M",
    "onEvents": [
        {
            "eventRefs": [
                "ExampleEvent1",
                "ExampleEvent2"
            ],
            "actions": [
              ...
            ]
        }
    ],
    "end": {
        "kind": "terminate"
    }
}
]
}
```

The first timeout would start once any of the referenced events are consumed. If the second event does not occur within
the defined timeout, no workflow instance should be created.

If the event state is not a starting state, the `timeout` property is relative to the time when the
state becomes active. If the defined event conditions (regardless of the value of the exclusive property)
are not satisfied within the defined timeout period, the event state should transition to the next state or end the workflow
instance in case it is an end state without performing any actions.

#### Action Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique action name | string | no |
| [functionRef](#FunctionRef-Definition) | References a reusable function definition | object | yes if `eventRef` is not used |
| [eventRef](#EventRef-Definition) | References a `trigger` and `result` reusable event definitions | object | yes if `functionRef` is not used |
| timeout | Time period to wait for function execution to complete or the resultEventRef to be consumed (ISO 8601 format). For example: "PT15M" (15 minutes), or "P2DT3H4M" (2 days, 3 hours and 4 minutes)| string | no |
| [actionDataFilter](#action-data-filter) | Action data filter definition | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "name": "Finalize Application Action",
    "functionRef": {
        "refName": "finalizeApplicationFunction",
        "parameters": {
            "student": "{{ $.applicantId }}"
        }
    },
    "timeout": "PT15M"
}
```

</td>
<td valign="top">

```yaml
name: Finalize Application Action
functionRef:
  refName: finalizeApplicationFunction
  parameters:
    student: "{{ $.applicantId }}"
timeout: PT15M
```

</td>
</tr>
</table>

</details>

Actions specify invocations of services during workflow execution.
Service invocation can be done in two different ways:

* Reference [functions definitions](#Function-Definition) by its unique name using the `functionRef` property.
* Reference a `produced` and `consumed` [event definitions](#Event-Definition) via the `eventRef` property. 
In this scenario a service or a set of services we want to invoke
are not exposed via a specific resource URI for example, but can only be invoked via events. 
The [eventRef](#EventRef-Definition) defines the 
referenced `produced` event via its `triggerEventRef` property and a `consumed` event via its `resultEventRef` property.

The `timeout` property defines the amount of time to wait for function execution to complete, or the consumed event referenced by the 
`resultEventRef` to become available.
It is described in ISO 8601 format, so for example "PT2M" would mean the maximum time for the function to complete
its execution is two minutes. 

Possible invocation timeouts should be handled via the states [onErrors](#Workflow-Error-Handling) definition.

#### FunctionRef Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| refName | Name of the referenced [function](#Function-Definition) | string | yes |
| parameters | Parameters to be passed to the referenced function | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "refName": "finalizeApplicationFunction",
    "parameters": {
        "student": "{{ $.applicantId }}"
    }
}
```

</td>
<td valign="top">

```yaml
refName: finalizeApplicationFunction
parameters:
  student: "{{ $.applicantId }}"
```

</td>
</tr>
</table>

</details>

Used by actions to reference a defined serverless function by its unique name. Parameters are values passed to the
function. They can include either static values or reference the states data input.

#### EventRef Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| [triggerEventRef](#Event-Definition) | Reference to the unique name of a `produced` event definition | string | yes |
| [resultEventRef](#Event-Definitions) | Reference to the unique name of a `consumed` event definition | string | yes |
| data | If string type, an expression which selects parts of the states data output to become the data (payload) of the event referenced by `triggerEventRef`. If object type, a custom object to become the data (payload) of the event referenced by `triggerEventRef`. | string or object | no |
| contextAttributes | Add additional event extension context attributes to the trigger/produced event | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "eventRef": {
      "triggerEventRef": "MakeVetAppointment",
      "data": "{{ $.patientInfo }}",
      "resultEventRef":  "VetAppointmentInfo"
   }
}
```

</td>
<td valign="top">

```yaml
eventRef:
  triggerEventRef: MakeVetAppointment
  data: "{{ $.patientInfo }}"
  resultEventRef: VetAppointmentInfo
```

</td>
</tr>
</table>

</details>

References a `produced` and `consumed` [event definitions](#Event-Definition) via the "triggerEventRef" and `resultEventRef` properties, respectively.

The `data` property can have two types: string or object. If it is of string type, it is an expression that can select parts of state data
to be used as payload of the event referenced by `triggerEventRef`. If it is of object type, you can define a custom object to be the event payload.

The `contextAttributes` property allows you to add one or more [extension context attributes](https://github.com/cloudevents/spec/blob/master/spec.md#extension-context-attributes)
to the trigger/produced event. 

#### Error Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| error | Domain-specific error name, or '*' to indicate all possible errors | string | yes |
| code | Error code. Can be used in addition to the name to help runtimes resolve to technical errors/exceptions. Should not be defined if error is set to '*' | string | no |
| retryRef | Defines the unique retry strategy definition to be used | string | no |
| [transition](#Transitions) or [end](#End-Definition) | Transition to next state to handle the error, or end workflow execution if this error is encountered | object | yes |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "error": "Item not in inventory",
   "transition": {
     "nextState": "IssueRefundToCustomer"
   }
}
```

</td>
<td valign="top">

```yaml
error: Item not in inventory
transition:
  nextState: IssueRefundToCustomer
```

</td>
</tr>
</table>

</details>

Error definitions describe errors that can occur during workflow execution and how to handle them. 

The `error`property defines the domain-specific name of the error. Users can also set the name to 
`*` which is a wildcard specifying "all" errors, in the case where no other error definitions are defined,
or "all other" errors if there are other errors defined within the same states `onErrors` definition.

The `code` property can be used in addition to `name` to help runtimes resolve the defined 
domain-specific error to the actual technical errors/exceptions that may happen during runtime execution.

The `transition` property defines the transition to the next workflow state in cases when the defined 
error happens during runtime execution.

If `transition` is not defined you can also define the `end` property which will end workflow execution at that point.

The `retryRef` property is used to define the retry strategy to be used for this particular error.

For more information, see the [Workflow Error Handling](#Workflow-Error-Handling) sections.

#### Retry Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique retry strategy name | string | yes |
| delay | Time delay between retry attempts (ISO 8601 duration format) | string | no |
| multiplier | Multiplier value by which interval increases during each attempt (ISO 8601 time format). For example: "PT3S" meaning the second attempt interval is increased by 3 seconds, the third interval by 6 seconds and so on | string | no |
| maxAttempts | Maximum number of retry attempts. Value of 0 means no retries are performed | string or integer | no |
| jitter | If float type, maximum amount of random time added or subtracted from the delay between each retry relative to total delay (between 0.0 and 1.0). If string type, absolute maximum amount of random time added or subtracted from the delay between each retry (ISO 8601 duration format) | float or string | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "name": "TimeoutRetryStrat",
   "delay": "PT2M",
   "maxAttempts": 3,
   "jitter": "PT0.001S"
}
```

</td>
<td valign="top">

```yaml
name: TimeoutRetryStrat
delay: PT2M
maxAttempts: 3
jitter: PT0.001S
```

</td>
</tr>
</table>

</details>

Defines the states retry policy (strategy). This is an explicit definition and can be reused across multiple 
defined workflow state errors.

The `name` property specifies the unique name of the retry definition (strategy). This unique name 
can be refered by workflow states [error definitions](#Error-Definition).

The `delay` property specifies the time delay between retry attempts (ISO 8601 duration format).

The `multiplier` property specifies the value by which the interval time is increased for each of the retry attempts.
To explain this better, let's say we have the following retry definition:

```json
{
  "name": "Timeout Errors Strategy",
  "delay": "PT1M",
  "multiplier": "PT2M",
  "maxAttempts": 4
}
```
which means that we will retry up to 4 times after waiting with increasing delay between attempts; 
in this example 1, 3 (1 + 2), 5 (1 + 2 + 2), and 7 (1 + 2 + 2 + 2) minutes between retries.  

The `maxAttempts` property determines the maximum number of retry attempts allowed and is a positive integer value.

The `jitter` property is important to prevent certain scenarios where clients
are retrying in sync, possibly causing or contributing to a transient failure
precisely because they're retrying at the same time. Adding a typically small,
bounded random amount of time to the period between retries serves the purpose
of attempting to prevent these retries from happening simultaneously, possibly
reducing total time to complete requests and overall congestion. How this value
is used in the exponential backoff algorithm is left up to implementations.

`jitter` may be specified as a percentage relative to the total delay. 
For example, if `interval` is 2 seconds, `multiplier` is 2 seconds and we're at
the third attempt, there will be a delay of 6 seconds. If we set `jitter` to
0.3, then a random amount of time between 0 and 1.8 (`totalDelay * jitter == 6 * 0.3`)
will be added or subtracted from the delay.

Alternatively, `jitter` may be defined as an absolute value specified as an ISO
8601 duration. This way, the maximum amount of random time added is fixed and
will not increase as new attempts are made.

For more information, refer to the [Workflow Error Handling](#Workflow-Error-Handling) sections.

#### Transition Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| expression | JsonPath expression. Must return a result for the transition to be valid | string | no |
| compensateBefore | If set to `true`, triggers workflow compensation when before this transition is taken. Default is `false` | boolean | no |
| produceEvents | Array of [producedEvent](#ProducedEvent-Definition) definitions. Events to be produced when this transition happens | array | no |
| [nextState](#Transitions) | State to transition to next | string | yes |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "expression": "{{ $.result }}",
   "produceEvents": [{
       "eventRef": "produceResultEvent",
       "data": "{{ $.result.data }}"
   }],
   "nextState": "EvalResultState"
}
```

</td>
<td valign="top">

```yaml
expression: "{{ $.result }}"
produceEvents:
- eventRef: produceResultEvent
  data: "{{ $.result.data }}"
nextState: EvalResultState
```

</td>
</tr>
</table>

</details>

Transitions allow you to move from one state (control-logic block) to another. For more information see the
[Transitions section](#Transitions) section.

#### Operation State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id |  Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| actionMode | Should actions be performed sequentially or in parallel | string | no |
| [actions](#Action-Definition) | Actions to be performed | array | yes |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after all the actions have been performed | object | yes (if end is not defined) |
| [dataInputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data input adheres to | string | no |
| [dataOutputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | Is this state an end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "name": "RejectApplication",
    "type": "operation",
    "actionMode": "sequential",
    "actions": [
        {
            "functionRef": {
                "refName": "sendRejectionEmailFunction",
                "parameters": {
                    "applicant": "{{ $.customer }}"
                }
            }
        }
    ],
    "end": {
        "kind": "default"
    }
}
```

</td>
<td valign="top">

```yaml
name: RejectApplication
type: operation
actionMode: sequential
actions:
- functionRef:
    refName: sendRejectionEmailFunction
    parameters:
      applicant: "{{ $.customer }}"
end:
  kind: default
```

</td>
</tr>
</table>

</details>

Operation state defines a set of actions to be performed in sequence or in parallel.
Once all actions have been performed, a transition to another state can occur.

#### Switch State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| [dataConditions](#switch-state-dataconditions) or [eventConditions](#switch-state-eventconditions) | Defined if the Switch state evaluates conditions and transitions based on state data, or arrival of events. | array | yes (one) |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| eventTimeout | If eventConditions is used, defines the time period to wait for events (ISO 8601 format). For example: "PT15M" (15 minutes), or "P2DT3H4M" (2 days, 3 hours and 4 minutes)| string | yes only if eventConditions is defined |
| default | Default transition of the workflow if there is no matching data conditions or event timeout is reached. Can be a transition or end definition | object | yes |
| [dataInputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data input adheres to | string | no |
| [dataOutputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
     "name":"CheckVisaStatus",
     "type":"switch",
     "start": {
        "kind": "default"
     },
     "eventConditions": [
        {
          "eventRef": "visaApprovedEvent",
          "transition": {
            "nextState": "HandleApprovedVisa"
          }
        },
        {
          "eventRef": "visaRejectedEvent",
          "transition": {
            "nextState": "HandleRejectedVisa"
          }
        }
     ],
     "eventTimeout": "PT1H",
     "default": {
        "transition": {
          "nextState": "HandleNoVisaDecision"
        }
     }
}
```

</td>
<td valign="top">

```yaml
name: CheckVisaStatus
type: switch
start:
  kind: default
eventConditions:
- eventRef: visaApprovedEvent
  transition:
    nextState: HandleApprovedVisa
- eventRef: visaRejectedEvent
  transition:
    nextState: HandleRejectedVisa
eventTimeout: PT1H
default:
  transition:
    nextState: HandleNoVisaDecision
```

</td>
</tr>
</table>

</details>

Switch states can be viewed as workflow gateways: they can direct transitions of a workflow based on certain conditions.
There are two types of conditions for switch states:
* [Data-based conditions](#switch-state-dataconditions)
* [Event-based conditions](#switch-state-eventconditions)

These are exclusive, meaning that a switch state can define one or the other condition type, but not both.

At times multiple defined conditions can be evaluated to `true` by runtime implementations.
Conditions defined first take precedence over conditions defined later. This is backed by the fact that arrays/sequences
are orderd in both JSON and YAML. For example, let's say there are two `true` conditions: A and B, defined in that order.
Because A was defined first, its transition will be executed, not B's.

In case of data-based conditions definition, switch state controls workflow transitions based on the states data.
If no defined conditions can be matched, the state transitions is taken based on the `default` property.
This property can be either a `transition` to another workflow state, or an `end` definition meaning a workflow end.

For event-based conditions, a switch state acts as a workflow wait state. It halts workflow execution 
until one of the referenced events arrive, then making a transition depending on that event definition.
If events defined in event-based conditions do not arrive before the states `eventTimeout` property expires, 
 state transitions are based on the defined `default` property.

#### <a name="switch-state-dataconditions"></a>Switch State: Data Conditions

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Data condition name | string | no |
| condition | JsonPath expression evaluated against state data. True if results are not empty | string | yes |
| [transition](#Transitions) or [end](#End-Definition) | Defines what to do if condition is true. Transition to another state, or end workflow | object | yes |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
      "name": "Eighteen or older",
      "condition": "{{ $.applicants[?(@.age >= 18)] }}",
      "transition": {
        "nextState": "StartApplication"
      }
}
```

</td>
<td valign="top">

```yaml
name: Eighteen or older
condition: "{{ $.applicants[?(@.age >= 18)] }}"
transition:
  nextState: StartApplication
```

</td>
</tr>
</table>

</details>

Switch state data conditions specify a data-based condition statement, which causes a transition to another 
workflow state if evaluated to true.
The `condition` property of the condition defines a JsonPath expression (e.g., `$.applicants[?(@.age >= 18)]`), which selects
parts of the state data input. The condition is evaluated as "true" if it results a non-empty result.

If the condition is evaluated to "true", you can specify either the `transition` or `end` definitions
to decide what to do, transition to another workflow state, or end workflow execution.

#### <a name="switch-state-eventconditions"></a>Switch State: Event Conditions

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Event condition name | string | no |
| eventRef | References an unique event name in the defined workflow events | string | yes |
| [transition](#Transitions) or [end](#End-Definition) | Defines what to do if condition is true. Transition to another state, or end workflow | object | yes |
| [eventDataFilter](#event-data-filter) | Event data filter definition | object | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
      "name": "Visa approved",
      "eventRef": "visaApprovedEvent",
      "transition": {
        "nextState": "HandleApprovedVisa"
      }
}
```

</td>
<td valign="top">

```yaml
name: Visa approved
eventRef: visaApprovedEvent
transition:
  nextState: HandleApprovedVisa
```

</td>
</tr>
</table>

</details>

Switch state event conditions specify events, which the switch state must wait for. Each condition
can reference one workflow-defined event. Upon arrival of this event, the associated transition is taken.
The `eventRef` property references a name of one of the defined workflow events. 

If the referenced event is received, you can specify either the `transition` or `end` definitions
to decide what to do, transition to another workflow state, or end workflow execution.

The `eventDataFilter` property can be used to filter event when it is received.

#### Delay State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name |State name | string | yes |
| type |State type | string | yes |
| timeDelay |Amount of time (ISO 8601 format) to delay when in this state. For example: "PT15M" (delay 15 minutes), or "P2DT3H4M" (delay 2 days, 3 hours and 4 minutes) | integer | yes |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after the delay | object | yes (if end is not defined) |
| [dataInputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data input adheres to | string | no |
| [dataOutputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) |If this state an end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
      "name": "WaitForCompletion",
      "type": "delay",
      "timeDelay": "PT5S",
      "transition": {
        "nextState":"GetJobStatus"
      }
}
```

</td>
<td valign="top">

```yaml
name: WaitForCompletion
type: delay
timeDelay: PT5S
transition:
  nextState: GetJobStatus
```

</td>
</tr>
</table>

</details>

Delay state waits for a certain amount of time before transitioning to a next state. The amount of delay is specified by the `timeDelay` property in ISO 8601 format.

#### Parallel State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| [branches](#parallel-state-branch) | List of branches for this parallel state| array | yes |
| completionType | Option types on how to complete branch execution. | enum | no |
| n | Used when branchCompletionType is set to `n_of_m` to specify the `n` value. | string or integer | no |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after all branches have completed execution | object | yes (if end is not defined) |
| dataInputSchema | URI to JSON Schema that state data input adheres to | string | no |
| dataOutputSchema | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | If this state and end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
 {  
     "name":"ParallelExec",
     "type":"parallel",
     "start": {
       "kind": "default"
     },
     "completionType": "and",
     "branches": [
        {
          "name": "Branch1",
          "actions": [
            {
                "functionRef": {
                    "refName": "functionNameOne",
                    "parameters": {
                        "order": "{{ $.someParam }}"
                    }
                }
            }
        ]
        },
        {
          "name": "Branch2",
          "actions": [
              {
                  "functionRef": {
                      "refName": "functionNameTwo",
                      "parameters": {
                          "order": "{{ $.someParam }}"
                      }
                  }
              }
          ]
        }
     ],
     "end": {
       "kind": "default"
     }
}
```

</td>
<td valign="top">

```yaml
name: ParallelExec
type: parallel
start:
  kind: default
completionType: and
branches:
- name: Branch1
  actions:
  - functionRef:
      refName: functionNameOne
      parameters:
        order: "{{ $.someParam }}"
- name: Branch2
  actions:
  - functionRef:
      refName: functionNameTwo
      parameters:
        order: "{{ $.someParam }}"
end:
  kind: default
```

</td>
</tr>
</table>

</details>

Parallel state defines a collection of `branches` that are executed in parallel.

Branches contain set of actions that need to be performed or a reference to another workflow that needs to be executed. 

The "completionType" enum specifies the different ways of completing branch execution:
* and: All branches must complete execution before state can perform its transition
* xor: State can transition when one of the branches completes execution
* n_of_m: State can transition once `n` number of branches have completed execution. In this case you should also
specify the `n` property to define this number.

Exceptions may occur during execution of branches of the Parallel state, this is described in detail in [this section](#parallel-state-exceptions).

#### <a name="parallel-state-branch"></a>Parallel State: Branch

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Branch name | string | yes |
| [actions](#Action-Definition) | Actions to be executed in this branch | array | yes if workflowId is not defined |
| workflowId | Unique Id of a workflow to be executed in this branch | string | yes if actions is not defined |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
      "name": "Branch1",
      "actions": [
          {
              "functionRef": {
                  "refName": "functionNameOne",
                  "parameters": {
                      "order": "{{ $.someParam }}"
                  }
              }
          },
          {
              "functionRef": {
                  "refName": "functionNameTwo",
                  "parameters": {
                      "order": "{{ $.someParamTwo }}"
                  }
              }
          }
      ]
}
```

</td>
<td valign="top">

```yaml
name: Branch1
actions:
- functionRef:
    refName: functionNameOne
    parameters:
      order: "{{ $.someParam }}"
- functionRef:
    refName: functionNameTwo
    parameters:
      order: "{{ $.someParamTwo }}"
```

</td>
</tr>
</table>

</details>

Each branch receives the same copy of the Parallel state's data input.

A branch can define either actions or a workflow id of the workflow that needs to be executed.
The workflow id defined cannot be the same id of the workflow there the branch is defined.

#### <a name="parallel-state-exceptions"></a>Parallel State: Handling Exceptions

Exceptions can occur during execution of Parallel state branches.

By default, exceptions that are not handled within branches stop branch execution and are propagated 
to the Parallel state and should be handled with its `onErrors` definition.

If the parallel states branch defines actions, all exceptions that arise from executing these actions
 are propagated to the parallel state 
and can be handled with the parallel states `onErrors` definition.

If the parallel states defines a `workflowId`, exceptions that occur during execution of the called workflow
can chose to handle exceptions on their own. All unhandled exceptions from the called workflow
execution however are propagated back to the parallel state and can be handled with the parallel states
`onErrors` definition.

Note that once an error that is propagated to the parallel state from a branch and handled by the 
states `onErrors` definition is handled (its associated transition is taken) no further errors from branches of this 
parallel state should be considered as the workflow control flow logic has already moved to a different state. 

For more information, see the [Workflow Error Handling](#Workflow-Error-Handling) sections.

#### SubFlow State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name |State name | string | yes |
| type |State type | string | yes |
| waitForCompletion | If workflow execution must wait for sub-workflow to finish before continuing | boolean | yes |
| workflowId |Sub-workflow unique id | boolean | no |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after subflow has completed | object | yes (if end is not defined) |
| dataInputSchema | URI to JSON Schema that state data input adheres to | string | no |
| dataOutputSchema | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | If this state and end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "name": "HandleApprovedVisa",
    "type": "subflow",
    "workflowId": "handleApprovedVisaWorkflowID",
    "end": {
      "kind": "default"
    }
}
```

</td>
<td valign="top">

```yaml
name: HandleApprovedVisa
type: subflow
workflowId: handleApprovedVisaWorkflowID
end:
  kind: default
```

</td>
</tr>
</table>

</details>

Often you want to group your workflows into small logical units that solve a particular business problem and can be reused in 
multiple other workflow definitions.

<p align="center">
<img src="media/spec/subflowstateref.png" height="350px" alt="Referencing reusable workflow via SubFlow states"/>
</p>

Reusable workflow are referenced by their `id` property via the SubFlow states`workflowId` parameter.

Each referenced workflow receives the SubFlow states data as workflow data input.

The `waitForCompletion` property defines if the SubFlow state should wait until the referenced reusable workflow
has completed its execution. 

If `waitForCompletion` is "true", SubFlow state execution must wait until the referenced workflow has completed its execution.
In this case the workflow data output of the referenced workflow can and should be merged with the SubFlow states state data.

If `waitForCompletion` is set to "false" the parent workflow can continue its execution while the referenced workflow 
is being executed. For this case, the referenced (child) workflow data output cannot be merged with the SubFlow states
state data (as by the time its completion the parent workflow execution has already continued).

Referenced workflows must declare their own [function](#Function-Definition) and [event](#Event-Definition) definitions.

#### Inject State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| data | JSON object which can be set as state's data input and can be manipulated via filter | object | yes |
| [stateDataFilter](#state-data-filter) | State data filter | object | no |
| [transition](#Transitions) | Next transition of the workflow after subflow has completed | object | yes (if end is set to false) |
| dataInputSchema | URI to JSON Schema that state data input adheres to | string | no |
| dataOutputSchema | URI to JSON Schema that state data output adheres to | string | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | If this state and end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
     "name":"Hello",
     "type":"inject",
     "start": {
       "kind": "default"
     },
     "data": {
        "result": "Hello"
     },
     "transition": {
       "nextState": "World"
     }
}
```

</td>
<td valign="top">

```yaml
name: Hello
type: inject
start:
  kind: default
data:
  result: Hello
transition:
  nextState: World
```

</td>
</tr>
</table>

</details>

Inject state can be used to inject static data into state data input. Inject state does not perform any actions.
It is very useful for debugging, for example, as you can test/simulate workflow execution with pre-set data that would typically
be dynamic in nature (e.g., function calls, events).

The inject state `data` property allows you to statically define a JSON object which gets added to the states data input.
You can use the filter property to control the states data output to the transition state.

Here is a typical example of how to use the inject state to add static data into its states data input, which then is passed
as data output to the transition state:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

  ```json
  {  
   "name":"SimpleInjectState",
   "type":"inject",
   "data": {
      "person": {
        "fname": "John",
        "lname": "Doe",
        "address": "1234 SomeStreet",
        "age": 40
      }
   },
   "transition": {
      "nextState": "GreetPersonState"
   }
  }
  ```

</td>
<td valign="top">

```yaml
  name: SimpleInjectState
  type: inject
  data:
    person:
      fname: John
      lname: Doe
      address: 1234 SomeStreet
      age: 40
  transition:
    nextState: GreetPersonState
```

</td>
</tr>
</table>

The data output of the "SimpleInjectState" which then is passed as input to the transition state would be:

```json
{
 "person": {
      "fname": "John",
      "lname": "Doe",
      "address": "1234 SomeStreet",
      "age": 40
 }
}

```

If the inject state already receives a data input from the previous transition state, the inject data should be merged
with its data input.

You can also use the filter property to filter the state data after data is injected. Let's say we have:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
  {  
     "name":"SimpleInjectState",
     "type":"inject",
     "data": {
        "people": [
          {
             "fname": "John",
             "lname": "Doe",
             "address": "1234 SomeStreet",
             "age": 40
          },
          {
             "fname": "Marry",
             "lname": "Allice",
             "address": "1234 SomeStreet",
             "age": 25
          },
          {
             "fname": "Kelly",
             "lname": "Mill",
             "address": "1234 SomeStreet",
             "age": 30
          }
        ]
     },
     "stateDataFilter": {
        "dataOutputPath": "{{ $.people[?(@.age < 40)] }}"
     },
     "transition": {
        "nextState": "GreetPersonState"
     }
    }
```

</td>
<td valign="top">

```yaml
  name: SimpleInjectState
  type: inject
  data:
    people:
    - fname: John
      lname: Doe
      address: 1234 SomeStreet
      age: 40
    - fname: Marry
      lname: Allice
      address: 1234 SomeStreet
      age: 25
    - fname: Kelly
      lname: Mill
      address: 1234 SomeStreet
      age: 30
  stateDataFilter:
    dataOutputPath: "{{ $.people[?(@.age < 40)] }}"
  transition:
    nextState: GreetPersonState
```

</td>
</tr>
</table>

In which case the states data output would include people whose age is less than 40.
You can change your output path easily during testing, for example:

```text
$.people[?(@.age >= 40)]
```

This allows you to test if your workflow behaves properly for cases when there are people whose age is greater or equal 40.

#### ForEach State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| inputCollection | JsonPath expression selecting an array element of the states data | string | yes |
| outputCollection | JsonPath expression specifying an array element of the states data to add the results of each iteration | string | no |
| iterationParam | Name of the iteration parameter that can be referenced in actions/workflow. For each parallel iteration, this param should contain an unique element of the inputCollection array | string | yes |
| max | Specifies how upper bound on how many iterations may run in parallel | string or integer | no |
| [actions](#Action-Definition) | Actions to be executed for each of the elements of inputCollection | array | yes if subflowId is not defined |
| workflowId | Unique Id of a workflow to be executed for each of the elements of inputCollection | string | yes if actions is not defined |
| [stateDataFilter](#state-data-filter) | State data filter definition | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after state has completed | object | yes (if end is not defined) |
| dataInputSchema | URI to JSON Schema that state data input adheres to | string | no |
| dataOutputSchema | URI to JSON Schema that state data output adheres to | string | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | Is this state an end state | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "name": "ProvisionOrdersState",
    "type": "foreach",
    "start": {
       "kind": "default"
    },
    "inputCollection": "{{ $.orders }}",
    "iterationParam": "singleorder",
    "outputCollection": "{{ $.provisionresults }}",
    "actions": [
        {
            "functionRef": {
                "refName": "provisionOrderFunction",
                "parameters": {
                    "order": "{{ $.singleorder }}"
                }
            }
        }
    ]
}
```

</td>
<td valign="top">

```yaml
name: ProvisionOrdersState
type: foreach
start:
  kind: default
inputCollection: "{{ $.orders }}"
iterationParam: "singleorder"
outputCollection: "{{ $.provisionresults }}"
actions:
- functionRef:
    refName: provisionOrderFunction
    parameters:
      order: "{{ $.singleorder }}"
```

</td>
</tr>
</table>

</details>

ForEach states can be used to execute a defined actions, or execute a sub-workflow for each element of a data array.
Each iteration of the ForEach state should be executed in parallel.

You can use the `max` property to set the upper bound on how many iterations may run in parallel. The default
of the `max` property is zero, which places no limit on number of parallel executions.

The `inputCollection` property is a JsonPath expression which selects an array in the states data. All iterations 
of are done against the data elements of this array. This array must exist.

The `outputCollection` property is a JsonPath expression which selects an array in the state data where the results
of each iteration should be added to. If this array does not exist, it should be created.

The `iterationParam` property defines the name of the iteration parameter passed to each parallel execution of the foreach state.
It should contain the unique element of the `inputCollection` array and passed as data input to the actions/workflow defined.
`iterationParam` should be created for each iteration, so it can be referenced/used in defined actions / workflow data input.

The `actions` property defines actions to be executed in each state iteration.

If actions are not defined, you can specify the `workflowid` to reference a workflow id which needs to be executed
for each iteration. Note that `workflowid` should not be the same as the workflow id of the workflow wher the foreach state
is defined.

Let's take a look at an example:

In this example the data input to our workflow is an array of orders:

```json
{
    "orders": [
        {
            "orderNumber": "1234",
            "completed": true,
            "email": "firstBuyer@buyer.com"
        },
        {
            "orderNumber": "5678",
            "completed": true,
            "email": "secondBuyer@buyer.com"
        },
        {
            "orderNumber": "9910",
            "completed": false,
            "email": "thirdBuyer@buyer.com"
        }
    ]
}
```

and our workflow is defined as:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
  "id": "sendConfirmWorkflow",
  "name": "SendConfirmationForCompletedOrders",
  "version": "1.0",
  "functions": [
  {
    "name": "sendConfirmationFunction",
    "operation": "file://confirmationapi.json#sendOrderConfirmation"
  }
  ],
  "states": [
  {
      "start": { "kind":  "default" },
      "name":"SendConfirmState",
      "type":"foreach",
      "inputCollection": "{{ $.orders[?(@.completed == true)] }}",
      "iterationParam": "completedorder",
      "outputCollection": "{{ $.confirmationresults }}",
      "actions":[  
      {  
       "functionRef": {
         "refName": "sendConfirmationFunction",
         "parameters": {
           "orderNumber": "{{ $.completedorder.orderNumber }}",
           "email": "{{ $.completedorder.email }}"
         }
       }
      }],
      "end": { "kind": "default" }
  }]
}
```

</td>
<td valign="top">

```yaml
id: sendConfirmWorkflow
name: SendConfirmationForCompletedOrders
version: '1.0'
functions:
- name: sendConfirmationFunction
  operation: file://confirmationapi.json#sendOrderConfirmation
states:
- start:
    kind: default
  name: SendConfirmState
  type: foreach
  inputCollection: "{{ $.orders[?(@.completed == true)] }}"
  iterationParam: completedorder
  outputCollection: "{{ $.confirmationresults }}"
  actions:
  - functionRef:
      refName: sendConfirmationFunction
      parameters:
        orderNumber: "{{ $.completedorder.orderNumber }}"
        email: "{{ $.completedorder.email }}"
  end:
    kind: default
```

</td>
</tr>
</table>

The workflow data input containing order information is passed to the `SendConfirmState` foreach state.
The foreach state defines an `inputCollection` property which selects all orders that have the `completed` property set to `true`.

For each element of the array selected by `inputCollection` a JSON object defined by `iterationParam` should be
created containing an unique element of `inputCollection` and passed as the data input to the parallel executed actions.

So for this example, we would have two parallel executions of the `sendConfirmationFunction`, the first one having data:

```json
{
    "completedorder": {
        "orderNumber": "1234",
        "completed": true,
        "email": "firstBuyer@buyer.com"
    }
}
```

and the second:

```json
{
    "completedorder": {
        "orderNumber": "5678",
        "completed": true,
        "email": "secondBuyer@buyer.com"
    }
}
```

The results of each parallel action execution are stored as elements in the state data array defined by the `outputCollection` property.

#### Callback State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| [action](#Action-Definition) | Defines the action to be executed | object | yes |
| eventRef | References an unique callback event name in the defined workflow [events](#Event-Definition) | string | yes |
| [timeout](#eventstate-timeout) | Time period to wait from when action is executed until the callback event is received (ISO 8601 format). For example: "PT15M" (wait 15 minutes), or "P2DT3H4M" (wait 2 days, 3 hours and 4 minutes)| string | yes |
| [eventDataFilter](#event-data-filter) | Callback event data filter definition | object | no |
| [stateDataFilter](#state-data-filter) | State data filter definition | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [dataInputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data input adheres to | string | no |
| [dataOutputSchema](#Information-Passing-Between-States) | URI to JSON Schema that state data output adheres to | string | no |
| [transition](#Transitions) | Next transition of the workflow after callback event has been received | object | yes |
| [start](#Start-Definition) | Is this state a starting state | object | no |
| [end](#End-Definition) | Is this state an end state | object | no |
| [compensatedBy](#Workflow-Compensation) | Uniaue name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
        "name": "CheckCredit",
        "type": "callback",
        "start": {
           "kind": "default"
        },
        "action": {
            "functionRef": {
                "refName": "callCreditCheckMicroservice",
                "parameters": {
                    "customer": "{{ $.customer }}"
                }
            }
        },
        "eventRef": "CreditCheckCompletedEvent",
        "timeout": "PT15M",
        "transition": {
            "nextState": "EvaluateDecision"
        }
}
```

</td>
<td valign="top">

```yaml
name: CheckCredit
type: callback
start:
  kind: default
action:
  functionRef:
    refName: callCreditCheckMicroservice
    parameters:
      customer: "{{ $.customer }}"
eventRef: CreditCheckCompletedEvent
timeout: PT15M
transition:
  nextState: EvaluateDecision
```

</td>
</tr>
</table>

</details>

Serverless orchestration can at times require manual steps/decisions to be made. While some work performed
in a serverless workflow can be executed automatically, some decisions must involve manual steps (e.g., human decisions).
The Callback state allows you to explicitly model manual decision steps during workflow execution.

The action property defines a function call that triggers an external activity/service. Once the action executes,
the callback state will wait for a CloudEvent (defined via the `eventRef` property), which indicates the completion
of the manual decision by the called service.

Note that the called decision service is responsible for emitting the callback CloudEvent indicating the completion of the
decision and including the decision results as part of the event payload. This event must be correlated to the
workflow instance using the callback events context attribute defined in the `correlation` property of the
referenced [Event Definition](#Event-Definition).

Once the completion (callback) event is received, the Callback state completes its execution and transitions to the next
defined workflow state or completes workflow execution in case it is an end state.

The callback event payload is merged with the Callback state data and can be filtered via the "eventDataFilter" definition.

The Callback state `timeout` property defines a time period from the action execution until the callback event should be received.

If the defined callback event has not been received during this time period, the state should transition to the next state or end workflow execution if it is an end state.

#### Start Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| kind | End kind ("default", "scheduled") | enum | yes |
| [schedule](#Schedule-Definition) | If kind is "scheduled", define time/repeating intervals at which workflow instances can/should be started | object | yes only if kind is "scheduled" |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
  "kind": "scheduled",
  "schedule": {
    "interval": "2020-03-20T09:00:00Z/2020-03-20T15:00:00Z"
  }
}
```

</td>
<td valign="top">

```yaml
kind: scheduled
schedule:
  interval: 2020-03-20T09:00:00Z/2020-03-20T15:00:00Z
```

</td>
</tr>
</table>

</details>

Any state can declare to be the start state of the workflow, meaning that when a workflow instance is created it will be the initial
state to be executed. A workflow definition can declare one workflow start state.

The start definition provides a `kind` property, which describes the starting options:

- **default** - The start state is always "active" and there are no restrictions imposed on its execution.
- **scheduled** - Scheduled start states have two different choices. You can define time-based intervals during which workflow instances are **allowed**
to be created, or cron-based repeating times at which a workflow instance **should** be created. 

Defining a schedule for the start definition allows you to set time intervals during which workflow instances can be created, or 
periodic times at which workflow instance should be created.  

One use case for the interval-based schedule is the following. Let's say
we have a workflow that orchestrates an online auction and should be "available" only from when the auction starts until it ends. 
Customer bids should only be allowed during this time interval. Bids made before or after the defined time interval should not be allowed.

There are two cases to discuss when dealing with interval-based scheduled starts:

1. **Starting States in [Parallel](#Parallel-State) state [branches](#parallel-state-branch)**: if a state in a parallel state branch defines a scheduled start state that is not "active" at the time the branch is executed, the parent workflow should not wait until it becomes active, and just complete the execution of the branch.
2. **Starting states in [SubFlow](#SubFlow-State) states**: if a state in a workflow definition (referenced by SubFlow state) defines a scheduled start state that is not "active" at the time the SubFlow state is executed, the parent workflow should not wait until it becomes active, and just complete the execution of the SubFlow state.

You can also define cron-based scheduled starts, which allow to define periodically started workflow instances based on a [cron](http://crontab.org/) definition.
Cron-based scheduled starts can handle absolute time intervals (i.e., not calculated in respect to some particular point in time).
One use case for cron-based scheduled starts is a workflow that performs periodical data batch processing. 
In this case we could use a cron definition

``` text
0 0/5 * * * ?
```

to define that this workflow should be triggered every 5 minutes, starting at full hour. 

Here are some more examples of cron expressions and their meanings:

``` text
* * * * *   - Trigger workflow instance at the top of every minute
0 * * * *   - Trigger workflow instance at the top of every hour
0 */2 * * * - Trigger workflow instance every 2 hours
0 9 8 * *   - Trigger workflow instance at 9:00:00AM on the eighth day of every month
```

[See here](http://crontab.org/) to get more information on defining cron expressions.

One thing to discuss when dealing with cron-based scheduled starts is when the starting state of the workflow is an [Event](#Event-State).
Event states define that workflow instances are triggered by the existence of the defined event(s). 
Defining a cron-based scheduled starts for the runtime implementations would mean that there needs to be an event service that issues 
the needed events at the defined times to trigger workflow instance creation.

#### Schedule Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| interval | Time interval describing when the workflow starting state is active. (ISO 8601 time interval format). | string | yes if `cron` not defined |
| cron | Repeating interval (cron expression) describing when the workflow starting state should be triggered | string | yes if `interval` not defined |
| directInvoke | Define if workflow instances can be created outside of the defined interval/cron | enum | yes |
| timezone | Timezone name (for example "America/Los_Angeles") used to evaluate the cron expression against. Not used for `interval` property as timezone can be specified there directly. If not specified, should default to local machine timezone | string | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "cron": "0 0/15 * * * ?",
   "directInvoke": "allow"
}
```

</td>
<td valign="top">

```yaml
cron: 0 0/15 * * * ?
directInvoke: allow
```

</td>
</tr>
</table>

</details>

The interval property uses the ISO 8601 time interval format to describe when the starting state is active.
There is a number of ways to express the time interval:

1. **Start** + **End**: Defines the start and end time, for example "2020-03-20T13:00:00Z/2021-05-11T15:30:00Z", meaning this start state is active
from March 20th 2020 at 1PM UTC until May 11th 2021 at 3:30pm UTC.
2. **Start** + **Duration**: Defines the start time and the duration, for example: "2020-03-20T13:00:00Z/P1Y2M10DT2H30M", meaning this start state is active
from March 20th 2020 at 1pm UTC and will stay active for 1 year, 2 months, 10 days 2 hours and 30 minutes.
3. **Duration** + **End**: Defines the duration and an end, for example: "P1Y2M10DT2H30M/2020-05-11T15:30:00Z", meaning that this start state is active for
1 year, 2 months, 10 days 2 hours and 30 minutes, or until May 11th 2020 at 3:30PM UTC, whichever comes first.
4. **Duration**: Defines the duration only, for example: ""P1Y2M10DT2H30M"", meaning this start state is active for 1 year, 2 months, 10 days 2 hours and 30 minutes.
Implementations have to provide the context in this case on when the duration should start to be counted, as it may be the workflow deployment time or the first time this workflow instance is created, for example.

A case to consider here is when an [Event](#Event-State) state is also a workflow start state and the schedule definition is defined. Let's say we have a starting exclusive [Event](#Event-State) state
that waits to consume event "X", meaning that the workflow instance should be created when event "X" occurs. If we also define
a specific interval in the start schedule definition, the "waiting" for event "X" should only be started when the starting state becomes active.

Once a workflow instance is created, the start state schedule can be ignored for that particular workflow instance. States should from then on rely on their timeout properties, for example, to restrict the waiting time of incoming events, function executions, etc.  

The `cron` property uses a [cron expression](http://crontab.org/) 
to describe a repeating interval upon which the state becomes active and a new workflow instance is created.

The `timezone` property is used to define a time zone name to evaluate the cron expression against. If not specified, it should default to the local
machine time zone. See [here](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) for a list of timezone names.

Note that when the starting state of the workflow is an [Event](#Event-State) 
defining cron-based scheduled starts for the runtime implementations would mean that there needs to be an event service that issues 
the needed events at the defined times to trigger workflow instance creation.

The `directInvoke` property defines if workflow instances are allowed to be created outside of the defined interval or cron expression.

#### End Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| kind | End kind ("default", "terminate", or "event") | enum | yes |
| produceEvents | Array of [producedEvent](#ProducedEvent-Definition) definitions. If `kind` is "event", define what type of event to produce | array | yes only if `kind` is "event" |
| compensateBefore | If set to `true`, triggers workflow compensation when before workflow executin completes. Default is `false` | boolean | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "kind": "event",
    "produceEvents": [{
        "eventRef": "provisioningCompleteEvent",
        "data": "{{ $.provisionedOrders }}"
    }]
}
```

</td>
<td valign="top">

```yaml
kind: event
produceEvents:
- eventRef: provisioningCompleteEvent
  data: "{{ $.provisionedOrders }}"
```

</td>
</tr>
</table>

</details>

Any state with the exception of the [Switch](#Switch-State) state can declare to be the end state of the workflow, meaning that after the execution of this state is completed, workflow execution ends. Switch states require a transition to happen after their execution, thus cannot be workflow end states.

The end definitions provides different ways to complete workflow execution, which is set by the `kind` property:

- **default** - Default workflow execution completion; no other special behavior.
- **terminate** - Completes all execution flows in the given workflow instance. All activities/actions being executed
are completed. If a terminate end is reached inside a ForEach, Parallel, or SubFlow state, the entire workflow instance is terminated.
- **event** - Workflow executions completes, and one or moreCloudEvents can be produced.

#### ProducedEvent Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| eventRef | Reference to a defined unique event name in the [events](#Event-Definition) definition | string | yes |
| data | If string type, an expression which selects parts of the states data output to become the data (payload) of the produced event. If object type, a custom object to become the data (payload) of produced event. | string or object | no |
| contextAttributes | Add additional event extension context attributes | object | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "eventRef": "provisioningCompleteEvent",
    "data": "{{ $.provisionedOrders }}",
    "contextAttributes": [{
         "buyerId": "{{ $.buyerId }}"
     }]
 }
```

</td>
<td valign="top">

```yaml
eventRef: provisioningCompleteEvent
data: "{{ $.provisionedOrders }}"
contextAttributes:
- buyerId: "{{ $.buyerId }}"
```

</td>
</tr>
</table>

</details>

Defines the event (CloudEvent format) to be produced when workflow execution completes or during a workflow [transitions](#Transitions). 
The `eventRef` property must match the name of
one of the defined `produced` events in the [events](#Event-Definition) definition.

The `data` property can have two types, object or string. If of string type, it is an expression that can select parts of state data
to be used as the event payload. If of object type, you can define a custom object to be the event payload.

The `contextAttributes` property allows you to add one or more [extension context attributes](https://github.com/cloudevents/spec/blob/master/spec.md#extension-context-attributes)
to the generated event. 

Being able to produce events when workflow execution completes or during state transition
allows for event-based orchestration communication.
For example, completion of an orchestration workflow can notify other orchestration workflows to decide if they need to act upon
the produced event, or notify monitoring services of the current state of workflow execution, etc. 
It can be used to create very dynamic orchestration scenarios.

#### Transitions

Serverless workflow states can have one or more incoming and outgoing transitions (from/to other states).
Each state can define a `transition` definition that is used to determine which
state to transition to next.

To define a transition, set the `nextState` property in your transition definitions.

Implementers can choose to use the states `name` property
for determining the transition; however, we realize that in most cases this is not an
optimal solution that can lead to ambiguity. This is why each state also include an "id"
property. Implementers can choose their own id generation strategy to populate the `id` property
for each of the states and use it as the unique state identifier that is to be used as the "nextState" value.

So the options for next state transitions are:

- Use the state name property
- Use the state id property
- Use a combination of name and id properties

Events can be produced during state transitions. The `produceEvents` property of the `transition` definitions allows you
to reference one or more defined `produced` events in the workflow [events definitions](#Event-Definition).
For each of the produced events you can select what parts of state data to be the event payload.

Transitions can trigger compensation via their `compensateBefore` property. See the [Workflow Compensation](#Workflow-Compensation)
section for more information.

#### Restricting Transitions based on state output

In addition to specifying the `nextState` property a transition also defines a boolean expression which must
evaluate to true for the transition to happen. Having this data-based restriction capabilities can help
 stop transitions within workflow execution that can have serious and harmful business impacts.

The expression parameter is a JsonPath expression and is evaluated 
against state data output. It evaluates to true if the expression returns non empty results.

Here is an example of a restricted transition which only allows transition to the "highRiskState" if the
output of the state to transition from includes an user with the title "MANAGER".

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{  
"functions": [
  {
   "name": "doLowRiskOperationFunction",
   "operation": "file://myapi.json#lowRisk"
  },
  {
   "name": "doHighRiskOperationFunction",
   "operation": "file://myapi.json#highRisk"
  }
],
"states":[  
  {  
   "start": {
     "kind": "default"
   },
   "name":"lowRiskState",
   "type":"operation",
   "actionMode":"Sequential",
   "actions":[  
    {  
     "functionRef":{
        "refName": "doLowRiskOperationFunction"
     }
    }
    ],
    "transition": {
      "nextState":"highRiskState",
      "expression": "{{ $.users[?(@.title == 'MANAGER')] }}"
    }
  },
  {  
   "name":"highRiskState",
   "type":"operation",
   "end": {
     "kind": "default"
   },
   "actionMode":"Sequential",
   "actions":[  
    {  
     "functionRef":{
       "refName": "doHighRiskOperationFunction"
     }
    }
   ]
  }
]
}
```

</td>
<td valign="top">

```yaml
functions:
- name: doLowRiskOperationFunction
  operation: file://myapi.json#lowRisk
- name: doHighRiskOperationFunction
  operation: file://myapi.json#highRisk
states:
- start:
    kind: default
  name: lowRiskState
  type: operation
  actionMode: Sequential
  actions:
  - functionRef:
      refName: doLowRiskOperationFunction
  transition:
    nextState: highRiskState
    expression: "{{ $.users[?(@.title == 'MANAGER')] }}"
- name: highRiskState
  type: operation
  end:
    kind: default
  actionMode: Sequential
  actions:
  - functionRef:
      refName: doHighRiskOperationFunction
```

</td>
</tr>
</table>

Implementers should decide how to handle data-based transitions that return false (do not proceed).
The default should be that if this happens workflow execution should halt and a detailed message
 on why the transition failed should be provided.

#### Workflow Data Input

The initial data input into a workflow instance must be a valid [JSON object](https://tools.ietf.org/html/rfc7159#section-4).
If no input is provided the default data input is the empty object:

```json
{

}
```

Workflow data input is passed to the workflow's [start state](#Start-Definition) state as data input.

<p align="center">
<img src="media/spec/workflowdatainput.png" height="350px" alt="Workflow data input"/>
</p>

In order to define the structure of expected workflow data input you can use the workflow
`dataInputSchema` property. This property allows you to link to a [JSON Schema](https://json-schema.org/) definition
that describes the expected workflow data input. This can be used for documentation purposes or implementations may
decide to strictly enforce it.

#### Event Data

[Event states](#Event-State) wait for arrival of defined CloudEvents, and when consumed perform a number of defined actions.
CloudEvents can contain data which is needed to make further orchestration decisions. Data from consumed CloudEvents
can be merged with the data of the Event state, so it can be used inside defined actions
or be passed as data output to transition states.

<p align="center">
<img src="media/spec/eventdatamerged.png" height="350px" alt="Event data merged with state data input"/>
</p>

Similarly for Callback states, the callback event data is merged with the data of the Callback state.

Merging of this data case be controlled via [data filters](#State-Information-Filtering).

#### Action Data

[Event](#Event-State), [Callback](#Callback-State), and [Operation](#Operation-State) states can execute [actions](#Action-Definition). 
Actions can invoke different services (functions). Functions can return results that may be needed to make
further orchestration decisions. Function results can be  merged with the state data.

<p align="center">
<img src="media/spec/actionsdatamerged.png" height="350px" alt="Actions data merged with state data"/>
</p>

Merging of this data case be controlled via [data filters](#State-Information-Filtering).

#### Information Passing Between States

States in a workflow can receive data (data input) as well as produce a data result (data output). The states data input is
typically the previous states data output.
When a state completes its tasks, its data output is passed to the data input of the state it transitions to.

There are two of rules to consider here:

- If the state is the starting state its data input is the [workflow data input](#Workflow-data-input).
- If the state is an end state (`end` property is defined), its data output is the [workflow data output](#Workflow-data-output).  

<p align="center">
<img src="media/spec/basic-state-data-passing.png" height="350px" alt="Basic state data passing"/>
</p>

In order to define the structure of expected state data input and output you can use the workflow
"dataInputSchema" and `dataOutputSchema` properties. These property allows you to link to [JSON Schema](https://json-schema.org/) definitions
that describes the expected workflow data input/output. This can be used for documentation purposes or implementations may
decide to strictly enforce it.

#### State Data Filtering

Data filters allow you to select and extract specific data that is useful and needed during workflow execution.
Filters use [JsonPath](https://github.com/json-path/JsonPath) expressions for extracting portions of state's data
input and output, actions data and results, event data, as well as error data.

There are several types of data filters:

- [State Data Filter](#state-data-filter)
- [Action Data Filter](#action-data-filter)
- [Event Data Filter](#event-data-filter)

All states can define state data filters. States which can consume events can define event data filters, and states
that can perform actions can define action data filters for each of the actions they perform.

#### <a name="state-data-filter"></a> State data filtering - State Data Filter

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| dataInputPath | JsonPath expression to filter the states data input | string | no |
| dataOutputPath | JsonPath expression that filters the states data output | string | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "dataInputPath": "{{ $.orders }}",
    "dataOutputPath": "{{ $.provisionedOrders }}"
}
```

</td>
<td valign="top">

```yaml
dataInputPath: "{{ $.orders }}"
dataOutputPath: "{{ $.provisionedOrders }}"
```

</td>
</tr>
</table>

</details>

State data filters can be used to filter the states data input and output.

The state data filters `dataInputPath` expression is applied when the workflow transitions to the current state and it receives its data input.
It filters this data input selecting parts of it (only the selected data is considered part of the states data during its execution).
If `dataInputPath` is not defined, or it does not select any parts of the states data input, the states data input is not filtered.

The state data filter `dataOutputPath` is applied right before the state transitions to the next state defined. It filters the states data
output to be passed as data input to the transitioning state. If the current state is the workflow end state, the filtered states data
output becomes the workflow data output.
If `dataOutputPath` is not defined, or it does not select any parts of the states data output, the states data output is not filtered.

Let's take a look at some examples of state filters. For our examples the data input to our state is as follows:

```json
{
  "fruits": [ "apple", "orange", "pear" ],
  "vegetables": [
    {
      "veggieName": "potato",
      "veggieLike": true
    },
    {
      "veggieName": "broccoli",
      "veggieLike": false
    }
  ]
}
```

For the first example our state only cares about fruits data, and we want to disregard the vegetables. To do this
we can define a state filter:

```json
{
  "name": "FruitsOnlyState",
  "type": "inject",
  "stateDataFilter": {
    "dataInputPath": "{{ $.fruits }}"
  },
  "transition": {
     "nextState": "someNextState"
  }
}
```

The state data output then would include only the fruits data.

<p align="center">
<img src="media/spec/state-data-filter-example1.png" height="400px" alt="State Data Filter Example"/>
</p>

For our second example let's say that we are interested in only vegetable that are "veggie like".
Here we have two ways of filtering our data, depending on if actions within our state need access to all vegetables, or
only the ones that are "veggie like".
The first way would be to use both "dataInputPath", and "dataOutputPath":

```json
{
  "name": "VegetablesOnlyState",
  "type": "inject",
  "stateDataFilter": {
    "dataInputPath": "{{ $.vegetables }}",
    "dataOutputPath": "{{ $.[?(@.veggieLike)] }}"
  },
  "transition": {
     "nextState": "someNextState"
  }
}
```

The states data input filter selects all the vegetables from the main data input. Once all actions have performed, before the state transition
or workflow execution completion (if this is an end state), the "dataOutputPath" of the state filter selects only the vegetables which are "veggie like".

<p align="center">
<img src="media/spec/state-data-filter-example2.png" height="400px" alt="State Data Filter Example"/>
</p>

The second way would be to directly filter only the "veggie like" vegetables with just the data input path:

```json
{
  "name": "VegetablesOnlyState",
  "type": "inject",
  "stateDataFilter": {
    "dataInputPath": "{{ $.vegetables.[?(@.veggieLike)] }}"
  },
  "transition": {
     "nextState": "someNextState"
  }
}
```

#### <a name="action-data-filter"></a> State data filtering - Action Data Filter

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| dataInputPath | JsonPath expression that filters states data that can be used by the state action | string | no |
| dataResultsPath | JsonPath expression that filters the actions data result, to be added to or merged with the states data | string | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
  "dataInputPath": "{{ $.language }}", 
  "dataResultsPath": "{{ $.payload.greeting }}"
}
```

</td>
<td valign="top">

```yaml
dataInputPath: "{{ $.language }} "
dataResultsPath: "{{ $.payload.greeting }}"
```

</td>
</tr>
</table>

</details>

[Actions](#Action-Definition) have access to the state data. 
They can filter this data using an action data filter 'dataInputPath' expression before invoking functions.
This is useful if you want to restrict the data that can be passed as parameters to functions during action invocation.

Actions can reference [functions](#Function-Definition) that need to be invoked. 
The results of these functions are considered the output of the action which needs to be added to or merged with the states data. 
You can filter the results of actions with the `dataResultsPath` property, to only select
parts of the action results that need to go into the states data.

To give an example, let's say we have an action which returns a list of breads and pasta types.
For our workflow, we are only interested into breads and not the pasta.

Action results:

```json
{
  "breads": ["baguette", "brioche", "rye"],
  "pasta": [ "penne",  "spaghetti", "ravioli"]
}
```

We can use an action data filter to filter only the breads data:

```json
{
"actions":[  
    {  
       "functionRef": {
          "refName": "breadAndPastaTypesFunction"
       },
       "actionDataFilter": {
          "dataResultsPath": "{{ $.breads }}"
       }
    }
 ]
}
```

With this action data filter in place only the bread types returned by the function invocation will be added to or merged
with the state data.

#### <a name="event-data-filter"></a> State data filtering - Event Data Filter

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| dataOutputPath | JsonPath expression that filters of the event data, to be added to or merged with states data | string | no |

<details><summary><strong>Click to view example definition</strong></summary>
<p>

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "dataOutputPath": "{{ $.data.results }}"
}
```

</td>
<td valign="top">

```yaml
dataOutputPath: "{{ $.data.results }}"
```

</td>
</tr>
</table>

</details>

Allows event data to be filtered and added to or merged with the state data. All events have to be in the CloudEvents format
and event data filters can filter both context attributes and the event payload (data) using the `dataOutputPath` property.

Here is an example using an event filter:

<p align="center">
<img src="media/spec/event-data-filter-example1.png" height="400px" alt="Event Data Filter Example"/>
</p>

#### <a name="using-multiple-filters"></a> State data filtering - Using multiple filters

As [Event states](#Event-State) can take advantage of all defined data filters, it is probably the best way to
show how we can combine them all to filter state data.

Let's say we have a workflow which consumes events defining a customer arrival (to your store for example),
and then lets us know how to greet this customer in different languages. We could model this workflow as follows:

```json
{
    "id": "GreetCustomersWorkflow",
    "name": "Greet Customers when they arrive",
    "version": "1.0",
    "events": [{
        "name": "CustomerArrivesEvent",
        "type": "customer-arrival-type",
        "source": "customer-arrival-event-source"
     }],
    "functions": [{
        "name": "greetingFunction",
        "operation": "http://my.api.org/myapi.json#greeting"
    }],
    "states":[
        {
            "start": {
               "kind": "default"
            },
            "name": "WaitForCustomerToArrive",
            "type": "event",
            "onEvents": [{
                "eventRefs": ["CustomerArrivesEvent"],
                "eventDataFilter": {
                    "dataInputPath": "{{ $.customer }}"
                },
                "actions":[
                    {
                        "functionRef": {
                            "refName": "greetingFunction",
                            "parameters": {
                                "greeting": "{{ $.languageGreetings.spanish }} ",
                                "customerName": "{{ $.customer.name }} "
                            }
                        },
                        "actionDataFilter": {
                            "dataResultsPath": "{{ $.finalCustomerGreeting }}"
                        }
                    }
                ]
            }],
            "stateDataFilter": {
                "dataInputPath": "{{ $.hello }} ",
                "dataOutputPath": "{{ $.finalCustomerGreeting }}"
            },
            "end": {
              "kind": "default"
            }
        }
    ]
}
```

The example workflow contains an event state which consumes CloudEvents of type "customer-arrival-type", and then
calls the "greetingFunction" function passing in the greeting in Spanish and the name of the customer to greet.

The workflow data input when starting workflow execution is assumed to include greetings in different languages:

```json
{
  "hello": {
    "english": "Hello",
    "spanish": "Hola",
    "german": "Hallo",
    "russian": "Здравствуйте"
  },
  "goodbye": {
    "english": "Goodbye",
    "spanish": "Adiós",
    "german": "Auf Wiedersehen",
    "russian": "Прощай"
  }
}
```

We also assume for this example that the CloudEvent that our event state is set to consume (has the "customer-arrival-type" type) include the data:

```json
{
  "data": {
     "customer": {
       "name": "John Michaels",
       "address": "111 Some Street, SomeCity, SomeCountry",
       "age": 40
     }
  }
}
```

Here is a sample diagram showing our workflow, each numbered step on this diagram shows a certain defined point during
workflow execution at which data filters are invoked and correspond to the numbered items below.

<p align="center">
<img src="media/spec/using-multiple-filters-example.png" height="400px" alt="Using Multple Filters Example"/>
</p>

**(1) Workflow execution starts**: Workflow data is passed to our "WaitForCustomerToArrive" event state as data input.
Workflow transitions to its starting state, namely the "WaitForCustomerToArrive" event state.

The event state **stateDataFilter** is invoked to filter this data input. Its "dataInputPath" is evaluated and filters
 only the "hello" greetings in different languages. At this point our event state data contains:

```json
{
  "hello": {
      "english": "Hello",
      "spanish": "Hola",
      "german": "Hallo",
      "russian": "Здравствуйте"
    }
}
```

**(2) CloudEvent of type "customer-arrival-type" is consumed**: First the "eventDataFilter" is triggered. Its "dataInputPath"
expression selects the "customer" object from the events data and places it into the state data.

At this point our event state data contains:

```json
{
  "hello": {
      "english": "Hello",
      "spanish": "Hola",
      "german": "Hallo",
      "russian": "Здравствуйте"
    },
    "customer": {
       "name": "John Michaels",
       "address": "111 Some Street, SomeCity, SomeCountry",
       "age": 40
     }
}
```

**(3) Event state performs its actions**:
Before the first action is executed, its actionDataFilter is invoked. Its "dataInputPath" expression selects
the entire state data as the data available to functions that should be executed. Its "dataResultsPath" expression
specifies that results of all functions executed in this action should be placed back to the state data as part
of a new "finalCustomerGreeting" object.

The action then calls the "greetingFunction" function passing in as parameters the spanish greeting and the name of the customer that arrived.

We assume that for this example "greetingFunction" returns:

```json
{
   "finalCustomerGreeting": "Hola John Michaels!"
}
```

which then becomes the result of the action.

**(4) Event State Completes Workflow Execution**: The results of action executions as defined in the actionDataFilter are placed into the
states data under the "finalCustomerGreeting" object. So at this point our event state data contains:

```json
{
  "hello": {
      "english": "Hello",
      "spanish": "Hola",
      "german": "Hallo",
      "russian": "Здравствуйте"
    },
    "customer": {
       "name": "John Michaels",
       "address": "111 Some Street, SomeCity, SomeCountry",
       "age": 40
     },
     "finalCustomerGreeting": "Hola John Michaels!"
}
```

Since our event state has performed all actions it is ready to either transition to the next state or end workflow execution if it is an end state.
Before this happens though, the "stateDataFilter" is again invoked to filter this states data, specifically the "dataOutputPath" expression
selects only the "finalCustomerGreeting" object to make it the data output of the state.

Because our event state is also an end state, its data output becomes the final [workflow data output](#Workflow-data-output). Namely:

```json
{
   "finalCustomerGreeting": "Hola John Michaels!"
}
```

Note that in case of multiple actions with each containing an "actionDataFilter", you must be careful for their results
not to overwrite each other after actions complete and their results are added to the state data.
Also note that in case of parallel execution of actions, the results of only those that complete before the state
transitions to the next one or ends workflow execution (end state) can be considered to be added to the state data.

#### Workflow data output

Once a workflow instance reaches an end state (where the `end` property is defined) and the workflow finishes its execution
the data output of that result state becomes the workflow data output. This output can be logged or indexed depending on the
implementation details.

In order to define the structure of expected workflow data output you can use the workflow
`dataOutputSchema` property. This property allows you to link to a [JSON Schema](https://json-schema.org/) definition
that describes the expected workflow data output. This can be used for documentation purposes or implementations may
decide to strictly enforce it.

### Workflow Error Handling

Serverless Workflow language allows you to define `explicit` error handling, meaning you can define what should happen
in case of errors inside your workflow model rather than some generic error handling entity.
This allows error handling to become part of your orchestration activities and as such part of your business problem
solutions.

Each workflow state can define error handling, which is related only to errors that may arise during its 
execution. Error handling defined in one state cannot be used to handle errors that happened during execution of another state
during workflow execution.

Errors that may arise during workflow execution that are not explicitly handled within the workflow definition
should be reported by runtime implementations and halt workflow execution, 

Within workflow definitions, errors defined are `domain specific`, meaning they are defined within 
the actual business domain, rather than their technical (programming-language-specific) description.

For example, we can define errors such as "Order not found", or "Item not in inventory", rather than having to
use terms such as "java.lang.IllegalAccessError", or "response.status == 404", which 
might make little to no sense to our specific problem domain, as well as may not be portable across various runtime implementations.

In addition to the domain specific error name, users have the option to also an optional error code
to help runtime implementations with mapping defined errors to concrete underlying technical ones.

Runtime implementations must be able to map the error domain specific name (and the optional error code)
to concrete technical errors that arise during workflow execution. 

#### Defining Errors

Errors can be defined via the states `onErrors` property. It is an array of ['error'](#Error-Definition) definitions.

Each error definition should have a unique `error` property. There can be only one error definition
which has the `error` property set to the wildcard character `*`.

The order of error definitions within `onErrors` does not matter. 

If there is an error definition with the `error` property set to the wildcard character `*`, it 
can mean either "all errors", if it is the only error definition defined, or it can mean
"all other errors", in the case where other error definitions are defined.
Note that if the `error` property is set to `*`, the error definition `code` property should not be defined.
Runtime implementations should warn users in this case.

Let's take a look at an example of each of these two cases:


<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
"onErrors": [
  {
    "error": "Item not in inventory",
    "transition": {
      "nextState": "ReimburseCustomer"
    }
  },
  {
    "error": "*",
    "transition": {
      "nextState": "handleAnyOtherError"
    }
  }
]
}
```

</td>
<td valign="top">

```yaml
onErrors:
- error: Item not in inventory
  transition:
    nextState: ReimburseCustomer
- error: "*"
  transition:
    nextState: handleAnyOtherError
```

</td>
</tr>
</table>

In this example the "Item not in inventory" error is being handled by the first error definition.
The second error definition handles "all other" errors that may happen during this states execution.

On the other hand the following example shows how to handle "all" errors with the same error definition:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
"onErrors": [
  {
    "error": "*",
    "transition": {
      "nextState": "handleAllErrors"
    }
  }
]
}
```

</td>
<td valign="top">

```yaml
onErrors:
- error: "*"
  transition:
    nextState: handleAllErrors
```

</td>
</tr>
</table>

#### Defining Retries

Retries are related to errors. When certain errors are encountered we might want to retry the states execution.

We can define retries within the workflow states [error definitions](#Defining-Errors).
This is done by defining the [retry stragy](#Retry-Definition) as the workflow top-level parameter using its `retries` array, and then
adding a `retryRef` parameter to the error definition which references this retry strategy for a specific error. 

If a defined retry for the defined error is successful, the defined workflow control flow logic of the state
should be performed, meaning either workflow can transition according to the states `transition`
definition, or end workflow execution in case the state defines an `end` definition.

If the defined retry for the defined error is not successful, workflow control flow logic should follow the 
`transition` definition of the error definition where the retry is defined, to transition to the next state that can handle this problem.

Let's take a look at an example of a top-level retries definition of a workflow:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
"retries": [
  {
    "name": "Service Call Timeout Retry Strategy",
    "delay": "PT1M",
    "maxAttempts": 4
  }
]
}
```

</td>
<td valign="top">

```yaml
retries:
- name: Service Call Timeout Retry Strategy
  delay: PT1M
  maxAttempts: 4
```

</td>
</tr>
</table>

This defines a reusable retry strategy. It can be referenced by different workflow states if needed to define the 
retries that need to performed for some specific errors that might be encountered during workflow execution.

In this particular case we define a retry strategy for "Service Call Timeouts" which says that the states 
control-flow logic should be retried up to 4 times, with a 1 minute delay between each retry attempt.

Different states now can use this retry strategy defined. For example:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
"onErrors": [
  {
    "error": "Inventory service timeout",
    "retryRef": "Service Call Timeout Retry Strategy",
    "transition": {
      "nextState": "ReimburseCustomer"
    }
  }
]
}
```

</td>
<td valign="top">

```yaml
onErrors:
- error: Inventory service timeout
  retryRef: Service Call Timeout Retry Strategy
  transition:
    nextState: ReimburseCustomer
```

</td>
</tr>
</table>

In this example we say that if the  "Inventory service timeout" error is encountered, we want to use our defined "Service Call Timeout Retry Strategy"
which holds the needed retry information. If the error definition does not include a `retryRef` property
it means that we do not want to perform retries for the defined error.


When referencing a retry strategy in your states error definitions, if the maximum amount of unsuccessful retries is reached, 
the workflow should transition to the next state
as defined by the error definitions `transition` property. If one of the performed retries is successful,
the states `transition` property should be taken, and the one defined in the error definition should be 
ignored.

In order to issue a retry, the current state execution should be halted first, meaning 
that in the cases of [parallel](#Parallel-State) states, all currently running branches should 
halt their executions, before a retry can be performed.

It is important to consider one particular case which are retries defined within [event](#Event-State) states that are also
workflow starting states 
(have the `start` property defined). Starting event states trigger an instance of the workflow
when the particular event or events it defines are consumed. In case of an error which happens during
execution of the state, runtimes should not create a new instance of this workflow, or 
wait for the defined event or events again. In these cases only the states actions should be retried
and the received event information used for all of the issued retries.

### Workflow Compensation

Compensation deals with undoing or reversing the work of one or more states which have 
already successfully completed. For example, let's say that we have charged a customer $100 for an item 
purchase. If later the customer decides to cancel this purchase we need to undo this. One way of 
doing that is to credit the customer $100.

It's important to understand that compensation with workflows is not the same as for example rolling back
a transaction (a strict undo). Compensating a workflow state which has successfully completed 
might involve multiple logical steps and thus is part of the overall business logic that must be 
defined within the workflow itself. To explain this let's use our previous example and say that when our
customer made the item purchase, our workflow has sent her/him a confirmation email. In the case we need to 
compensate this purchase, we cannot just "undo" the confirmation email sent. Instead, we want to 
send a second email to the customer which includes purchase cancellation information.

Compensation in Serverless Workflow must be explicitly defined by the workflow control flow logic.
It cannot be dynamically triggered by initial workflow data, event payloads, results of service invocations, or 
errors.

#### Defining Compensation

Each workflow state can define how it should be compensated via its `compensatedBy` property.
This property references another workflow state (by it's unique name) which is responsible for the actual compensation.

States referenced by `compensatedBy` must obey following rules:
* They should not have any incoming transitions (should not be part of the main workflow control-flow logic)
* They cannot be an [event state](#Event-State)
* They cannot define an [start definition](#Start-definition). If they do, it should be ignored
* They cannot define an [end definition](#End-definition). If they do, it should be ignored
* They must define the `usedForCompensation` property and set it to `true`
* They can transition only to states which also have their `usedForCompensation` property and set to `true`
* They cannot themselves set their `compensatedBy` property to true (compensation is not recursive)
 
Runtime implementations should raise compile time / parsing exceptions for all the cases mentioned above. 

Let's take a look at an example workflow state which defines its `compensatedBy` property, and the compensation
state it references:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
 {
 "states": [
      {
          "name": "NewItemPurchase",
          "type": "event",
          "onEvents": [
            {
              "eventRefs": [
                "NewPurchase"
              ],
              "actions": [
                {
                  "functionRef": {
                    "refName": "DebitCustomerFunction",
                    "parameters": {
                        "customerid": "{{ $.purchase.customerid }}",
                        "amount": "{{ $.purchase.amount }}"
                    }
                  }
                },
                {
                  "functionRef": {
                    "refName": "SendPurchaseConfirmationEmailFunction",
                    "parameters": {
                        "customerid": "{{ $.purchase.customerid }}"
                    }
                  }
                }
              ]
            }
          ],
          "compensatedBy": "CancelPurchase",
          "transition": {
              "nextState": "SomeNextWorkflowState"
          }
      },
      {
        "name": "CancelPurchase",
        "type": "operation",
        "usedForCompensation": true,
        "actions": [
            {
              "functionRef": {
                "refName": "CreditCustomerFunction",
                "parameters": {
                    "customerid": "{{ $.purchase.customerid }}",
                    "amount": "{{ $.purchase.amount }}"
                }
              }
            },
            {
              "functionRef": {
                "refName": "SendPurchaseCancellationEmailFunction",
                "parameters": {
                    "customerid": "{{ $.purchase.customerid }}"
                }
              }
            }
          ]
    }
 ]
 }
```
</td>
<td valign="top">

```yaml
states:
- name: NewItemPurchase
  type: event
  onEvents:
  - eventRefs:
    - NewPurchase
    actions:
    - functionRef:
        refName: DebitCustomerFunction
        parameters:
          customerid: "{{ $.purchase.customerid }}"
          amount: "{{ $.purchase.amount }}"
    - functionRef:
        refName: SendPurchaseConfirmationEmailFunction
        parameters:
          customerid: "{{ $.purchase.customerid }}"
  compensatedBy: CancelPurchase
  transition:
    nextState: SomeNextWorkflowState
- name: CancelPurchase
  type: operation
  usedForCompensation: true
  actions:
  - functionRef:
      refName: CreditCustomerFunction
      parameters:
        customerid: "{{ $.purchase.customerid }}"
        amount: "{{ $.purchase.amount }}"
  - functionRef:
      refName: SendPurchaseCancellationEmailFunction
      parameters:
        customerid: "{{ $.purchase.customerid }}"
```
</td>
</tr>
</table>

In this example our "NewItemPurchase" [event state](#Event-state) waits for a "NewPurchase" event and then 
debits the customer and sends them a purchase confirmation email. It defines that it's compensated by the 
"CancelPurchase" [operation state](#Operation-state) which performs two actions, namely credits back the 
purchase amount to customer and sends them a purchase cancellation email.

#### Triggering Compensation

As previously mentioned, compensation must be explicitly triggered by the workflows control-flow logic. 
This can be done via [transition](#Transition-definition) and [end](#End-definition) definitions.

Let's take a look at each:

1. Compensation triggered on transition:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
  "transition": {
      "compensateBefore": true,
      "nextState": "NextWorkflowState"
  }
}
```
</td>
<td valign="top">

```yaml
transition:
  compensateBefore: true
  nextState: NextWorkflowState
```
</td>
</tr>
</table>

Transitions can trigger compensations by specifying the `compensateBefore` property and setting it to `true`.
This means that before the transition is executed (workflow continues its execution to the "NextWorkflowState" in this example),
workflow compensation must be performed. 

2. Compensation triggered by end definition:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
  "end": {
    "compensateBefore": true,
    "kind":"default"
  }
}
```
</td>
<td valign="top">

```yaml
end:
  compensateBefore: true
  kind: default
```
</td>
</tr>
</table>

End definitions can trigger compensations by specifying the `compensateBefore` property and setting it to `true`.
This means that before workflow finishes its execution workflow compensation must be performed. Note that 
in case when the end definition has its "kind" set to "event", compensation must be performed before 
producing the specified events and ending workflow execution.

#### Compensation Execution Details

Now that we have seen how to define and trigger compensation, we need to go into details on how compensation should be executed.
Compensation is performed on all already successfully completed states (that define `compensatedBy`) in **reverse** order.
Compensation is always done in sequential order, and should not be executed in parallel.

Let's take a look at the following workflow image:

<p align="center">
<img src="media/spec/compensation-exec.png" height="400px" alt="Compensation Execution Example"/>
</p> 

In this example lets say our workflow execution is at the "End" state which defines the `compensateBefore` property to true
as shown in the previous section. States with a red border, namely "A", "B", "D" and "E" are states which have so far
been executed successfully. State "C" has not been executed during workflow execution in our example.

When workflow execution encounters our "End" state, compensation has to be performed. This is done in **reverse** order:
1. State "E" is not compensated as it does not define a `compensatedBy` state
2. State "D" is compensated by executing compensation "D1"
3. State "B" is compensated by executing "B1" and then "B2"
4. State C is not compensated as it was never active during workflow execution
5. State A is not comped as it does not define a `compensatedBy` state

So if we look just at the workflow execution flow, the same workflow could be seen as:

<p align="center">
<img src="media/spec/compensation-exec2.png" height="400px" alt="Compensation Execution Example 2"/>
</p> 

Workflow data management does not change during compensation. In our example when compensation is triggered, 
the current workflow data is passed as input to the "D1" state, the first compensation state for our example. 
It state data output is then passed as state data input to "B1", and so on.

#### Compensation and Active States

In some cases when compensation is triggered, some states such as [Parallel](#Parallel-State) and [ForEach](#ForEach-State)
states can still be "active", meaning they still might have some async executions that are being performed.

If compensation needs to performed on such still active states, the state execution must be first cancelled.
After it is cancelled, compensation should be performed.

#### Unrecoverable errors during compensation

States that are marked as `usedForCompensation` can define [error handling](#Workflow-Error-Handling) via their
`onErrors` property just like any other workflow states. In case of unrecoverable errors during their execution
(errors not explicitly handled),
workflow execution should be stopped, which is the same behaviour as when not using compensation as well. 

### Workflow Metadata

Metadata enables you to enrich the serverless workflow model with information beyond its core definitions.
It is intended to be used by clients, such as tools and libraries, as well as users that find this information relevant.

Metadata should not affect workflow execution. Implementations may choose to use metadata information or ignore it.
Note, however, that using metadata to control workflow execution can lead to vendor-locked implementations that do not comply with the main goals of this specification, which is to be completely vendor-neutral.

Metadata includes key/value pairs (string types). Both keys and values are completely arbitrary and non-identifying.

Metadata can be added to:

- [Core Workflow definition](#Workflow-Definition)
- [Function definitions](#Function-Definition)
- [Event definitions](#Event-Definition)
- [State definitions](#State-Definition)
- [Switch state conditions](#switch-state-conditions)

Here is an example of metadata attached to the core workflow definition:

```json
{
  "id": "processSalesOrders",
  "name": "Process Sales Orders",
  "version": "1.0",
  "startsAt": "NewOrder",
  "metadata": {
    "loglevel": "Info",
    "environment": "Production",
    "category": "Sales",
    "giturl": "github.com/myproject",
    "author": "Author Name",
    "team": "Team Name",
    ...
  },
  "states": [
    ...
  ]
}
```

Some other examples of information that could be recorded in metadata are:

- UI tooling information such as sizing or scaling factors.
- Build, release, or image information such as timestamps, release ids, git branches, PR numbers, etc.
- Logging, monitoring, analytics, or audit repository information.
- Labels used for organizing/indexing purposes, such as "release" "stable", "track", "daily", etc.
 
## Extensions

The workflow extension mechanism allows you to enhance your model definitions with additional information useful for 
things like analytics, logging, simulation, debugging, tracing, etc.

Model extensions do no influence control flow logic (workflow execution semantics). 
They enhance it with extra information that can be consumed by runtime systems or tooling and 
evaluated with the end goal being overall workflow improvements in terms of time, cost, efficiency, etc.

Serverless Workflow specification provides extensions which can be found [here](extensions/README.md).

Even tho users can define their own extensions, it is encouraged to use the ones provided by the specification,
and for the community to contribute their extension to the specification so they can grow alongside it.

If you have an idea for a new workflow extension, or would like to enhance an existing one, 
please open an `New Extension Request` issue in this repository.

## Use Cases

You can find different Serverless Workflow use cases [here](usecases/README.md).

## Examples

You can find different Serverless Workflow examples [here](examples/README.md).

## References

You can find a list of other languages, technologies and specifications related to workflows [here](references/README.md).

## License

Serverless Workflow specification operates under the
[Apache License version 2.0](LICENSE).
