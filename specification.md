# Serverless Workflow

## Abstract

A specification that defines a vendor-neutral and declarative workflow language,
targeting the Serverless computing technology domain.
 
## Status of this document

This document represents the current state of the specification.
It includes all features so far released 
as well as all features planned to be added in the next release.

You can find all specification releases [here](https://github.com/serverlessworkflow/specification/releases).
You can find the specification roadmap [here](roadmap/README.md).

## Table of Contents

- [Overview](#Overview)
- [Project Components](#Project-Components)
- [Specification Details](#Specification-Details)
  - [Core Concepts](#Core-Concepts)
  - [Workflow Definition](#Workflow-Definition)
  - [Workflow Model](#Workflow-Model)
  - [Workflow Data](#Workflow-Data)
  - [Workflow Functions](#Workflow-Functions)
  - [Workflow Expressions](#Workflow-Expressions)
  - [Workflow Error Handling](#Workflow-Error-Handling)
    - [Defining Errors](#Defining-Errors)
    - [Defining Retries](#Defining-Retries)
  - [Workflow Compensation](#Workflow-Compensation)
  - [Workflow Versioning](#Workflow-Versioning)
  - [Workflow Globals](#Workflow-Globals)
  - [Workflow Secrets](#Workflow-Secrets)  
  - [Workflow Metadata](#Workflow-Metadata)
- [Extensions](#Extensions)
- [Use Cases](#Use-Cases)
- [Examples](#Examples)
- [Comparison to other workflow languages](#Comparison-to-other-workflow-languages)
- [References](#References)
- [License](#License)

## Overview

Workflows allow us to capture and organize business requirements in an unified manner.
They can bridge the gap between how we express and model business logic.

A key component of workflows is the domain-specific language (DSL) we use to model our 
business logic and solutions. Selecting the appropriate workflow language for our business and technology domains is 
a very important decision to be considered. 

Serverless Workflow focuses on defining a **vendor-neutral**, **platform-independent**, and **declarative** workflow
language that targets the serverless computing technology domain. 
It can be used to significantly bridge the gap between your unique business domain and the target technology domain.

### Why we need a specification?

The lack of a common way to define and model workflows means that we must constantly re-learn 
how to write them. This also limits the potential for common libraries, tooling and 
infrastructure to aid workflow modeling and execution across different platforms.
Portability as well as productivity that can be achieved from workflow orchestration is hindered overall.

Serverless Workflow addresses the need for a community-driven, vendor-neutral and a platform-independent
workflow language specification that targets the serverless computing technology domain.

Having and using a specification-based workflow language allows us to model our workflows once and deploy them 
onto many different container/cloud platforms, expecting the same execution results.

<p align="center">
<img src="media/spec/spec-goals.png" height="400px" alt="Serverless Workflow Specification Goals"/>
</p>

For more information on the history, development and design rationale behind the specification, see the [Serverless Workflow Wiki](https://github.com/serverlessworkflow/specification/wiki).

### Focus on standards

<p align="center">
<img src="media/spec/spec-parts.png" width="600" alt="Serverless Workflow Specification Focus On Standards"/>
</p>

Serverless Workflow language takes advantage of well-established and known standards such as [CloudEvents](https://cloudevents.io/), [OpenAPI](https://www.openapis.org/) specifications,
[gRPC](https://grpc.io/) and [GraphQL](https://graphql.org/).

## Project Components

<p align="center">
<img src="media/spec/spec-overview.png" height="400px" alt="Serverless Workflow Specification Overview"/>
</p>

The specification has multiple components:

* Definitions of the workflow language. This is defined via the [Workflow JSON Schema](schema/workflow.json). You can use both 
[JSON](https://www.json.org/json-en.html) and [YAML](https://yaml.org/) formats to model your workflows.
* Software Development Kits (SDKs) for both [Go](https://github.com/serverlessworkflow/sdk-go) and [Java](https://github.com/serverlessworkflow/sdk-java),
  and we plan to add them for more languages in the future.
* Set of [Workflow Extensions](extensions/README.md) which 
  allow users to define additional, non-execution-related workflow information. This information can be used to improve
  workflow performance. 
  Some example workflow extensions include Key Performance Indicators (KPIs), Simulation, Tracing, etc.
* Technology Compatibility Kit (TCK) to be used as a specification conformance tool for runtime implementations.

## Specification Details

Following sections provide detailed descriptions of all parts of the Serverless Workflow language. 

### Core Concepts

This section describes some of the core Serverless Workflow concepts:

### Workflow Definition

A workflow definition is a single artifact written in the Serverless Workflow 
language. It consists of the core [Workflow Definition Structure](#Workflow-Definition-Structure) 
and the [Workflow Model](#Workflow-Model) It defines a blueprint used by runtimes for its execution. 

A business solution can be composed of any number of related workflow definitions.
Their relationships are explicitly modeled with the Serverless Workflow language (for example
by using [SubFlowRef Definition](#SubFlowRef-Definition) in actions).

Runtimes can initialize workflow definitions for some particular set of data inputs or events
which forms [workflow instances](#Workflow-Instance).

### Workflow Instance

A workflow instance represents a single workflow execution according to the given 
workflow definition. Instances should be kept isolated, but 
still be able to have access to other running instances. 

Depending on their workflow definition, workflow instances can be short-lived or 
can execute for days, weeks, or longer.

Each workflow instances should have its unique identifier, which should remain 
unchanged throughout its execution.

Workflow definitions can describe how/when workflow instances should be created via
its `start` property. This property is described in detail in the  [start definition section](#Start-Definition).
For example, instance creation can be defined for some set of data, but other ways are also possible.
For example you can enforce instance creations upon arrival of certain events with a starting [EventState](#Event-State), as well
on a defined [schedule](#Schedule-Definition). 

Workflow instance termination is also explicitly described in the workflow definition. 
By default, instances should be terminated once there are no active workflow paths (all active
paths reach a state containing the default [end definition](#End-Definition)). Other ways, such as 
using the `terminate` property of the [end definition](#End-Definition) to terminate instance execution,
or defining an [execution timeout](#ExecTimeout-Definition) are also possible.

This default behavior can be changed by setting the `keepActive` workflow property to `true`.
In this case the only way to terminate a workflow instance is for its control flow to explicitly end with a "terminate" [end definition](#End-Definition),
or if the defined [`execTimeout`](#ExecTimeout-Definition) time is reached.
 
### Workflow Model

The Serverless Workflow language is composed of:

* [Function definitions](#Function-Definition): Reusable functions that can declare services that need to be invoked, or expressions to be evaluated.
* [Event definitions](#Event-Definition): Reusable declarations of events that need to be `consumed` to start or continue workflow instances, trigger function/service execution, or be `produced` during workflow execution.
* [Retry definitions](#Retry-Definition): Reusable retry definitions. Can specify retry strategies for service invocations during workflow execution.
* [State definitions](#State-Definition): Definition of states, the building blocks of workflow `control flow logic`. States can reference the reusable function, event and retry definitions.

### Workflow Data

Serverless Workflow data is represented in [JSON](https://www.json.org/json-en.html) format.
Data flow and execution logic go hand in hand, meaning as workflow execution follows the workflow definition
logic, so does the workflow data:

<p align="center">
<img src="media/spec/workflowdataflow.png" height="400" alt="Serverless Workflow Data Flow"/>
</p>
 
The initial [Workflow data input](#Workflow-data-input) is passed to the workflow starting state as its data input.
When a state finishes its execution, [its data output is passed as data input to the next state](#Information-passing-Between-States) that should be executed.

When workflow execution ends, the last executed workflow state's data output becomes the final [Workflow data output](#Workflow-data-output).

States can filter their data inputs and outputs using [State Data filters](#State-data-filters).

States can also consume events as well as invoke services. These event payloads and service invocation results
can be filtered using [Event data filters](#Event-data-filters) and [Action data filters](#Action-data-filters).

Data filters use [workflow expressions](#Workflow-Expressions) for selecting and manipulating state data
input and output, action inputs and results, and event payloads.

Multiple filters can be combined to gain high level of control of your workflow state data. You can find an example of that in
[this](#Using-multiple-data-filters) section.

Data from consumed events,and action execution results are added/merged
to state data. Reference the [data merging section](#Data-Merging) to learn about the merging rules that should be applied.

#### Workflow Data Input

The initial data input into a workflow instance. Must be a valid [JSON object](https://tools.ietf.org/html/rfc7159#section-4).
If no input is provided, the default data input should be an empty JSON object:

```json
{ }
```

Workflow data input is passed to the workflow starting state as its data input.

<p align="center">
<img src="media/spec/workflowdatainput.png" height="350px" alt="Workflow data input"/>
</p>

#### Information Passing Between States

States in a workflow can receive data (data input) and produce a data result (data output). The state's data input is typically the previous state's data output.
When a state completes its execution, its data output is passed to the state's data input it transitions to.
There are two rules to consider here:

- If the state is the workflow starting state, its data input is the [workflow data input](#Workflow-data-input).
- When workflow execution ends, the data output of the last executed state becomes the [workflow data output](#Workflow-data-output).  

<p align="center">
<img src="media/spec/basic-state-data-passing.png" height="350px" alt="Basic state data passing"/>
</p>

#### Workflow data output

Each workflow execution should produce a data output. 
The workflow data output is the data output of the last executed workflow state.

#### State data filters

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| input | Workflow expression to filter the states data input | string | no |
| output | Workflow expression that filters the states data output | string | no |

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
    "stateDataFilter": {
      "input": "${ .orders }",
      "output": "${ .provisionedOrders }"
    }
}
```

</td>
<td valign="top">

```yaml
stateDataFilter:
  input: "${ .orders }"
  output: "${ .provisionedOrders }"
```

</td>
</tr>
</table>

</details>

State data filters can be used to filter the states data input and output.

The state data filters `input` property expression is applied when the workflow transitions to the current state and receives its data input.
It can be used to select only data that is needed and disregard what is not needed.
If `input` is not defined or does not select any parts of the state's data input, its data input is not filtered.

The state data filter `output` property expression is applied right before the state transitions to the next state defined. 
It filters the state's data output to be passed as data input to the transitioning state.
If the current state is the workflow end state, the filtered state's data output becomes the workflow data output.
If `output` is not defined or does not select any parts of the state's data output, its data output is not filtered.

Results of the `input` expression should become the state data. 
Results of the `output` expression should become the state data output.

For more information on this you can reference the [data merging](#Data-Merging) section.

Let's take a look at some examples of state filters. For our examples let's say the data input to our state is as follows:

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

For the first example, our state only cares about fruits data, and we want to disregard the vegetables. To do this
we can define a state filter:

```json
{
  "stateDataFilter": {
    "input": "${ {fruits: .fruits} }"
  }
}
```

The state data output then would include only the fruits data:

```json
{
  "fruits": [ "apple", "orange", "pear"]
}
```

<p align="center">
<img src="media/spec/state-data-filter-example1.png" height="400px" alt="State Data Filter Example"/>
</p>

For our second example, let's say that we are interested in the only vegetable "veggie-like".
Here we have two ways of filtering our data, depending on if actions within our state need access to all vegetables, or
only the ones that are "veggie-like".

The first way would be to use both "input", and "output":

```json
{
  "stateDataFilter": {
    "input": "${ {vegetables: .vegetables} }",
    "output": "${ {vegetables: .vegetables[] | select(.veggieLike == true)} }"
  }
}
```

The states data input filter selects all the vegetables from the main data input. Once all actions have performed, before the state transition
or workflow execution completion (if this is an end state), the "output" of the state filter selects only the vegetables which are "veggie like".

<p align="center">
<img src="media/spec/state-data-filter-example2.png" height="400px" alt="State Data Filter Example"/>
</p>

The second way would be to directly filter only the "veggie like" vegetables with just the data input path:

```json
{
  "stateDataFilter": {
    "input": "${ {vegetables: .vegetables[] | select(.veggieLike == true)} }"
  }
}
```

#### Action data filters

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| fromStateData | Workflow expression that filters state data that can be used by the action | string | no |
| results | Workflow expression that filters the actions data results | string | no |
| toStateData | Workflow expression that selects a state data element to which the action results should be added/merged into. If not specified denotes the top-level state data element | string | no |

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
  "actionDataFilter": {
    "fromStateData": "${ .language }", 
    "results": "${ .results.greeting }",
    "toStateData": "${ .finalgreeting }"
  }
}
```

</td>
<td valign="top">

```yaml
actionDataFilter:
  fromStateData: "${ .language }"
  results: "${ .results.greeting }"
  toStateData: "${ .finalgreeting }"
```

</td>
</tr>
</table>

</details>

Action data filters can be used inside [Action definitions.](#Action-Definition)
Each action can define this filter which can:

* Filter the state data to select only the data that can be used within function definition arguments using its `fromStateData` property.
* Filter the action results to select only the result data that should be added/merged back into the state data
using its `results` property.
* Select the part of state data which the action data results should be added/merged to
using the `toStateData` property.

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
       "functionRef": "breadAndPastaTypesFunction",
       "actionDataFilter": {
          "results": "${ {breads: .breads} }"
       }
    }
 ]
}
```

The `results` will filter the action results, which would then be:

```json
{
  "breads": [
    "baguette",
    "brioche",
    "rye"
  ]
}
```

Now let's take a look at a similar example (same expected action results) and assume our current state data is:

```json
{
  "itemsToBuyAtStore": [
  ]
}
```

and have the following action definition:

```json
{
"actions":[  
    {  
       "functionRef": "breadAndPastaTypesFunction",
       "actionDataFilter": {
          "results": "${ [ .breads[0], .pasta[1] ] }",
          "toStateData": "${ .itemsToBuyAtStore }"
       }
    }
 ]
}
```

In this case, our `results` select the first bread and the second element of the pasta array.
The `toStateData` expression then selects the `itemsToBuyAtStore` array of the state data to add/merge these results
into. With this, after our action executes the state data would be:

```json
{
  "itemsToBuyAtStore": [
    "baguette",
    "spaghetti"
  ]
}
```

#### Event data filters

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| data | Workflow expression that filters the event data (payload) | string | no |
| toStateData | Workflow expression that selects a state data element to which the action results should be added/merged into. If not specified denotes the top-level state data element | string | no |

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
    "eventDataFilter": {
       "data": "${ .data.results }"
    }
}
```

</td>
<td valign="top">

```yaml
eventDataFilter:
  data: "${ .data.results }"
```

</td>
</tr>
</table>

</details>

Event data filters can be used to filter consumed event payloads.
They can be used to:

* Filter the event payload to select only the data that should be added/merged into the state data 
using its `data` property.
* Select the part of state data into which the event payload should be added/merged into
using the `toStateData` property.

Allows event data to be filtered and added to or merged with the state data. All events have to be in the CloudEvents format
and event data filters can filter both context attributes and the event payload (data) using the `data` property.

Here is an example using an event filter:

<p align="center">
<img src="media/spec/event-data-filter-example1.png" height="400px" alt="Event Data Filter Example"/>
</p>

Note that the data input to the Event data filters depends on the `dataOnly` property of the associated [Event definition](#Event-Definition).
If this property is not defined (has default value of `true`), Event data filter expressions are evaluated against the event payload (the CloudEvents `data` attribute only). If it is set to
`false`, the expressions should be evaluated against the entire CloudEvent (including its context attributes).

#### Using multiple data filters

As [Event states](#Event-State) can take advantage of all defined data filters. In the example below, we define
a workflow with a single event state and show how data filters can be combined.

```json
{
    "id": "GreetCustomersWorkflow",
    "name": "Greet Customers when they arrive",
    "version": "1.0",
    "specVersion": "0.6",
    "start": "WaitForCustomerToArrive",
    "states":[
         {
            "name": "WaitForCustomerToArrive",
            "type": "event",
            "onEvents": [{
                "eventRefs": ["CustomerArrivesEvent"],
                "eventDataFilter": {
                    "data": "${ .customer }",
                    "toStateData": "${ .customerInfo }"
                },
                "actions":[
                    {
                        "functionRef": {
                            "refName": "greetingFunction",
                            "arguments": {
                                "greeting": "${ .spanish } ",
                                "customerName": "${ .customerInfo.name } "
                            }
                        },
                        "actionDataFilter": {
                            "fromStateData": "${ .hello }",
                            "results": "${ .greetingMessageResult }",
                            "toStateData": "${ .finalCustomerGreeting }"
                        }
                    }
                ]
            }],
            "stateDataFilter": {
                "input": "${ .greetings } ",
                "output": "${ .finalCustomerGreeting }"
            },
            "end": true
        }
    ],
    "events": [{
        "name": "CustomerArrivesEvent",
        "type": "customer-arrival-type",
        "source": "customer-arrival-event-source"
     }],
    "functions": [{
        "name": "greetingFunction",
        "operation": "http://my.api.org/myapi.json#greeting"
    }]
}
```

The workflow data input when starting workflow execution is assumed to include greetings in different languages:

```json
{
  "greetings": {
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
}
```
The workflow data input then becomes the data input of the starting workflow state.

We also assume for this example that the CloudEvent that our event state consumes include the data (payload):

```json
{
 "customer": {
   "name": "John Michaels",
   "address": "111 Some Street, SomeCity, SomeCountry",
   "age": 40
 }
}
```

Here is a sample diagram showing our workflow, each numbered step on this diagram shows a certain defined point during
workflow execution at which data filters are invoked and correspond to the numbered items below.

<p align="center">
<img src="media/spec/using-multiple-filters-example.png" height="400px" alt="Using Multple Filters Example"/>
</p>

**(1) Workflow execution starts**: Workflow data is passed to our "WaitForCustomerToArrive" event state as data input.
Workflow executes its starting state, namely the "WaitForCustomerToArrive" event state.

The event state **stateDataFilter** is invoked to filter its data input. The filters "input" expression is evaluated and 
selects only the "greetings" data. The rest of the state data input should be disregarded. 

At this point our state data should be:

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

**(2) CloudEvent of type "customer-arrival-type" is consumed**: Once the event is consumed, the "eventDataFilter" is triggered.
Its "data" expression selects the "customer" object from the events data. The "toStateData" expression
says that we should add/merge this selected event data to the state data in its "customerInfo" property. If this property
exists it should be merged, if it does not exist, one should be created.

At this point our state data contains:

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
    },
    "customerInfo": {
       "name": "John Michaels",
       "address": "111 Some Street, SomeCity, SomeCountry",
       "age": 40
     }
}
```

**(3) Event state performs its actions**:
Before the first action is executed, its actionDataFilter is invoked. Its "fromStateData" expression filters
the current state data to select from its data that should be available to action arguments. In this example
it selects the "hello" property from the current state data.
At this point the action is executed. 
We assume that for this example "greetingFunction" returns:

```json
{
   "execInfo": {
     "execTime": "10ms",
     "failures": false
   },
   "greetingMessageResult": "Hola John Michaels!"
}
```

After the action is executed, the actionDataFilter "results" expression is evaluated to filter the results returned from the action execution. In this case, we select only the "greetingMessageResult" element from the results.

The action filters "toStateData" expression then defines that we want to add/merge this action result to
state data under the "finalCustomerGreeting" element.

At this point, our state data contains:

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
    },
    "customerInfo": {
       "name": "John Michaels",
       "address": "111 Some Street, SomeCity, SomeCountry",
       "age": 40
     },
     "finalCustomerGreeting": "Hola John Michaels!"
}
```


**(4) Event State Completes  Execution**: 

When our event state finishes its execution, the states "stateDataFilter" "output" filter expression is executed
to filter the state data to create the final state data output.

Because our event state is also an end state, its data output becomes the final [workflow data output](#Workflow-data-output). Namely:

```json
{
   "finalCustomerGreeting": "Hola John Michaels!"
}
```

#### Data Merging

Consumed event data (payload) and action execution results should be merged into the state data.
Event and action data filters can be used to give more details about this operation.

By default, with no data filters specified, when an event is consumed, its entire data section (payload) should be merged
to the state data. Merging should be applied to the entire state data JSON element.

In case of event and action filters, their "toStateData" property can be defined to select a specific element 
of the state data with which merging should be done against. If this element does not exist, a new one should
be created first.

When merging, the state data element and the data (payload)/action result should have the same type, meaning
that you should not merge arrays with objects or objects with arrays etc. 

When merging elements of type object should be done by inserting all the key-value pairs from both objects into
a single combined object. If both objects contain a value for the same key, the object of the event data/action results 
should "win". To give an example, let's say we have the following state data:

```json
{
    "customer": {
        "name": "John",
        "address": "1234 street",
        "zip": "12345"
    }
}
```

and we have the following event payload that needs to be merged into the state data:

```json
{
    "customer": {
        "name": "John",
        "zip": "54321"
    }
}
```

After merging the state data should be:

```json
{
  "customer": {
    "name": "John",
    "address": "1234 street",
    "zip": "54321"
  }
}
```

Merging array types should be done by concatenating them into a larger array including unique elements of both arrays.
To give an example, merging:

```json
{
    "customers": [
      {
        "name": "John",
        "address": "1234 street",
        "zip": "12345"
      },
      {
        "name": "Jane",
        "address": "4321 street",
        "zip": "54321"
      }
    ]
}
```

into state data:

```json
{
    "customers": [
      {
        "name": "Michael",
        "address": "6789 street",
        "zip": "6789"
      }
    ]
}
```

should produce state data:

```json
{
    "customers": [
      { 
        "name": "Michael",
        "address": "6789 street",
        "zip": "6789"
      },
      {
        "name": "John",
        "address": "1234 street",
        "zip": "12345"
      },
      {
        "name": "Jane",
        "address": "4321 street",
        "zip": "54321"
      }
    ]
}
```


To give an example, merging:

```json
{
    "customers": [
      {
        "name": "John",
        "address": "1234 street",
        "zip": "12345"
      },
      {
        "name": "Jane",
        "address": "4321 street",
        "zip": "54321"
      }
    ]
}
```

into state data:

```json
{
    "customers": [
      {
        "name": "Michael",
        "address": "6789 street",
        "zip": "6789"
      }
    ]
}
```

should produce state data:

```json
{
    "customers": [
      { 
        "name": "Michael",
        "address": "6789 street",
        "zip": "6789"
      },
      {
        "name": "John",
        "address": "1234 street",
        "zip": "12345"
      },
      {
        "name": "Jane",
        "address": "4321 street",
        "zip": "54321"
      }
    ]
}
```


Merging number types should be done by overwriting the data from events data/action results into the merging element of the state data. 
For example merging action results:

```json
{
    "age": 30
}
```

into state data:

```json
{
    "age": 20
}
```

would produce state data: 

```json
{
    "age": 30
}
```

Merging string types should be done by overwriting the data from events data/action results into the merging element of the state data.

Merging number types should be done by overwriting the data from events data/action results into the merging element of the state data. 

### Workflow Functions

Workflow [functions](#Function-Definition) are reusable definitions for service invocations and/or expression evaluation.
They can be referenced by their domain-specific names inside workflow [states](#State-Definition).

Reference the following sections to learn more about workflow functions:
* [Using functions for RESTful service invocations](#Using-Functions-For-RESTful-Service-Invocations)
* [Using functions for gRPC service invocation](#Using-Functions-For-RPC-Service-Invocations)
* [Using functions for GraphQL service invocation](#Using-Functions-For-GraphQL-Service-Invocations)
* [Using functions for expression evaluations](#Using-Functions-For-Expression-Evaluation)

### Using Functions For RESTful Service Invocations

[Functions](#Function-Definition) can be used to describe services and their operations that need to be invoked during 
workflow execution. They can be referenced by states [action definitions](#Action-Definition) to clearly
define when the service operations should be invoked during workflow execution, as well as the data parameters
passed to them if needed.

Note that with Serverless Workflow, we can also define service invocations via events.
To learn more about that, please reference the [event definitions](#Event-Definition) section, 
as well as the [actions definitions](#Action-Definition) [eventRef](#EventRef-Definition) property.

Because of an overall lack of a common way to describe different services and their operations,
many workflow languages typically chose to define custom function definitions.
This approach, however, often runs into issues such as lack of portability, limited capabilities, as well as 
forcing non-workflow-specific information, such as service authentication, to be added inside the workflow language.

To avoid these issues, the Serverless Workflow specification mandates that details about 
RESTful services and their operations be described using the [OpenAPI Specification](https://www.openapis.org/) specification.
OpenAPI is a language-agnostic standard that describes discovery of RESTful services.
This allows Serverless Workflow language to describe RESTful services in a portable 
way, as well as workflow runtimes to utilize OpenAPI tooling and APIs to invoke service operations.

Here is an example function definition for a RESTful service operation. 

```json
{
"functions": [
  {
    "name": "sendOrderConfirmation",
    "operation": "file://confirmationapi.json#sendOrderConfirmation"
  }
]
}
```

It can, as previously mentioned be referenced during workflow execution when the invocation of this service is desired. 
For example:

```json
{
"states": [
  {
      "name":"SendConfirmState",
      "type":"operation",
      "actions":[  
       {  
       "functionRef": "sendOrderConfirmation"
      }],
      "end": true
  }]
}
```

Note that the referenced function definition type in this case must be `rest` (default type). 

For more information about functions, reference the [Functions definitions](#Function-Definition) section.

### Using Functions For RPC Service Invocations

Similar to defining invocations of operations on RESTful services, you can also use the workflow 
[functions definitions](#Function-Definition) that follow the remote procedure call (RPC) protocol.
For RPC invocations, the Serverless Workflow specification mandates that they are described using [gRPC](https://grpc.io/),
a widely used RPC system. 
gRPC uses [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview) to define messages, services,
and the methods on those services that can be invoked. 

Let's look at an example of invoking a service method using RPC. For this example let's say we have the following
gRPC protocol buffer definition in a myuserservice.proto file:

```text
service UserService {
    rpc AddUser(User) returns (google.protobuf.Empty) {
        option (google.api.http) = {
            post: "/api/v1/users"
            body: "*"
        };
    }
    rpc ListUsers(ListUsersRequest) returns (stream User) {
        option (google.api.http) = {
            get: "/api/v1/users"
        };
    }
    rpc ListUsersByRole(UserRole) returns (stream User) {
        option (google.api.http) = {
            get: "/api/v1/users/role"
        };
    }
    rpc UpdateUser(UpdateUserRequest) returns (User) {
        option (google.api.http) = {
            patch: "/api/v1/users/{user.id}"
            body: "*"
        };
    }
}
```

In our workflow definition, we can then use function definitions:

```json
{
"functions": [
  {
    "name": "listUsers",
    "operation": "file://myuserservice.proto#UserService#ListUsers",
    "type": "rpc"
  }
]
}
```

Note that the `operation` property has the following format: 

```text
<URI_to_proto_file>#<Service_Name>#<Service_Method_Name>
```

Note that the referenced function definition type in this case must be `rpc`. 

For more information about functions, reference the [Functions definitions](#Function-Definition) section.

### Using Functions For GraphQL Service Invocations

If you want to use GraphQL services, you can also invoke them using a similar syntax to the above methods.

We'll use the following [GraphQL schema definition](https://graphql.org/learn/schema/) to show how that would work with both a query and a mutation:

```graphql
type Query {
    pets: [Pet]
    pet(id: Int!): Pet
}

type Mutation {
    createPet(pet: PetInput!): Pet
}

type Treat {
    id: Int!
}

type Pet {
    id: Int!
    name: String!
    favoriteTreat: Treat
}

input PetInput {
    id: Int!
    name: String!
    favoriteTreatId: Int
}
```

#### Invoking a GraphQL `Query`

In our workflow definition, we can then use a function definition for the `pet` query field as such:

```json
{
  "functions": [
    {
      "name": "getOnePet",
      "operation": "https://example.com/pets/graphql#query#pet",
      "type": "graphql"
    }
  ]
}
```

Note that the `operation` property has the following format for the `graphql` type:

```text
<url_to_graphql_endpoint>#<literal "mutation" or "query">#<mutation_or_query_field>
```

In order to invoke this query, we would use the following `functionRef` parameters:

```json
{
  "refName": "getOnePet",
  "arguments": {
    "id": 42
  },
  "selectionSet": "{ id, name, favoriteTreat { id } }"
}
```

Which would return the following result:

```json
{
  "pet": {
    "id": 42,
    "name": "Snuffles",
    "favoriteTreat": {
      "id": 9001
    }
  }
}
```

#### Invoking a GraphQL `Mutation`

Likewise, we would use the following function definition:

```json
{
  "functions": [
    {
      "name": "createPet",
      "operation": "https://example.com/pets/graphql#mutation#createPet",
      "type": "graphql"
    }
  ]
}
```

With the parameters for the `functionRef`:

```json
{
  "refName": "createPet",
  "arguments": {
    "pet": {
      "id": 43,
      "name":"Sadaharu",
      "favoriteTreatId": 9001
    }
  },
  "selectionSet": "{ id, name, favoriteTreat { id } }"
}
```

Which would execute the mutation, creating the object and returning the following data:

```json
{
  "pet": {
    "id": 43,
    "name": "Sadaharu",
    "favoriteTreat": {
      "id": 9001
    }
  }
}
```

Note you can include [expressions](#Workflow-Expressions) in both `arguments` and `selectionSet`:

```json
{
  "refName": "getOnePet",
  "arguments": {
    "id": "${ .petId }"
  },
  "selectionSet": "{ id, name, age(useDogYears: ${ .isPetADog }) { dateOfBirth, years } }"
}
```

Expressions must be evaluated before executing the operation.

Note that GraphQL Subscriptions are not supported at this time.

For more information about functions, reference the [Functions definitions](#Function-Definition) section.

### Using Functions For Expression Evaluation

In addition to defining RESTful, RPC and GraphQL services and their operations, workflow [functions definitions](#Function-Definition)
can also be used to define expressions that should be evaluated during workflow execution.

Defining expressions as part of function definitions has the benefit of being able to reference
them by their logical name through workflow states where expression evaluation is required.

Expression functions must declare their `type` parameter to be `expression`. 

Let's take a look at an example of such definitions:

```json
{
"functions": [
  {
    "name": "isAdult",
    "operation": ".applicant | .age >= 18",
    "type": "expression"
  },
  {
    "name": "isMinor",
    "operation": ".applicant | .age < 18",
    "type": "expression"
  }
]
}
```

Here we define two reusable expression functions. Expressions in Serverless Workflow
can be evaluated against the workflow, or workflow state data. Note that different data filters play a big role as to which parts of the 
workflow data are being evaluated by the expressions. Reference the 
[State Data Filters](#State-data-filters) section for more information on this.

Our expression function definitions can now be referenced by workflow states when they need to be evaluated. For example:

```json
{
"states":[  
  {  
     "name":"CheckApplicant",
     "type":"switch",
     "dataConditions": [
        {
          "name": "Applicant is adult",
          "condition": "${ fn:isAdult }",
          "transition": "ApproveApplication"
        },
        {
          "name": "Applicant is minor",
          "condition": "${ fn:isMinor }",
          "transition": "RejectApplication"
        }
     ],
     "defaultCondition": {
        "transition": "RejectApplication"
     }
  }
]
}
```

Our expression functions can also be referenced and executed as part of state [action](#Action-Definition) execution.
Let's say we have the following workflow definition:

```json
{
    "name": "simpleadd",
    "functions": [
        {
            "name": "Increment Count Function",
            "type": "expression",
            "operation": ".count += 1 | .count"
        }
    ],
    "start": "Initialize Count",
    "states": [
        {
            "name": "Initialize Count",
            "type": "inject",
            "data": {
                "count": 0
            },
            "transition": "Increment Count"
        },
        {
            "name": "Increment Count",
            "type": "operation",
            "actions": [
                {
                    "functionRef": "Increment Count Function",
                    "actionFilter": {
                        "toStateData": "${ .count }"
                    }
                }
            ],
            "end": true
        }
    ]
}
```

The starting [inject state](#Inject-State) "Initialize Count" injects the count element into our state data, 
which then becomes the state data input of our "Increment Count" [operation state](#Operation-State).
This state defines an invocation of the "Increment Count Function" expression function defined in our workflow definition.

This triggers the evaluation of the defined expression. The input of this expression is by default the current state data.
Just like with "rest", and "rpc" type functions, expression functions also produce a result. In this case 
the result of the expression is just the number 1. 
The actions filter then assigns this result to the state data element "count" and the state data becomes:

``` json
{
    "count": 1
}
```

Note that the used function definition type in this case must be `expression`. 

For more information about functions, reference the [Functions definitions](#Function-Definition) section.

For more information about workflow expressions, reference the [Workflow Expressions](#Workflow-Expressions) section.

### Workflow Expressions

Workflow model parameters can use expressions to select/manipulate workflow and/or state data.

Note that different data filters play a big role as to which parts of the states data are to be used when the expression is
evaluated. Reference the 
[State Data Filtering](#State-data-filters) section for more information about state data filters.

By default, all workflow expressions should be defined using the [jq](https://stedolan.github.io/jq/) [version 1.6](https://github.com/stedolan/jq/releases/tag/jq-1.6) syntax.
You can find more information on jq in its [manual](https://stedolan.github.io/jq/manual/).

Serverless Workflow does not mandate the use of jq and it's possible to use an expression language 
of your choice with the restriction that a single one must be used for all expressions
in a workflow definition. If a different expression language needs to be used, make sure to set the workflow 
`expressionLang` property to identify it to runtime implementations.

Note that using a non-default expression language could lower the portability of your workflow definitions 
across multiple container/cloud platforms.

All workflow expressions in this document, [specification examples](examples/README.md) as well as [comparisons examples](comparisons/README.md) 
are written using the default jq syntax.

Workflow expressions have the following format:

```text
${ expression }
```

Where `expression` can be either an in-line expression, or a reference to a 
defined [expression function definition](#Using-Functions-For-Expression-Evaluation).

To reference a defined [expression function definition](#Using-Functions-For-Expression-Evaluation)
the expression must have the following format, for example:

```text
${ fn:myExprFuncName }
```

Where `fn` is the namespace of the defined expression functions and
`myExprName` is the unique expression function name.

To show some expression examples, let's say we have the following state data:

```json
{
    "applicant": {
      "name": "John Doe",
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
}
```

In our workflow model we can define our reusable expression function:

```json
{
"functions": [
  { 
    "name": "IsAdultApplicant",
    "operation": ".applicant | .age > 18",
    "type": "expression"
  }
]
}
```

We will get back to this function definition in just a bit, but now let's take a look at using 
an inline expression that sets an input parameter inside an action for example:

```json
{
"actions": [
    {
        "functionRef": {
            "refName": "confirmApplicant",
            "parameters": {
                "applicantName": "${ .applicant.name }"
            }
        }
    }
]
}
```

In this case our input parameter `applicantName` would be set to "John Doe".

Expressions can also be used to select and manipulate state data, this is in particularly useful for 
state data filters. 

For example let's use another inline expression:

```json
{
   "stateDataFilter": {
       "output": "${ .applicant | {applicant: .name, contactInfo: { email: .email, phone: .phoneNumbers }} }"
   }
}
```

This would set the data output of the particular state to:

```json
{
  "applicant": "John Doe",
  "contactInfo": {
    "email": "johndoe@something.com",
    "phone": [
      {
        "type": "iPhone",
        "number": "0123-4567-8888"
      },
      {
        "type": "home",
        "number": "0123-4567-8910"
      }
    ]
  }
}
```

[Switch state](#Switch-State) [conditions](#switch-state-dataconditions) require for expressions to be resolved to a boolean value (true / false). 

We can now get back to our previously defined "IsAdultApplicant" expression function and reference it:

```json
{
  "dataConditions": [ {
    "condition": "${ fn:IsAdultApplicant }",
    "transition": "StartApplication"
  }]
}
```

As previously mentioned, expressions are evaluated against certain subsets of data. For example 
the `parameters` param of the [functionRef definition](#FunctionRef-Definition) can evaluate expressions 
only against the data that is available to the [action](#Action-Definition) it belongs to.
One thing to note here are the top-level [workflow definition](#Workflow-Definition-Structure) parameters. Expressions defined
in them can only be evaluated against the initial [workflow data input](#Workflow-Data-Input).

For example let's say that we have a workflow data input of:

```json
{
   "inputVersion" : "1.0.0"
}
```

we can use this expression in the workflow "version" parameter:

```json
{
   "id": "MySampleWorkflow",
   "name": "Sample Workflow",
   "version": "${ .inputVersion }",
   "specVersion": "0.6"
}
```

which would set the workflow version to "1.0.0".
Note that the workflow "id" property value is not allowed to use an expression. The workflow 
definition "id" must be a constant value.

### Workflow Definition Structure

| Parameter | Description | Type | Required |
| --- | --- |  --- | --- |
| id | Workflow unique identifier | string | yes if `key` not defined |
| key | Domain-specific workflow identifier | string | yes if `id` not defined |
| name | Workflow name | string | yes |
| description | Workflow description | string | no |
| version | Workflow version | string | no |
| annotations | List of helpful terms describing the workflows intended purpose, subject areas, or other important qualities | string | no |
| dataInputSchema | Used to validate the workflow data input against a defined JSON Schema| string or object | no |
| [secrets](#Workflow-Secrets) | Workflow secrets | string or array | no |
| [globals](#Workflow-Globals) | Workflow globals | string or object | no |
| [start](#Start-Definition) | Workflow start definition | string | yes |
| specVersion | Serverless Workflow specification release version | string | yes |
| expressionLang | Identifies the expression language used for workflow expressions. Default value is "jq" | string | no |
| [execTimeout](#ExecTimeout-Definition) | Defines the execution timeout for a workflow instance | object | no |
| keepActive | If "true", workflow instances is not terminated when there are no active execution paths. Instance can be terminated with "terminate end definition" or reaching defined "execTimeout" | boolean | no |
| [events](#Event-Definition) | Workflow event definitions.  | array or string | no |
| [functions](#Function-Definition) | Workflow function definitions. Can be either inline function definitions (if array) or URI pointing to a resource containing json/yaml function definitions (if string) | array or string| no |
| [retries](#Retry-Definition) | Workflow retries definitions. Can be either inline retries definitions (if array) or URI pointing to a resource containing json/yaml retry definitions (if string) | array or string| no |
| [states](#State-Definition) | Workflow states | array | yes |
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
   "id": "sampleWorkflow",
   "version": "1.0",
   "specVersion": "0.6",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "start": "MyStartingState",
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
specVersion: '0.6'
name: Sample Workflow
description: Sample Workflow
start: MyStartingState
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

The `id` property defines the unique, domain-specific workflow identifier, for example "orders", "payment", etc.

The `key` property defines the unique, domain-specific workflow identifier.
It can be used when the `id` property is auto-generated by a content-management system for example.
In these cases, you can specify the `key` property to be the domain-specific identifier of the workflow definition.
The `id` and `key` properties are mutually exclusive, meaning you cannot define both.

The `name` property is the workflow logical name. 

The `description` property can be used to give further information about the workflow.

The `version` property can be used to provide a specific workflow version.

The `annotations` property defines a list of helpful terms describing the workflows intended purpose, subject areas, or other important qualities,
for example "machine learning", "monitoring", "networking", etc

The `dataInputSchema` property can be used to validate the workflow data input against a defined JSON Schema.
This check should be done before any states are executed. `dataInputSchema` can have two different types.
If it is an object type it has the following definition: 

```json
"dataInputSchema": {
   "schema": "URL_to_json_schema",
   "failOnValidationErrors": false
}
```
It's `schema` property is an URI which points to the JSON schema used to validate the workflow data input.
It' `failOnValidationErrors` property  determines if workflow execution should continue in case of validation
errors. The default value of `failOnValidationErrors` is `true`.
If `dataInputSchema` has the string type, it has the following definition:
```json
"dataInputSchema": "URL_to_json_schema"
```
In this case the `failOnValidationErrors` default value of `true` is assumed. 

The `dataInputSchema` property validates the [workflow data input](#Workflow-Data-Input). In case of 
a starting [Event state](#Event-state), it is not used to validate its event payloads.

The `secrets` property allows you to use sensitive information such as passwords, OAuth tokens, ssh keys, etc. inside your
Workflow expressions. 

It has two possible types, `string` or `array`. 
If `string` type, it is an URI pointing to a JSON or YAML document
which contains an array of names of the secrets, for example:

```json
"secrets": "file://workflowsecrets.json"
```

If `array` type, it defines an array (of string types) which contains the names of the secrets, for example:

```json
"secrets": ["MY_PASSWORD", "MY_STORAGE_KEY", "MY_ACCOUNT"]
```

For more information about Workflow secrets, reference the [Workflow Secrets section](#Workflow-Secrets).

The `globals` property can be used to define Workflow global values
which are accessible in [Workflow Expressions](#Workflow-Expressions).

It has two possible types, `string` or `object`.
If `string` type, it is an URI pointing to a JSON or YAML document
which contains an object of global definitions, for example:

```json
"globals": "file://workflowglobals.json"
```

If `object` type, it defines an array which contains the global definitions, for example:

```json
{
  "AGE": {
    "MIN_ADULT": 18
  }
}
```

For more information see the [Workflow Globals](#Workflow-Globals) section.

The `start` property defines the workflow starting information. For more information see the [start definition](#Start-Definition) section.

The `specVersion` property is used to set the Serverless Workflow specification release version 
the workflow markup adheres to.
It has to follow the specification release versions (excluding the leading "v"), meaning that for 
the [release version v0.6](https://github.com/serverlessworkflow/specification/releases/tag/v0.6) 
its value should be set to `"0.6"`.

The `expressionLang` property can be used to identify the expression language used for all expressions in
the workflow definition. The default value of this property is ["jq"](https://stedolan.github.io/jq/). 
You should set this property if you chose to define [workflow expressions](#Workflow-Expressions) 
with an expression language / syntax other than the default.

The `execTimeout` property is used to define execution timeout for a workflow instance.
For more information about this property and its use cases see the [execTimeout definition](#ExecTimeout-Definition) section.

The `functions` property can be either an in-line [function](#Function-Definition) definition array, or an URI reference to
a resource containing an array of [functions](#Function-Definition) definition. 
Referenced resource can be used by multiple workflow definitions.

Here is an example of using external resource for function definitions:

1. Workflow definition:
```json
{  
   "id": "sampleWorkflow",
   "version": "1.0",
   "specVersion": "0.6",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "start": "MyStartingState",
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
   "specVersion": "0.6",
   "name": "Sample Workflow",
   "description": "Sample Workflow",
   "start": "MyStartingState",
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

The `keepActive` property allows you to change the default behavior of workflow instances.
By default, as described in the [Core Concepts](#Core-Concepts) section, a workflow instance is terminated once there are no more 
active execution paths, one of its active paths ends in a "terminate" [end definition](#End-Definition), or when
its [`execTimeout`](#ExecTimeout-Definition) time is reached. 

Setting the `keepActive` property to "true" allows you to change this default behavior in that a workflow instance
created from this workflow definition can only be terminated if one of its active paths ends in a "terminate" [end definition](#End-Definition), or when
its [`execTimeout`](#ExecTimeout-Definition) time is reached. 
This allows you to explicitly model workflows where an instance should be kept alive, to collect (event) data for example.

You can reference the [specification examples](#Examples) to see the `keepActive` property in action.

#### Additional Properties

Specifying additional properties, namely properties which are not defined by the specification
are only allowed in the [Workflow Definition](#Workflow-Definition-Structure). 
Additional properties serve the same purpose as [Workflow Metadata](#Workflow-Metadata).
They allow you to enrich the workflow definition with custom information. 

Additional properties, just like workflow metadata, should not affect workflow execution. 
Implementations may choose to use additional properties or ignore them.

It is recommended to use workflow metadata instead of additional properties in the workflow definition.

Let's take a look at an example of additional properties:

```json
{  
  "id": "myworkflow",
  "version": "1.0",
  "specVersion": "0.6",
  "name": "My Test Workflow",
  "start": "My First State",
  "loglevel": "Info",
  "environment": "Production",
  "category": "Sales",
  "states": [ ... ]
}
```

In this example, we specify the `loglevel`, `environment`, and `category` additional properties.

Note the same can be also specified using workflow metadata, which is the preferred approach:

```json
{
  "id": "myworkflow",
  "version": "1.0",
  "specVersion": "0.6",
  "name": "Py Test Workflow",
  "start": "My First State",
  "metadata": {
    "loglevel": "Info",
    "environment": "Production",
    "category": "Sales"
  },
  "states": [ ... ]
}
```

#### ExecTimeout Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| duration | Timeout duration (ISO 8601 duration format) | string | yes |
| interrupt | If `false`, workflow instance is allowed to finish current execution. If `true`, current workflow execution is stopped immediately. Default is `false`  | boolean | no |
| runBefore | Name of a workflow state to be executed before workflow instance is terminated | string | no |


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
   "duration": "PT2M",
   "runBefore": "createandsendreport"
}
```

</td>
<td valign="top">

```yaml
duration: PT2M
runBefore: createandsendreport
```

</td>
</tr>
</table>

</details>

The `duration` property defines the time duration of the execution timeout. Once a workflow instance is created,
and the amount of the defined time is reached, the workflow instance should be terminated.

The `interrupt` property defines if the currently running instance should be allowed to finish its current 
execution flow before it needs to be terminated. If set to `true`, the current instance execution should stop immediately.

The `runBefore` property defines a name of a workflow state to be executed before workflow instance is terminated.
States referenced by `runBefore` (as well as any other states that they transition to) must obey following rules:
* They should not have any incoming transitions (should not be part of the main workflow control-flow logic)
* They cannot be states marked for compensation (have their `usedForCompensation` property and set to `true`)
* If it is a single state, it must define an [end definition](#End-Definition), if it transitions to other states,
at last one must define it.
* They can transition only to states are also not part of the main control flow logic (and are not marked 
for compensation).
 
Runtime implementations should raise compile time / parsing exceptions if any of the rules mentioned above are 
not obeyed in the workflow definition. 

#### Function Definition 

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique function name | string | yes |
| operation | If type is `rest`, <path_to_openapi_definition>#<operation_id>. If type is `rpc`, <path_to_grpc_proto_file>#<service_name>#<service_method>. If type is `graphql`, <url_to_graphql_endpoint>#<literal \"mutation\" or \"query\">#<query_or_mutation_name>. If type is `expression`, defines the workflow expression. | string | no |
| type | Defines the function type. Is either `rest`, `rpc` or `expression`. Default is `rest` | enum | no |
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

The `name` property defines an unique name of the function definition.

The `type` property defines the function type. Its value can be either `rest` or `expression`. Default value is `rest`.

Depending on the function `type`, the `operation` property can be:
* If `type` is `rest`, a combination of the function/service OpenAPI definition document URI and the particular service operation that needs to be invoked, separated by a '#'. 
  For example `https://petstore.swagger.io/v2/swagger.json#getPetById`. 
* If `type` is `rpc`, a combination of the gRPC proto document URI and the particular service name and service method name that needs to be invoked, separated by a '#'. 
For example `file://myuserservice.proto#UserService#ListUsers`.
* If `type` is `graphql`, a combination of the GraphQL schema definition URI and the particular service name and service method name that needs to be invoked, separated by a '#'. 
For example `file://myuserservice.proto#UserService#ListUsers`.
* If `type` is `expression`, defines the expression syntax. Take a look at the [workflow expressions section](#Workflow-Expressions) for more information on this.

The [`metadata`](#Workflow-Metadata) property allows users to define custom information to function definitions.
This allows you for example to define functions that describe of a command executions on a Docker image:

```yaml
functions:
- name: whalesayimage
  metadata:
    image: docker/whalesay
    command: cowsay
```

Note that using metadata for cases such as above heavily reduces the portability of your workflow markup.

Function definitions themselves do not define data input parameters. Parameters can be 
defined via the `parameters` property in [function definitions](#FunctionRef-Definition) inside [actions](#Action-Definition).

#### Event Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique event name | string | yes |
| source | CloudEvent source | string | yes if kind is set to "consumed", otherwise no |
| type | CloudEvent type | string | yes |
| kind | Defines the event is either `consumed` or `produced` by the workflow. Default is `consumed` | enum | no |
| [correlation](#Correlation-Definition) | Define event correlation rules for this event. Only used for consumed events | array | no |
| dataOnly | If `true` (default value), only the Event payload is accessible to consuming Workflow states. If `false`, both event payload and context attributes should be accessible | boolean | no |
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
Otherwise, (i.e., when the `kind` property is set to `consumed`), the `source` property must be defined in the event definition.

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

You can also correlate multiple events together. In the following example, we assume that the workflow consumes two different event types,
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

The `dataOnly` property deals with what Event data is accessible by the consuming Workflow states.
If its value is `true` (default value), only the Event payload is accessible to consuming Workflow states. 
If `false`, both Event payload and context attributes should be accessible.

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
| [onEvents](#eventstate-onevents) | Define the events to be consumed and optional actions to be performed | array | yes |
| [timeout](#eventstate-timeout) | Time period to wait for incoming events (ISO 8601 format). For example: "PT15M" (wait 15 minutes), or "P2DT3H4M" (wait 2 days, 3 hours and 4 minutes)| string | no |
| [stateDataFilter](#State-data-filters) | State data filter definition| object | no |
| [transition](#Transitions) | Next transition of the workflow after all the actions have been performed | object | yes |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
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
"exclusive": true,
"onEvents": [{
        "eventRefs": ["HighBodyTemperature"],
        "actions": [{
            "functionRef": {
                "refName": "sendTylenolOrder",
                "arguments": {
                    "patientid": "${ .patientId }"
                }
            }
        }]
    },
    {
        "eventRefs": ["HighBloodPressure"],
        "actions": [{
            "functionRef": {
                "refName": "callNurse",
                "arguments": {
                    "patientid": "${ .patientId }"
                }
            }
        }]
    },
    {
        "eventRefs": ["HighRespirationRate"],
        "actions": [{
            "functionRef": {
                "refName": "callPulmonologist",
                "arguments": {
                    "patientid": "${ .patientId }"
                }
            }
        }]
    }
],
"end": {
    "terminate": true
}
}
```

</td>
<td valign="top">

```yaml
name: MonitorVitals
type: event
exclusive: true
onEvents:
- eventRefs:
  - HighBodyTemperature
  actions:
  - functionRef:
      refName: sendTylenolOrder
      arguments:
        patientid: "${ .patientId }"
- eventRefs:
  - HighBloodPressure
  actions:
  - functionRef:
      refName: callNurse
      arguments:
        patientid: "${ .patientId }"
- eventRefs:
  - HighRespirationRate
  actions:
  - functionRef:
      refName: callPulmonologist
      arguments:
        patientid: "${ .patientId }"
end:
  terminate: true
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

If the Event state in this case is a workflow starting state, the occurrence of *any* of the defined events would start a new workflow instance.

<p align="center">
<img src="media/spec/event-state-exclusive-false.png" height="300px" alt="Event state with exclusive set to false"/>
</p>

If the Event state in this case is a workflow starting state, the occurrence of *all* defined events would start a new
 workflow instance.
  
In order to consider only events that are related to each other, we need to set the `correlation` property in the workflow
 [events definitions](#Event-Definition). This allows us to set up event correlation rules against the events 
 extension context attributes.

If the Event state is not a workflow starting state, the `timeout` property can be used to define the time duration from the 
invocation of the event state. If the defined event, or events have not been received during this time, 
the state should transition to the next state or can end the workflow execution (if it is an end state).

#### <a name="eventstate-onevents"></a> Event State: onEvents Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| eventRefs | References one or more unique event names in the defined workflow [events](#Event-Definition) | array | yes |
| actionMode | Specifies how actions are to be performed (in sequence of parallel). Default is "sequential" | string | no |
| [actions](#Action-Definition) | Actions to be performed | array | no |
| [eventDataFilter](#Event-data-filters) | Event data filter definition | object | no |

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
            "arguments": {
                "patientid": "${ .patientId }"
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
    arguments:
      patientid: "${ .patientId }"
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
	"onEvents": [{
		"eventRefs": ["HighBodyTemperature", "HighBloodPressure"],
		"actions": [{
				"functionRef": {
					"refName": "SendTylenolOrder",
					"arguments": {
						"patient": "${ .patientId }"
					}
				}
			},
			{
				"functionRef": {
					"refName": "CallNurse",
					"arguments": {
						"patient": "${ .patientId }"
					}
				}
			}
		]
	}]
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
Timeout values should always be represented as durations and not as specific time intervals.

The timeout property needs to be described in detail as it depends on whether or not the Event state is a workflow starting
state or not.

If the Event state is a workflow starting state, incoming events may trigger workflow instances. In this case,
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
        "terminate": true
    }
}
]
}
```

The first timeout would start once any of the referenced events are consumed. If the second event does not occur within
the defined timeout, no workflow instance should be created.

If the event state is not a workflow starting state, the `timeout` property is relative to the time when the
state becomes active. If the defined event conditions (regardless of the value of the exclusive property)
are not satisfied within the defined timeout period, the event state should transition to the next state or end the workflow
instance in case it is an end state without performing any actions.

#### Action Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Unique action name | string | no |
| [functionRef](#FunctionRef-Definition) | References a reusable function definition | object | yes if `eventRef` & `subFlowRef` are not defined |
| [eventRef](#EventRef-Definition) | References a `trigger` and `result` reusable event definitions | object | yes if `functionRef` & `subFlowRef` are not defined |
| [subFlowRef](#SubFlowRef-Definition) | References a workflow to be invoked | object or string | yes if `eventRef` & `functionRef` are not defined |
| timeout | Time period to wait for function execution to complete or the resultEventRef to be consumed (ISO 8601 format). For example: "PT15M" (15 minutes), or "P2DT3H4M" (2 days, 3 hours and 4 minutes)| string | no |
| [actionDataFilter](#Action-data-filters) | Action data filter definition | object | no |

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
        "arguments": {
            "applicantid": "${ .applicantId }"
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
  arguments:
    applicantid: "${ .applicantId }"
timeout: PT15M
```

</td>
</tr>
</table>

</details>

Actions specify invocations of services or other workflows during workflow execution.
Service invocation can be done in two different ways:

* Reference [functions definitions](#Function-Definition) by its unique name using the `functionRef` property.
* Reference a `produced` and `consumed` [event definitions](#Event-Definition) via the `eventRef` property.

In the event-based scenario a service or a set of services we want to invoke
are not exposed via a specific resource URI for example, but can only be invoked via events. 
The [eventRef](#EventRef-Definition) defines the 
referenced `produced` event via its `triggerEventRef` property and a `consumed` event via its `resultEventRef` property.

The `timeout` property defines the amount of time to wait for function execution to complete, or the consumed event referenced by the 
`resultEventRef` to become available.
It is described in ISO 8601 format, so for example "PT2M" would mean the maximum time for the function to complete
its execution is two minutes. 

Possible invocation timeouts should be handled via the states [onErrors](#Workflow-Error-Handling) definition.

##### Subflow action

Often you want to group your workflows into small logical units that solve a particular business problem and can be reused in 
multiple other workflow definitions.

<p align="center">
<img src="media/spec/subflowstateref.png" height="350px" alt="Referencing reusable workflow via SubFlow actions"/>
</p>

Reusable workflows are referenced by their `id` property via the SubFlow action `workflowId` parameter.

For the simple case, `subFlowRef` can be a string containing the `id` of the sub-workflow to invoke.  
If you want to specify other parameters then a [subFlowRef](#SubFlowRef-Definition) should be provided instead.

Each referenced workflow receives the SubFlow actions data as workflow data input.

Referenced sub-workflows must declare their own [function](#Function-Definition) and [event](#Event-Definition) definitions.


#### FunctionRef Definition

`FunctionRef` definition can have two types, either `string` or `object`.
If `string`, it defines the name of the referenced [function](#Function-Definition).
This can be used as a short-cut definition when you don't need to define any parameters, for example:

```json
"functionRef": "myFunction"
```

If you need to define parameters in your `functionRef` definition, you can define 
it with its `object` type which has the following properties:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| refName | Name of the referenced [function](#Function-Definition) | string | yes |
| arguments | Arguments (inputs) to be passed to the referenced function | object | yes if function type is `graphql`, otherwise no |
| selectionSet | Used if function type is `graphql`. String containing a valid GraphQL [selection set](https://spec.graphql.org/June2018/#sec-Selection-Sets) | string | yes if function type is `graphql`, otherwise no |

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
    "arguments": {
        "applicantid": "${ .applicantId }"
    }
}
```

</td>
<td valign="top">

```yaml
refName: finalizeApplicationFunction
arguments:
  applicantid: "${ .applicantId }"
```

</td>
</tr>
</table>

</details>

The `refName` property is the name of the referenced [function](#Function-Definition).

The `arguments` property defines the arguments that are to be passed to the referenced function.
Here is an example of using the `arguments` property:

```json
{
   "refName": "checkFundsAvailabe",
   "arguments": {
     "account": {
       "id": "${ .accountId }"
     },
     "forAmount": "${ .payment.amount }",
     "insufficientMessage": "The requested amount is not available."
   }
}
```

#### EventRef Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| [triggerEventRef](#Event-Definition) | Reference to the unique name of a `produced` event definition | string | yes |
| [resultEventRef](#Event-Definition) | Reference to the unique name of a `consumed` event definition | string | yes |
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
      "data": "${ .patientInfo }",
      "resultEventRef":  "VetAppointmentInfo"
   }
}
```

</td>
<td valign="top">

```yaml
eventRef:
  triggerEventRef: MakeVetAppointment
  data: "${ .patientInfo }"
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

#### SubFlowRef Definition

`SubFlowRef` definition can have two types, namely `string` or `object`.

If `string` type, it defines the unique id of the sub-workflow to be invoked. 
This short-hand definition can be used if sub-workflow lookup is done only by its `id`
property and not its `version` property and if the default value of `waitForCompletion` is assumed.

```json
"subFlowRef": "mySubFlowId"
```

If you need to define the `waitForCompletion` or the `version` properties, you can use its
`object` type:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| workflowId | Sub-workflow unique id | string | yes |
| waitForCompletion | If workflow execution must wait for sub-workflow to finish before continuing (default is true) | boolean | no |
| version | Sub-workflow version | string | no |

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
    "workflowId": "handleApprovedVisaWorkflowID",
    "version": "2.0"
}
```

</td>
<td valign="top">

```yaml
workflowId: handleApprovedVisaWorkflowID
version: '2.0'
```

</td>
</tr>
</table>

</details>

The `workflowId` property defined the unique ID of the sub-workflow to be invoked.

The `version` property defined the unique version of the sub-workflow to be invoked.
If this property is defined, runtimes should match both the `id` and the `version` properties
defined in the sub-workflow definition. 

The `waitForCompletion` property defines if the SubFlow action should wait until the referenced reusable workflow
has completed its execution. If it's set to "true" (default value), SubFlow action execution must wait until the referenced workflow has completed its execution.
In this case the workflow data output of the referenced workflow will be used as the result data of the action.
If it is set to "false" the parent workflow can continue its execution as soon as the referenced sub-workflow
has been invoked (fire-and-forget). For this case, the referenced (child) workflow data output will be ignored and the result data
of the action will be an empty json object (`{}`).

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
   "transition": "IssueRefundToCustomer"
}
```

</td>
<td valign="top">

```yaml
error: Item not in inventory
transition: IssueRefundToCustomer
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
| maxAttempts | Maximum number of retry attempts. Value of 0 means no retries are performed | string or number | no |
| maxDelay | Maximum amount of delay between retry attempts (ISO 8601 duration format) | string | no |
| increment | Static duration which will be added to the delay between successive retries (ISO 8601 duration format) | string | no |
| multiplier | Float value by which the delay is multiplied before each attempt. For example: "1.2" meaning that each successive delay is 20% longer than the previous delay.  For example, if delay is 'PT10S', then the delay between the first and second attempts will be 10 seconds, and the delay before the third attempt will be 12 seconds. | float or string | no |
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
can be referred by workflow states [error definitions](#Error-Definition).

The `delay` property specifies the initial time delay between retry attempts (ISO 8601 duration format).

The `increment` property specifies a static duration which will be added to the delay between successive retries.
To explain this better, let's say we have the following retry definition:
```json
{
  "name": "Timeout Errors Strategy",
  "delay": "PT10S",
  "increment": "PT2S",
  "maxAttempts": 4
}
```
which means that we will retry up to 4 times after waiting with increasing delay between attempts; 
in this example 10, 12, 14, and 16 seconds between retries.  

The `multiplier` property specifies the value by which the interval time is increased for each of the retry attempts.
To explain this better, let's say we have the following retry definition:

```json
{
  "name": "Timeout Errors Strategy",
  "delay": "PT10S",
  "multiplier": 2,
  "maxAttempts": 4
}
```
which means that we will retry up to 4 times after waiting with increasing delay between attempts; 
in this example 10, 20, 40, and 80 seconds between retries.  

The `maxAttempts` property determines the maximum number of retry attempts allowed and is a positive integer value.

The `jitter` property is important to prevent certain scenarios where clients
are retrying in sync, possibly causing or contributing to a transient failure
precisely because they're retrying at the same time. Adding a typically small,
bounded random amount of time to the period between retries serves the purpose
of attempting to prevent these retries from happening simultaneously, possibly
reducing total time to complete requests and overall congestion. How this value
is used in the exponential backoff algorithm is left up to implementations.

`jitter` may be specified as a percentage relative to the total delay. 
For example, if `interval` is 2 seconds, `multiplier` is 2 seconds, and we're at
the third attempt, there will be a delay of 6 seconds. If we set `jitter` to
0.3, then a random amount of time between 0 and 1.8 (`totalDelay * jitter == 6 * 0.3`)
will be added or subtracted from the delay.

Alternatively, `jitter` may be defined as an absolute value specified as an ISO
8601 duration. This way, the maximum amount of random time added is fixed and
will not increase as new attempts are made.

The `maxDelay` property determines the maximum amount of delay that is desired between retry attempts, and is applied 
after `increment`, `multiplier`, and `jitter`.

To explain this better, let's say we have the following retry definition:

```json
{
  "name": "Timeout Errors Strategy",
  "delay": "PT10S",
  "maxDelay": "PT100S",
  "multiplier": 4,
  "jitter": "PT1S",
  "maxAttempts": 4
}
```
which means that we will retry up to 4 times after waiting with increasing delay between attempts; 
in this example we might observe the following series of delays:
* 11s (min(`maxDelay`, (`delay` +/- rand(`jitter`)) => min(100, 10 + 1))
* 43s (min(`maxDelay`, (11s * `multiplier`) +/- rand(`jitter`)) => min(100, (11 * 4) - 1))
* 100s (min(`maxDelay`, (43s * `multiplier`) +/- rand(`jitter`)) => min(100, (43 * 4) + 0))
* 100s (min(`maxDelay`, (100s * `multiplier`) +/- rand(`jitter`)) => min(100, (100 * 4) - 1))  

For more information, refer to the [Workflow Error Handling](#Workflow-Error-Handling) sections.

#### Transition Definition

`Transition` definition can have two types, either `string` or `object`.
If `string`, it defines the name of the state to transition to.
This can be used as a short-cut definition when you don't need to define any other parameters, for example:

```json
"transition": "myNextState"
```

If you need to define additional parameters in your `transition` definition, you can define 
it with its `object` type which has the following properties:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| [nextState](#Transitions) | Name of the state to transition to next | string | yes |
| [compensate](#Workflow-Compensation) | If set to `true`, triggers workflow compensation before this transition is taken. Default is `false` | boolean | no |
| produceEvents | Array of [producedEvent](#ProducedEvent-Definition) definitions. Events to be produced before the transition takes place | array | no |

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
   "produceEvents": [{
       "eventRef": "produceResultEvent",
       "data": "${ .result.data }"
   }],
   "nextState": "EvalResultState"
}
```

</td>
<td valign="top">

```yaml
produceEvents:
- eventRef: produceResultEvent
  data: "${ .result.data }"
nextState: EvalResultState
```

</td>
</tr>
</table>

</details>

The `nextState` property defines the name of the state to transition to next.
The `compensate` property allows you to trigger [compensation](#Workflow-Compensation) before the transition (if set to true).
The `produceEvents` property allows you to define a list of events to produce before the transition happens.

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
| [stateDataFilter](#State-data-filters) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after all the actions have been performed | object | yes (if end is not defined) |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
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
                "arguments": {
                    "customer": "${ .customer }"
                }
            }
        }
    ],
    "end": true
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
    arguments:
      customer: "${ .customer }"
end: true
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
| [stateDataFilter](#State-data-filters) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| eventTimeout | If eventConditions is used, defines the time period to wait for events (ISO 8601 format). For example: "PT15M" (15 minutes), or "P2DT3H4M" (2 days, 3 hours and 4 minutes)| string | yes only if eventConditions is defined |
| defaultCondition | Default transition of the workflow if there is no matching data conditions or event timeout is reached. Can be a transition or end definition | object | yes |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
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
     "name":"CheckVisaStatus",
     "type":"switch",
     "eventConditions": [
        {
          "eventRef": "visaApprovedEvent",
          "transition": "HandleApprovedVisa"
        },
        {
          "eventRef": "visaRejectedEvent",
          "transition": "HandleRejectedVisa"
        }
     ],
     "eventTimeout": "PT1H",
     "defaultCondition": {
        "transition": "HandleNoVisaDecision"
     }
}
```

</td>
<td valign="top">

```yaml
name: CheckVisaStatus
type: switch
eventConditions:
- eventRef: visaApprovedEvent
  transition: HandleApprovedVisa
- eventRef: visaRejectedEvent
  transition: HandleRejectedVisa
eventTimeout: PT1H
defaultCondition:
  transition: HandleNoVisaDecision
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
are ordered in both JSON and YAML. For example, let's say there are two `true` conditions: A and B, defined in that order.
Because A was defined first, its transition will be executed, not B's.

In case of data-based conditions definition, switch state controls workflow transitions based on the states data.
If no defined conditions can be matched, the state transitions is taken based on the `defaultCondition` property.
This property can be either a `transition` to another workflow state, or an `end` definition meaning a workflow end.

For event-based conditions, a switch state acts as a workflow wait state. It halts workflow execution 
until one of the referenced events arrive, then making a transition depending on that event definition.
If events defined in event-based conditions do not arrive before the states `eventTimeout` property expires, 
 state transitions are based on the defined `defaultCondition` property.

#### <a name="switch-state-dataconditions"></a>Switch State: Data Conditions

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Data condition name | string | no |
| [condition](#Workflow-Expressions) | Workflow expression evaluated against state data. Must evaluate to true or false | string | yes |
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
      "condition": "${ .applicant | .age >= 18 }",
      "transition": "StartApplication"
}
```

</td>
<td valign="top">

```yaml
name: Eighteen or older
condition: "${ .applicant | .age >= 18 }"
transition: StartApplication
```

</td>
</tr>
</table>

</details>

Switch state data conditions specify a data-based condition statement, which causes a transition to another 
workflow state if evaluated to true.
The `condition` property of the condition defines an expression (e.g., `${ .applicant | .age > 18 }`), which selects
parts of the state data input. The condition must evaluate to `true` or `false`.

If the condition is evaluated to `true`, you can specify either the `transition` or `end` definitions
to decide what to do, transition to another workflow state, or end workflow execution.

#### <a name="switch-state-eventconditions"></a>Switch State: Event Conditions

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Event condition name | string | no |
| eventRef | References an unique event name in the defined workflow events | string | yes |
| [transition](#Transitions) or [end](#End-Definition) | Defines what to do if condition is true. Transition to another state, or end workflow | object | yes |
| [eventDataFilter](#Event-data-filters) | Event data filter definition | object | no |
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
      "transition": "HandleApprovedVisa"
}
```

</td>
<td valign="top">

```yaml
name: Visa approved
eventRef: visaApprovedEvent
transition: HandleApprovedVisa
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

The `eventDataFilter` property can be used to filter event data when it is received.

#### Delay State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name |State name | string | yes |
| type |State type | string | yes |
| timeDelay |Amount of time (ISO 8601 format) to delay when in this state. For example: "PT15M" (delay 15 minutes), or "P2DT3H4M" (delay 2 days, 3 hours and 4 minutes) | integer | yes |
| [stateDataFilter](#State-data-filters) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after the delay | object | yes (if end is not defined) |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
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
      "transition": "GetJobStatus"
}
```

</td>
<td valign="top">

```yaml
name: WaitForCompletion
type: delay
timeDelay: PT5S
transition: GetJobStatus
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
| completionType | Option types on how to complete branch execution. Default is "allOf" | enum | no |
| numCompleted | Used when branchCompletionType is set to `atLeast` to specify the least number of branches that must complete in order for the state to transition/end. | string or number | no |
| [stateDataFilter](#State-data-filters) | State data filter | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after all branches have completed execution | object | yes (if end is not defined) |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
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
     "completionType": "allOf",
     "branches": [
        {
          "name": "Branch1",
          "actions": [
            {
                "functionRef": {
                    "refName": "functionNameOne",
                    "arguments": {
                        "order": "${ .someParam }"
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
                      "arguments": {
                          "order": "${ .someParam }"
                      }
                  }
              }
          ]
        }
     ],
     "end": true
}
```

</td>
<td valign="top">

```yaml
name: ParallelExec
type: parallel
completionType: allOf
branches:
- name: Branch1
  actions:
  - functionRef:
      refName: functionNameOne
      arguments:
        order: "${ .someParam }"
- name: Branch2
  actions:
  - functionRef:
      refName: functionNameTwo
      arguments:
        order: "${ .someParam }"
end: true
```

</td>
</tr>
</table>

</details>

Parallel state defines a collection of `branches` that are executed in parallel.
A parallel state can be seen a state which splits up the current workflow instance execution path
into multiple ones, one for each branch. These execution paths are performed in parallel
and are joined back into the current execution path depending on the defined `completionType` parameter value.

The "completionType" enum specifies the different ways of completing branch execution:
* allOf: All branches must complete execution before the state can transition/end. This is the default value in case this parameter is not defined in the parallel state definition.
* atLeast: State can transition/end once at least the specified number of branches have completed execution. In this case you must also
specify the `numCompleted` property to define this number.

Exceptions may occur during execution of branches of the Parallel state, this is described in detail in [this section](#parallel-state-exceptions).

#### <a name="parallel-state-branch"></a>Parallel State: Branch

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| name | Branch name | string | yes |
| [actions](#Action-Definition) | Actions to be executed in this branch | array | yes |

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
                  "arguments": {
                      "order": "${ .someParam }"
                  }
              }
          },
          {
              "functionRef": {
                  "refName": "functionNameTwo",
                  "arguments": {
                      "order": "${ .someParamTwo }"
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
    arguments:
      order: "${ .someParam }"
- functionRef:
    refName: functionNameTwo
    arguments:
      order: "${ .someParamTwo }"
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

If the parallel states defines a subflow action, exceptions that occur during execution of the called workflow
can chose to handle exceptions on their own. All unhandled exceptions from the called workflow
execution however are propagated back to the parallel state and can be handled with the parallel states
`onErrors` definition.

Note that once an error that is propagated to the parallel state from a branch and handled by the 
states `onErrors` definition is handled (its associated transition is taken) no further errors from branches of this 
parallel state should be considered as the workflow control flow logic has already moved to a different state. 

For more information, see the [Workflow Error Handling](#Workflow-Error-Handling) sections.

#### Inject State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| data | JSON object which can be set as state's data input and can be manipulated via filter | object | yes |
| [stateDataFilter](#state-data-filters) | State data filter | object | no |
| [transition](#Transitions) | Next transition of the workflow after injection has completed | object | yes (if end is set to false) |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
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
     "data": {
        "result": "Hello"
     },
     "transition": "World"
}
```

</td>
<td valign="top">

```yaml
name: Hello
type: inject
data:
  result: Hello
transition: World
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
   "transition": "GreetPersonState"
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
  transition: GreetPersonState
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
        "output": "${ {people: [.people[] | select(.age < 40)]} }"
     },
     "transition": "GreetPersonState"
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
    output: "${ {people: [.people[] | select(.age < 40)]} }"
  transition: GreetPersonState
```

</td>
</tr>
</table>

In which case the states data output would include only people whose age is less than 40:

```json
{
  "people": [
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
}
```

You can change your output path easily during testing, for example change the expression to:

```text
${ {people: [.people[] | select(.age >= 40)]} }
```

This allows you to test if your workflow behaves properly for cases when there are people whose age is greater or equal 40.

#### ForEach State

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| id | Unique state id | string | no |
| name | State name | string | yes |
| type | State type | string | yes |
| inputCollection | Workflow expression selecting an array element of the states data | string | yes |
| outputCollection | Workflow expression specifying an array element of the states data to add the results of each iteration | string | no |
| iterationParam | Name of the iteration parameter that can be referenced in actions/workflow. For each parallel iteration, this param should contain an unique element of the inputCollection array | string | yes |
| max | Specifies how upper bound on how many iterations may run in parallel | string or number | no |
| [actions](#Action-Definition) | Actions to be executed for each of the elements of inputCollection | array | yes |
| [stateDataFilter](#State-data-filters) | State data filter definition | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after state has completed | object | yes (if end is not defined) |
| [compensatedBy](#Workflow-Compensation) | Unique name of a workflow state which is responsible for compensation of this state | String | no |
| [usedForCompensation](#Workflow-Compensation) | If true, this state is used to compensate another state. Default is "false" | boolean | no |
| [metadata](#Workflow-Metadata) | Metadata information| object | no |
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
    "inputCollection": "${ .orders }",
    "iterationParam": "singleorder",
    "outputCollection": "${ .provisionresults }",
    "actions": [
        {
            "functionRef": {
                "refName": "provisionOrderFunction",
                "arguments": {
                    "order": "${ .singleorder }"
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
inputCollection: "${ .orders }"
iterationParam: "singleorder"
outputCollection: "${ .provisionresults }"
actions:
- functionRef:
    refName: provisionOrderFunction
    arguments:
      order: "${ .singleorder }"
```

</td>
</tr>
</table>

</details>

ForEach states can be used to execute [actions](#Action-Definition) for each element of a data set.

Each iteration of the ForEach state should be executed in parallel.

You can use the `max` property to set the upper bound on how many iterations may run in parallel. The default
of the `max` property is zero, which places no limit on number of parallel executions.

The `inputCollection` property is a workflow expression which selects an array in the states data. All iterations 
are performed against data elements of this array. If this array does not exist, the runtime should throw 
an error. This error can be handled inside the states [`onErrors`](#Error-Definition) definition.

The `outputCollection` property is a workflow expression which selects an array in the state data where the results
of each iteration should be added to. If this array does not exist, it should be created.

The `iterationParam` property defines the name of the iteration parameter passed to each parallel execution of the ForEach state.
It should contain the unique element of the `inputCollection` array and passed as data input to the actions/workflow defined.
`iterationParam` should be created for each iteration, so it can be referenced/used in defined actions / workflow data input.

The `actions` property defines actions to be executed in each state iteration.

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
  "specVersion": "0.6",
  "start": "SendConfirmState",
  "functions": [
  {
    "name": "sendConfirmationFunction",
    "operation": "file://confirmationapi.json#sendOrderConfirmation"
  }
  ],
  "states": [
  {
      "name":"SendConfirmState",
      "type":"foreach",
      "inputCollection": "${ [.orders[] | select(.completed == true)] }",
      "iterationParam": "completedorder",
      "outputCollection": "${ .confirmationresults }",
      "actions":[  
      {  
       "functionRef": {
         "refName": "sendConfirmationFunction",
         "arguments": {
           "orderNumber": "${ .completedorder.orderNumber }",
           "email": "${ .completedorder.email }"
         }
       }
      }],
      "end": true
  }]
}
```

</td>
<td valign="top">

```yaml
id: sendConfirmWorkflow
name: SendConfirmationForCompletedOrders
version: '1.0'
specVersion: '0.6'
start: SendConfirmState
functions:
- name: sendConfirmationFunction
  operation: file://confirmationapi.json#sendOrderConfirmation
states:
- name: SendConfirmState
  type: foreach
  inputCollection: "${ [.orders[] | select(.completed == true)] }"
  iterationParam: completedorder
  outputCollection: "${ .confirmationresults }"
  actions:
  - functionRef:
      refName: sendConfirmationFunction
      arguments:
        orderNumber: "${ .completedorder.orderNumber }"
        email: "${ .completedorder.email }"
  end: true
```

</td>
</tr>
</table>

The workflow data input containing order information is passed to the `SendConfirmState` [ForEach](#ForEach-State) state.
The ForEach state defines an `inputCollection` property which selects all orders that have the `completed` property set to `true`.

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
| [eventDataFilter](#Event-data-filters) | Callback event data filter definition | object | no |
| [stateDataFilter](#State-data-filters) | State data filter definition | object | no |
| [onErrors](#Error-Definition) | States error handling and retries definitions | array | no |
| [transition](#Transitions) | Next transition of the workflow after callback event has been received | object | yes |
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
        "action": {
            "functionRef": {
                "refName": "callCreditCheckMicroservice",
                "arguments": {
                    "customer": "${ .customer }"
                }
            }
        },
        "eventRef": "CreditCheckCompletedEvent",
        "timeout": "PT15M",
        "transition": "EvaluateDecision"
}
```

</td>
<td valign="top">

```yaml
name: CheckCredit
type: callback
action:
  functionRef:
    refName: callCreditCheckMicroservice
    arguments:
      customer: "${ .customer }"
eventRef: CreditCheckCompletedEvent
timeout: PT15M
transition: EvaluateDecision
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

Can be either `string` or `object` type. If type string, it defines the name of the workflow starting state.

```json
"start": "MyStartingState"
```
In this case it's assumed that the `schedule` property is not defined.

If the start definition is of type `object`, it has the following structure:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| stateName | Name of the starting workflow state | object | yes |
| [schedule](#Schedule-Definition) | Define the recurring time intervals or cron expressions at which workflow instances should be automatically started. | object | yes |

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
  "stateName": "MyStartingstate",
  "schedule": "2020-03-20T09:00:00Z/2020-03-20T15:00:00Z"
}
```

</td>
<td valign="top">

```yaml
stateName: MyStartingstate
schedule: 2020-03-20T09:00:00Z/2020-03-20T15:00:00Z
```

</td>
</tr>
</table>

</details>

Start definition explicitly defines how/when workflow instances should be created and what the workflow starting state is.

The start definition can be either `string` or `object` type.

If `string` type, it defines the name of the workflow starting state.

If `object` type, it provides the ability to set the workflow starting state name, as well as the `schedule` property.

The `schedule` property allows to define scheduled workflow instance creation. 
Scheduled starts have two different choices. You can define a recurring time interval or cron-based schedule at which a workflow 
instance **should** be created (automatically). 

You can also define cron-based scheduled starts, which allows you to specify periodically started workflow instances based on a [cron](http://crontab.org/) definition.
Cron-based scheduled starts can handle absolute time intervals (i.e., not calculated in respect to some particular point in time).
One use case for cron-based scheduled starts is a workflow that performs periodical data batch processing. 
In this case we could use a cron definition

``` text
0 0/5 * * * ?
```

to define that a workflow instance from the workflow definition should be created every 5 minutes, starting at full hour. 

Here are some more examples of cron expressions and their meanings:

``` text
* * * * *   - Create workflow instance at the top of every minute
0 * * * *   - Create workflow instance at the top of every hour
0 */2 * * * - Create workflow instance every 2 hours
0 9 8 * *   - Create workflow instance at 9:00:00AM on the eighth day of every month
```

[See here](http://crontab.org/) to get more information on defining cron expressions.

One thing to discuss when dealing with cron-based scheduled starts is when the workflow starting state is an [Event](#Event-State).
Event states define that workflow instances are triggered by the existence of the defined event(s). 
Defining a cron-based scheduled starts for the runtime implementations would mean that there needs to be an event service that issues 
the needed events at the defined times to trigger workflow instance creation.

#### Schedule Definition

`Schedule` definition can have two types, either `string` or `object`.
If `string` type, it defines time interval describing when the workflow instance should be automatically created.
This can be used as a short-cut definition when you don't need to define any other parameters, for example:

```json
{
  "schedule": "R/PT2H"
}
```

If you need to define the `cron` or the `timezone` parameters in your `schedule` definition, you can define 
it with its `object` type which has the following properties:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| interval | A recurring time interval expressed in the derivative of ISO 8601 format specified below. Declares that workflow instances should be automatically created at the start of each time interval in the series. | string | yes if `cron` not defined |
| [cron](#Cron-Definition) | Cron expression defining when workflow instances should be automatically created | object | yes if `interval` not defined |
| timezone | Timezone name used to evaluate the interval & cron-expression. If the interval specifies a date-time w/ timezone then proper timezone conversion will be applied. (default: UTC). | string | no |

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
   "cron": "0 0/15 * * * ?"
}
```

</td>
<td valign="top">

```yaml
cron: 0 0/15 * * * ?
```

</td>
</tr>
</table>

</details>

The `interval` property uses a derivative of ISO 8601 recurring time interval format to describe a series of consecutive time intervals for workflow instances to be automatically created at the start of. Unlike full ISO 8601, this derivative format does not allow expression of an explicit number of recurrences or identification of a series by the date and time at the start and end of its first time interval.
There are three ways to express a recurring interval:

1. `R/<Start>/<Duration>`: Defines the start time and a duration, for example: "R/2020-03-20T13:00:00Z/PT2H", meaning workflow 
instances will be automatically created every 2 hours starting from March 20th 2020 at 1pm UTC.
2. `R/<Duration>/<End>`: Defines a duration and an end, for example: "R/PT2H/2020-05-11T15:30:00Z", meaning that workflow instances will be 
automatically created every 2 hours until until May 11th 2020 at 3:30pm UTC (i.e., the last instance will be created 2 hours prior to that, at 1:30pm UTC).
3. `R/<Duration>`: Defines a duration only, for example: "R/PT2H", meaning workflow instances will be automatically created every 2 hours. The start time of the first interval may be indeterminate, but should be delayed by no more than the specified duration and must repeat on schedule after that (this is effectively supplying the start time "out-of-band" as permitted ISO ISO 8601-1:2019 section 5.6.1 NOTE 1). Each runtime implementation should document how the start time for a duration-only interval is established.

The `cron` property uses a [cron expression](http://crontab.org/) 
to describe a repeating interval upon which a workflow instance should be created automatically.
For more information see the [cron definition](#Cron-Definition) section.

The `timezone` property is used to define a time zone name to evaluate the cron or interval expression against. If not specified, it should default 
to UTC time zone. See [here](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) for a list of timezone names.  For ISO 8601 date time 
values in `interval` or `cron.validUntil`, runtimes should treat `timezone` as the 'local time' (UTC if `interval` is not defined by the user).

Note that when the workflow starting state is an [Event](#Event-State) 
defining cron-based scheduled starts for the runtime implementations would mean that there needs to be an event service that issues 
the needed events at the defined times to trigger workflow instance creation.

#### Cron Definition

`Cron` definition can have two types, either `string` or `object`.
If `string` type, it defines the cron expression describing when the workflow instance should be created (automatically).
This can be used as a short-cut definition when you don't need to define any other parameters, for example:

```json
{
  "cron": "0 15,30,45 * ? * *"
}
```

If you need to define the `validUntil` parameters in your `cron` definition, you can define 
it with its `object` type which has the following properties:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| expression | Cron expression describing when the workflow instance should be created (automatically) | string | yes |
| validUntil | Specific date and time (ISO 8601 format) when the cron expression is no longer valid | string | no |

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
    "expression": "0 15,30,45 * ? * *",
    "validUntil": "2021-11-05T08:15:30-05:00"
}
```

</td>
<td valign="top">

```yaml
expression: 0 15,30,45 * ? * *
validUntil: '2021-11-05T08:15:30-05:00'
```

</td>
</tr>
</table>

</details>

The `expression` property is a a [cron expression](http://crontab.org/) which defines 
when workflow instances should be created (automatically).

The `validUntil` property defines a date and time (using ISO 8601 format). When the 
`validUntil` time is reached, the cron expression for instances creations of this workflow 
should no longer be valid.

For example let's say we have to following cron definitions:

```json
{
    "expression": "0 15,30,45 * ? * *",
    "validUntil": "2021-11-05T08:15:30-05:00"
}
```

This tells the runtime engine to create an instance of this workflow every hour 
at minutes 15, 30 and 45. This is to be done until November 5, 2021, 8:15:30 am, US Eastern Standard Time
as defined by the `validUntil` property value.

#### End Definition

Can be either `boolean` or `object` type. If type boolean, must be set to `true`, for example:

```json
"end": true
```
In this case it's assumed that the `terminate` property has its default value of `false`, and the `produceEvents` and 
`compensate` properties are not defined.

If the end definition is of type `object`, it has the following structure:

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| terminate | If true, terminates workflow instance execution | boolean | no |
| produceEvents | Array of [producedEvent](#ProducedEvent-Definition) definitions. Defines events that should be produced. | array | no |
| [compensate](#Workflow-Compensation) | If set to `true`, triggers workflow compensation before workflow execution completes. Default is `false` | boolean | no |

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
    "terminate": true,
    "produceEvents": [{
        "eventRef": "provisioningCompleteEvent",
        "data": "${ .provisionedOrders }"
    }]
}
```

</td>
<td valign="top">

```yaml
terminate: true
produceEvents:
- eventRef: provisioningCompleteEvent
  data: "${ .provisionedOrders }"

```

</td>
</tr>
</table>

</details>

End definitions are used to explicitly define execution completion of a workflow instance or workflow execution path.
A workflow definition must include at least one [workflow state](#State-Definition).
Note that [Switch states](#Switch-State) cannot declare to be workflow end states. Switch states must end
their execution followed by a transition another workflow state, given their conditional evaluation.


The `terminate` property, if set to `true`, completes the workflow instance execution, this any other active 
execution paths.
If a terminate end is reached inside a ForEach or Parallel state the entire workflow instance is terminated.

The [`produceEvents`](#ProducedEvent-Definition) allows defining events which should be produced
by the workflow instance before workflow stops its execution.

It's important to mention that if the workflow `keepActive` property is set to`true`, 
the only way to complete execution of the workflow instance 
is if workflow execution reaches a state that defines an end definition with `terminate` property set to `true`,
or, if the [execution timeout](#ExecTimeout-Definition) property is defined, the time defined in its `interval`
is reached.

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
    "data": "${ .provisionedOrders }",
    "contextAttributes": [{
         "buyerId": "${ .buyerId }"
     }]
 }
```

</td>
<td valign="top">

```yaml
eventRef: provisioningCompleteEvent
data: "${ .provisionedOrders }"
contextAttributes:
- buyerId: "${ .buyerId }"
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

Transitions can trigger compensation via their `compensate` property. See the [Workflow Compensation](#Workflow-Compensation)
section for more information.

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
    "transition": "ReimburseCustomer"
  },
  {
    "error": "*",
    "transition": "handleAnyOtherError"
  }
]
}
```

</td>
<td valign="top">

```yaml
onErrors:
- error: Item not in inventory
  transition: ReimburseCustomer
- error: "*"
  transition: handleAnyOtherError
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
    "transition": "handleAllErrors"
  }
]
}
```

</td>
<td valign="top">

```yaml
onErrors:
- error: "*"
  transition: handleAllErrors
```

</td>
</tr>
</table>

#### Defining Retries

Retries are related to errors. When certain errors are encountered we might want to retry the states execution.

We can define retries within the workflow states [error definitions](#Defining-Errors).
This is done by defining the [retry strategy](#Retry-Definition) as the workflow top-level parameter using its `retries` array, and then
adding a `retryRef` parameter to the error definition which references these retry strategies for a specific error. 

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

Different states now can use the defined retry strategy. For example:

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
    "transition": "ReimburseCustomer"
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
  transition: ReimburseCustomer
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
purchase. In the case customer laster on decides to cancel this purchase we need to undo it. One way of 
doing that is to credit the customer $100.

It's important to understand that compensation with workflows is not the same as for example rolling back
a transaction (a strict undo). Compensating a workflow state which has successfully completed 
might involve multiple logical steps and thus is part of the overall business logic that must be 
defined within the workflow itself. To explain this let's use our previous example and say that when our
customer made the item purchase, our workflow has sent her/him a confirmation email. In the case, to 
compensate this purchase, we cannot just "undo" the confirmation email sent. Instead, we want to 
send a second email to the customer which includes purchase cancellation information.

Compensation in Serverless Workflow must be explicitly defined by the workflow control flow logic.
It cannot be dynamically triggered by initial workflow data, event payloads, results of service invocations, or 
errors.

#### Defining Compensation

Each workflow state can define how it should be compensated via its `compensatedBy` property.
This property references another workflow state (by its unique name) which is responsible for the actual compensation.

States referenced by `compensatedBy` (as well as any other states that they transition to) must obey following rules:
* They should not have any incoming transitions (should not be part of the main workflow control-flow logic)
* They cannot be an [event state](#Event-State)
* They cannot define an [end definition](#End-definition). If they do, it should be ignored
* They must define the `usedForCompensation` property and set it to `true`
* They can transition only to states which also have their `usedForCompensation` property and set to `true`
* They cannot themselves set their `compensatedBy` property to true (compensation is not recursive)
 
Runtime implementations should raise compile time / parsing exceptions if any of the rules mentioned above are 
not obeyed in the workflow definition. 

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
                    "arguments": {
                        "customerid": "${ .purchase.customerid }",
                        "amount": "${ .purchase.amount }"
                    }
                  }
                },
                {
                  "functionRef": {
                    "refName": "SendPurchaseConfirmationEmailFunction",
                    "arguments": {
                        "customerid": "${ .purchase.customerid }"
                    }
                  }
                }
              ]
            }
          ],
          "compensatedBy": "CancelPurchase",
          "transition": "SomeNextWorkflowState"
      },
      {
        "name": "CancelPurchase",
        "type": "operation",
        "usedForCompensation": true,
        "actions": [
            {
              "functionRef": {
                "refName": "CreditCustomerFunction",
                "arguments": {
                    "customerid": "${ .purchase.customerid }",
                    "amount": "${ .purchase.amount }"
                }
              }
            },
            {
              "functionRef": {
                "refName": "SendPurchaseCancellationEmailFunction",
                "arguments": {
                    "customerid": "${ .purchase.customerid }"
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
        arguments:
          customerid: "${ .purchase.customerid }"
          amount: "${ .purchase.amount }"
    - functionRef:
        refName: SendPurchaseConfirmationEmailFunction
        arguments:
          customerid: "${ .purchase.customerid }"
  compensatedBy: CancelPurchase
  transition: SomeNextWorkflowState
- name: CancelPurchase
  type: operation
  usedForCompensation: true
  actions:
  - functionRef:
      refName: CreditCustomerFunction
      arguments:
        customerid: "${ .purchase.customerid }"
        amount: "${ .purchase.amount }"
  - functionRef:
      refName: SendPurchaseCancellationEmailFunction
      arguments:
        customerid: "${ .purchase.customerid }"
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
      "compensate": true,
      "nextState": "NextWorkflowState"
  }
}
```
</td>
<td valign="top">

```yaml
transition:
  compensate: true
  nextState: NextWorkflowState
```
</td>
</tr>
</table>

Transitions can trigger compensations by specifying the `compensate` property and setting it to `true`.
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
    "compensate": true
  }
}
```
</td>
<td valign="top">

```yaml
end:
  compensate: true
```
</td>
</tr>
</table>

End definitions can trigger compensations by specifying the `compensate` property and setting it to `true`.
This means that before workflow finishes its execution workflow compensation must be performed. Note that 
in case when the end definition has its `produceEvents` property set, compensation must be performed before 
producing the specified events and ending workflow execution.

#### Compensation Execution Details

Now that we have seen how to define and trigger compensation, we need to go into details on how compensation should be executed.
Compensation is performed on all already successfully completed states (that define `compensatedBy`) in **reverse** order.
Compensation is always done in sequential order, and should not be executed in parallel.

Let's take a look at the following workflow image:

<p align="center">
<img src="media/spec/compensation-exec.png" height="400px" alt="Compensation Execution Example"/>
</p> 

In this example lets say our workflow execution is at the "End" state which defines the `compensate` property to true
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
<img src="media/spec/compensation-exec2.png" height="200px" alt="Compensation Execution Example 2"/>
</p> 

In our example, when compensation triggers,
the current workflow data is passed as input to the "D1" state, the first compensation state for our example. 
The states data output is then passed as states data input to "B1", and so on.

#### Compensation and Active States

In some cases when compensation is triggered, some states such as [Parallel](#Parallel-State) and [ForEach](#ForEach-State)
states can still be "active", meaning they still might have some async executions that are being performed.

If compensation needs to performed on such still active states, the state execution must be first cancelled.
After it is cancelled, compensation should be performed.

#### Unrecoverable errors during compensation

States that are marked as `usedForCompensation` can define [error handling](#Workflow-Error-Handling) via their
`onErrors` property just like any other workflow states. In case of unrecoverable errors during their execution
(errors not explicitly handled),
workflow execution should be stopped, which is the same behavior as when not using compensation as well. 

### Workflow Versioning

In any application, regardless of size or type, one thing is for sure: changes happen.
Versioning your workflow definitions is an important task to consider. Versions indicate 
changes or updates of your workflow definitions to the associated execution runtimes. 

There are two places in the [workflow definition](#Workflow-Definition-Structure) where versioning can be applied:

1. Top level workflow definition `version` property.
2. Actions [subflowRef](#SubFlowRef-Definition) `version` property.

The Serverless Workflow specification does not mandate a specific versioning strategy
for the top level and actions subflowRef definitions `version` properties. It does not mandate the use 
of a versioning strategy at all. We do recommend however that you do use a versioning strategy 
for your workflow definitions especially in production environments. 

To enhance portability when using versioning of your workflow and sub-workflow definitions,
we recommend using an existing versioning standard such as [SemVer](https://semver.org/) for example.

### Workflow Globals

Workflow globals are used to define static data which is available to [Workflow Expressions](#Workflow-Expressions).

Globals can be defined via the [Workflow top-level "globals" property](#Workflow-Definition-Structure),
for example:

```json
"globals": {
  "Translations": {
    "Dog": {
      "Serbian": "pas",
      "Spanish": "perro",
      "French": "chien"
    }
  }
}
```

Globals can only be accessed inside Workflow expressions via the $GLOBALS namespace.
Runtimes must make globals available to expressions under that namespace.

Here is an example of using globals in Workflow expressions:

```json
{
...,
"globals": {
  "AGE": {
    "MIN_ADULT": 18
  }
},
...
"states":[  
  {  
     "name":"CheckApplicant",
     "type":"switch",
     "dataConditions": [
        {
          "name": "Applicant is adult",
          "condition": "${ .applicant | .age >= $GLOBALS.AGE.MIN_ADULT }",
          "transition": "ApproveApplication"
        },
        {
          "name": "Applicant is minor",
          "condition": "${ .applicant | .age < $GLOBALS.AGE.MIN_ADULT }",
          "transition": "RejectApplication"
        }
     ],
     ...
  },
  ...
]
}
```
Note that globals can also be used in [expression functions](#Using-Functions-for-Expression-Evaluation),
for example:

```json
{
"functions": [
  {
    "name": "isAdult",
    "operation": ".applicant | .age >= $GLOBALS.AGE.MIN_ADULT",
    "type": "expression"
  },
  {
    "name": "isMinor",
    "operation": ".applicant | .age < $GLOBALS.AGE.MIN_ADULT",
    "type": "expression"
  }
]
}
```

Workflow globals values should only contain static data, meaning that their value should not 
contain Workflow expressions. 
Workflow globals should not have access to [Workflow secrets definitions](#Workflow-Secrets).

### Workflow Secrets

Secrets allow you access sensitive information, such as passwords, OAuth tokens, ssh keys, etc
inside your [Workflow Expressions](#Workflow-Expressions). 

You can define the names of secrets via the [Workflow top-level "secrets" property](#Workflow-Definition-Structure), 
for example:

```json
"secrets": ["MY_PASSWORD", "MY_STORAGE_KEY", "MY_ACCOUNT"]
```

If secrets are defined in a Workflow definition, runtimes must assure to provide their values
during Workflow execution.

Secrets can be used only in [Workflow expressions](#Workflow-Expressions) under the `SECRETS` namespace. 
This is reserved namespace that should only be allowed for values defined by the `secrets` property.

Here is an example on how to use secrets and pass them as arguments to a function invocation:

```json
"secrets": ["AZURE_STORAGE_ACCOUNT", "AZURE_STORAGE_KEY"],

...
        
{
  "refName": "uploadToAzure",
    "arguments": {
      "account": "${ $SECRETS.AZURE_STORAGE_ACCOUNT }",
      "account-key": "${ $SECRETS.AZURE_STORAGE_KEY }",
      ...
    }
  
}
```

Note that secrets can also be used in [expression functions](#Using-Functions-for-Expression-Evaluation).

### Workflow Metadata

Metadata enables you to enrich the serverless workflow model with information beyond its core definitions.
It is intended to be used by clients, such as tools and libraries, as well as users that find this information relevant.

Metadata should not affect workflow execution. Implementations may choose to use metadata information or ignore it.
Note, however, that using metadata to control workflow execution can lead to vendor-locked implementations that do not comply with the main goals of this specification, which is to be completely vendor-neutral.

Metadata includes key/value pairs (string types). Both keys and values are completely arbitrary and non-identifying.

Metadata can be added to:

- [Workflow Definition](#Workflow-Definition-Structure)
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
  "specVersion": "0.6",
  "start": "MyStartingState",
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

Even tho users can define their own extensions, it is encouraged to use the ones provided by the specification.
We also encourage users to contribute their extensions to the specification. That way they can be shared
with the rest of the community.

If you have an idea for a new workflow extension, or would like to enhance an existing one, 
please open an `New Extension Request` issue in this repository.

## Use Cases

You can find different Serverless Workflow use cases [here](usecases/README.md).

## Examples

You can find many Serverless Workflow examples [here](examples/README.md).

## Comparison to other workflow languages

You can find info how the Serverless Workflow language compares with
 other workflow languages [here](comparisons/README.md).

## References

You can find a list of other languages, technologies and specifications related to workflows [here](references/README.md).

## License

Serverless Workflow specification operates under the
[Apache License version 2.0](LICENSE).
