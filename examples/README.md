<!--
   !!! THIS IS A GENERATED FILE !!!
   Please do not update this file but the TEMPLATE instead!
-->
# Examples

Provides Serverless Workflow language examples

## Table of Contents

- [Hello World](#Hello-World-Example)
- [Greeting](#Greeting-Example)
- [Event-based greeting (Event State)](#Event-Based-Greeting-Example)
- [Solving Math Problems (ForEach state)](#Solving-Math-Problems-Example)
- [Parallel Execution](#Parallel-Execution-Example)
- [Async Function Invocation](#Async-Function-Invocation-Example)
- [Async SubFlow Invocation](#Async-SubFlow-Invocation-Example)
- [Event Based Transitions (Event-based Switch)](#Event-Based-Transitions-Example)
- [Applicant Request Decision (Data-based Switch + SubFlows)](#Applicant-Request-Decision-Example)
- [Provision Orders (Error Handling)](#Provision-Orders-Example)
- [Monitor Job for completion (Polling)](#Monitor-Job-Example)
- [Send CloudEvent on Workflow Completion](#Send-CloudEvent-On-Workflow-Completion-Example)
- [Monitor Patient Vital Signs (Event state)](#Monitor-Patient-Vital-Signs-Example)
- [Finalize College Application (Event state)](#Finalize-College-Application-Example)
- [Perform Customer Credit Check (Callback state)](#Perform-Customer-Credit-Check-Example)
- [Handle Car Auction Bids (Scheduled start Event state)](#Handle-Car-Auction-Bids-Example)
- [Check Inbox Periodically (Cron-based Workflow start)](#Check-Inbox-Periodically)
- [Event-based service invocation (Event triggered actions)](#Event-Based-Service-Invocation)
- [Reusing Function and Event Definitions](#Reusing-Function-And-Event-Definitions)
- [New Patient Onboarding (Error checking and Retries)](#New-Patient-Onboarding)
- [Purchase order deadline (ExecTimeout)](#Purchase-order-deadline)
- [Accumulate room readings and create timely reports (ExecTimeout and KeepActive)](#Accumulate-room-readings)
- [Car vitals checks (SubFlow Repeat)](#Car-Vitals-Checks)
- [Book Lending Workflow](#Book-Lending)
- [Filling a glass of water (Expression functions)](#Filling-a-glass-of-water)
- [Online Food Ordering](#Online-Food-Ordering)
- [Continuing as a new Execution](#Continuing-as-a-new-Execution)
- [Process Transactions (Foreach State with conditions)](#Process-Transactions)

### Hello World Example

#### Description

In this simple example we use an [Inject State](../specification.md#Inject-State) to inject
`Hello World` in the states data (as the value of the 'result' property).
After the state execution completes, since it is an end state, its data output becomes the workflow
data output, which is:

```json
{
  "result": "Hello World"
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-helloworld.png" height="400px" alt="Hello World Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
   "id": "helloworld",
   "version": "1.0.0",
   "specVersion": "0.8",
   "name": "Hello World Workflow",
   "description": "Inject Hello World",
   "start": "hello-state",
   "states": [
      {
         "name": "hello-state",
         "type": "inject",
         "data": {
            "result": "Hello World!"
         },
         "end": true
      }
   ]
}```

</td>
<td valign="top">

```yaml
id: helloworld
version: 1.0.0
specVersion: "0.8"
name: Hello World Workflow
description: Inject Hello World
start: hello-state
states:
  - name: hello-state
    type: inject
    data:
      result: Hello World!
    end: true
```

</td>
</tr>
</table>

### Greeting Example

#### Description

This example shows a single [Operation State](../specification.md#operation-state) with one action that calls the "greeting" function.
The workflow data input is assumed to be the name of the person to greet:

```json
{
  "person": {
    "name": "John"
  }
}
```

The results of the action is assumed to be the greeting for the provided persons name:

```json
{
   "greeting":  "Welcome to Serverless Workflow, John!"
}
```

Which is added to the states data and becomes the workflow data output.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-greeting.png" height="500px" alt="Greeting Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "greeting",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "greeting-workflow",
    "description": "Greet Someone",
    "start": "greet",
    "functions": [
        {
            "name": "greeting-function",
            "type": "openapi",
            "operation": "file://myapis/greetingapis.json#greeting"
        }
    ],
    "states": [
        {
            "name": "greet",
            "type": "operation",
            "actions": [
                {
                    "name": "greet-action",
                    "functionRef": {
                        "refName": "greeting-function",
                        "arguments": {
                            "name": "${ .person.name }"
                        }
                    },
                    "actionDataFilter": {
                        "results": "${ {greeting: .greeting} }"
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: greeting
version: 1.0.0
specVersion: "0.8"
name: greeting-workflow
description: Greet Someone
start: greet
functions:
  - name: greeting-function
    type: openapi
    operation: file://myapis/greetingapis.json#greeting
states:
  - name: greet
    type: operation
    actions:
      - name: greet-action
        functionRef:
          refName: greeting-function
          arguments:
            name: ${ .person.name }
        actionDataFilter:
          results: "${ {greeting: .greeting} }"
    end: true
```

</td>
</tr>
</table>

### Event Based Greeting Example

#### Description

This example shows a single [Event State](../specification.md#event-state) with one action that calls the "greeting" function.
The event state consumes cloud events of type "greetingEventType". When an event with this type
is consumed, the Event state performs a single action that calls the defined "greeting" function.

For the sake of the example we assume that the cloud event we will consume has the format:

```json
{
    "specversion" : "1.0",
    "type" : "greetingEventType",
    "source" : "greetingEventSource",
    "data" : {
      "greet": {
          "name": "John"
        }
    }
}
```

The results of the action is assumed to be the full greeting for the provided persons name:

```json
{
  "payload": {
    "greeting": "Welcome to Serverless Workflow, John!"
  }
}
```

Note that in the workflow definition you can see two filters defined. The event data filter defined inside the consume element:

```json
{
  "eventDataFilter": {
    "data": "${ .data.greet } "
  }
}
```

which is triggered when the greeting event is consumed. It extracts its "data.greet" of the event data (payload) and
merges it with the state data.

The second, a state data filter, which is defined on the event state itself:

```json
{
  "stateDataFilter": {
     "output": "${ .payload.greeting }"
  }
}
```

filters what is selected to be the state data output which then becomes the workflow data output (as it is an end state):

```text
   "Welcome to Serverless Workflow, John!"
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-eventbasedgreeting.png" height="500px" alt="Event Based Greeting Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "eventbasedgreeting",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "Event Based Greeting Workflow",
    "description": "Event Based Greeting",
    "start": "greet",
    "events": [
        {
            "name": "greeting-event",
            "type": "greetingEventType",
            "source": "greetingEventSource"
        }
    ],
    "functions": [
        {
            "name": "greeting-function",
            "operation": "file://myapis/greetingapis.json#greeting"
        }
    ],
    "states": [
        {
            "name": "greet",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "greeting-event"
                    ],
                    "eventDataFilter": {
                        "data": "${ .greet }",
                        "toStateData": "${ .greet }"
                    },
                    "actions": [
                        {
                            "name": "greet-action",
                            "functionRef": {
                                "refName": "greeting-function",
                                "arguments": {
                                    "name": "${ .greet.name }"
                                }
                            }
                        }
                    ]
                }
            ],
            "stateDataFilter": {
                "output": "${ .payload.greeting }"
            },
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: eventbasedgreeting
version: 1.0.0
specVersion: "0.8"
name: Event Based Greeting Workflow
description: Event Based Greeting
start: greet
events:
  - name: greeting-event
    type: greetingEventType
    source: greetingEventSource
functions:
  - name: greeting-function
    operation: file://myapis/greetingapis.json#greeting
states:
  - name: greet
    type: event
    onEvents:
      - eventRefs:
          - greeting-event
        eventDataFilter:
          data: ${ .greet }
          toStateData: ${ .greet }
        actions:
          - name: greet-action
            functionRef:
              refName: greeting-function
              arguments:
                name: ${ .greet.name }
    stateDataFilter:
      output: ${ .payload.greeting }
    end: true
```

</td>
</tr>
</table>

### Solving Math Problems Example

#### Description

In this example we show how to iterate over data using the [ForEach State](../specification.md#foreach-state).
The state will iterate over a collection of simple math expressions which are
passed in as the workflow data input:

```json
    {
      "expressions": ["2+2", "4-1", "10x3", "20/2"]
    }
```

The ForEach state will execute a single defined operation state for each math expression. The operation
state contains an action which calls a serverless function which actually solves the expression
and returns its result.

Results of all math expressions are accumulated into the data output of the ForEach state which become the final
result of the workflow execution.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-looping.png" height="500px" alt="Looping Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "solvemathproblems",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "solve-math-problems-workflow",
    "description": "Solve math problems",
    "start": "solve",
    "functions": [
        {
            "name": "solve-math-exp-func",
            "operation": "http://myapis.org/mapthapis.json#solveExpression"
        }
    ],
    "states": [
        {
            "name": "solve",
            "type": "foreach",
            "inputCollection": "${ .expressions }",
            "iterationParam": "singleexpression",
            "outputCollection": "${ .results }",
            "actions": [
                {
                    "name": "solve-action",
                    "functionRef": {
                        "refName": "solve-math-exp-func",
                        "arguments": {
                            "expression": "${ .singleexpression }"
                        }
                    }
                }
            ],
            "stateDataFilter": {
                "output": "${ .results }"
            },
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: solvemathproblems
version: 1.0.0
specVersion: "0.8"
name: solve-math-problems-workflow
description: Solve math problems
start: solve
functions:
  - name: solve-math-exp-func
    operation: http://myapis.org/mapthapis.json#solveExpression
states:
  - name: solve
    type: foreach
    inputCollection: ${ .expressions }
    iterationParam: singleexpression
    outputCollection: ${ .results }
    actions:
      - name: solve-action
        functionRef:
          refName: solve-math-exp-func
          arguments:
            expression: ${ .singleexpression }
    stateDataFilter:
      output: ${ .results }
    end: true
```

</td>
</tr>
</table>

### Parallel Execution Example

#### Description

This example uses a [Parallel State](../specification.md#parallel-state) to execute two branches (simple wait states) at the same time.
The completionType type is set to "allOf", which means the parallel state has to wait for both branches
to finish execution before it can transition (end workflow execution in this case as it is an end state).

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-parallel.png" height="500px" alt="Parallel Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "parallelexec",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "parallel-execution",
    "description": "Executes two branches in parallel",
    "start": "parallelexec",
    "states": [
        {
            "name": "parallelexec",
            "type": "parallel",
            "completionType": "allOf",
            "branches": [
                {
                    "name": "short-delay-branch",
                    "actions": [
                        {
                            "name": "short-delay-action",
                            "subFlowRef": "shortdelayworkflowid"
                        }
                    ]
                },
                {
                    "name": "long-delay-branch",
                    "actions": [
                        {
                            "name": "short-delay-action",
                            "subFlowRef": "longdelayworkflowid"
                        }
                    ]
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: parallelexec
version: 1.0.0
specVersion: "0.8"
name: parallel-execution
description: Executes two branches in parallel
start: parallelexec
states:
  - name: parallelexec
    type: parallel
    completionType: allOf
    branches:
      - name: short-delay-branch
        actions:
          - name: short-delay-action
            subFlowRef: shortdelayworkflowid
      - name: long-delay-branch
        actions:
          - name: short-delay-action
            subFlowRef: longdelayworkflowid
    end: true
```

</td>
</tr>
</table>

We assume that the two referenced workflows, namely `shortdelayworkflowid` and `longdelayworkflowid` both include a single delay state,
with the `shortdelayworkflowid` workflow delay state defining its `timeDelay` property to be shorter than that of the `longdelayworkflowid` workflows
delay state.

### Async Function Invocation Example

#### Description

This example uses a [Operation State](../specification.md#operation-state) to invoke a function async. 
This functions sends an email to a customer.
Async function execution is a "fire-and-forget" type of invocation. The function is invoked and workflow execution
does not wait for its results.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-asyncfunction.png" height="500px" alt="Async Function Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "sendcustomeremail",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "send-customer-email-workflow",
    "description": "Send email to a customer",
    "start": "send-email",
    "functions": [
        {
            "name": "email-function",
            "operation": "file://myapis/emailapis.json#sendEmail"
        }
    ],
    "states": [
        {
            "name": "send-email",
            "type": "operation",
            "actions": [
                {
                    "name": "send-email-action",
                    "functionRef": {
                        "invoke": "async",
                        "refName": "email-function",
                        "arguments": {
                            "customer": "${ .customer }"
                        }
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: sendcustomeremail
version: 1.0.0
specVersion: "0.8"
name: send-customer-email-workflow
description: Send email to a customer
start: send-email
functions:
  - name: email-function
    operation: file://myapis/emailapis.json#sendEmail
states:
  - name: send-email
    type: operation
    actions:
      - name: send-email-action
        functionRef:
          invoke: async
          refName: email-function
          arguments:
            customer: ${ .customer }
    end: true
```

</td>
</tr>
</table>

### Async SubFlow Invocation Example

#### Description

This example uses a [Operation State](../specification.md#operation-state) to invoke a [SubFlow](../specification.md#Subflow-Action) async.
This SubFlow is responsible for performing some customer business logic.
Async SubFlow invocation is a "fire-and-forget" type of invocation. The SubFlow is invoked and workflow execution
does not wait for its results. In addition, we specify that the SubFlow should be allowed to continue its execution 
event if the parent workflow completes its own execution. This is done by defining the actions `onParentComplete`
property to `continue`.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-asyncsubflow.png" height="500px" alt="Async SubFlow Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "onboardcustomer",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "onboard-customer",
    "description": "Onboard a Customer",
    "start": "onboard",
    "states": [
        {
            "name": "onboard",
            "type": "operation",
            "actions": [
                {
                    "name": "onboard-action",
                    "subFlowRef": {
                        "invoke": "async",
                        "onParentComplete": "continue",
                        "workflowId": "customeronboardingworkflow",
                        "version": "1.0.0"
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: onboardcustomer
version: 1.0.0
specVersion: "0.8"
name: onboard-customer
description: Onboard a Customer
start: onboard
states:
  - name: onboard
    type: operation
    actions:
      - name: onboard-action
        subFlowRef:
          invoke: async
          onParentComplete: continue
          workflowId: customeronboardingworkflow
          version: 1.0.0
    end: true
```

</td>
</tr>
</table>

For the sake of the example, the definition of "customeronboardingworkflow" workflow invoked as a SubFlow 
is not shown. 

### Event Based Transitions Example

#### Description

In this example we use an Event-based [Switch State](../specification.md#switch-state) to wait for arrival
of the "VisaApproved", or "VisaRejected" Cloud Events. Depending on which type of event happens,
the workflow performs a different transition. If none of the events arrive in the defined 1 hour timeout
period, the workflow transitions to the "HandleNoVisaDecision" state.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-eventbasedswitch.png" height="500px" alt="Event Based Switch Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "eventbasedswitchstate",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "event-based-switch-transitions",
    "description": "Event Based Switch Transitions",
    "start": "checkvisastatus",
    "events": [
        {
            "name": "visa-approved-event",
            "type": "VisaApproved",
            "source": "visaCheckSource"
        },
        {
            "name": "visa-rejected-event",
            "type": "VisaRejected",
            "source": "visaCheckSource"
        }
    ],
    "states": [
        {
            "name": "checkvisastatus",
            "type": "switch",
            "eventConditions": [
                {
                    "eventRef": "visa-approved-event",
                    "transition": "handle-approved-visa",
                    "name": "approved-condition"
                },
                {
                    "eventRef": "visa-rejected-event",
                    "transition": "handle-rejected-visa",
                    "name": "rejected-condition"
                }
            ],
            "timeouts": {
                "eventTimeout": "PT1H"
            },
            "defaultCondition": {
                "transition": "handle-no-visa-decision"
            }
        },
        {
            "name": "handle-approved-visa",
            "type": "operation",
            "actions": [
                {
                    "name": "handle-approved-action",
                    "subFlowRef": "handleApprovedVisaWorkflowID"
                }
            ],
            "end": true
        },
        {
            "name": "handle-rejected-visa",
            "type": "operation",
            "actions": [
                {
                    "name": "handle-rejected-action",
                    "subFlowRef": "handleRejectedVisaWorkflowID"
                }
            ],
            "end": true
        },
        {
            "name": "handle-no-visa-decision",
            "type": "operation",
            "actions": [
                {
                    "name": "handle-novisa-action",
                    "subFlowRef": "handleNoVisaDecisionWorkflowId"
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: eventbasedswitchstate
version: 1.0.0
specVersion: "0.8"
name: event-based-switch-transitions
description: Event Based Switch Transitions
start: checkvisastatus
events:
  - name: visa-approved-event
    type: VisaApproved
    source: visaCheckSource
  - name: visa-rejected-event
    type: VisaRejected
    source: visaCheckSource
states:
  - name: checkvisastatus
    type: switch
    eventConditions:
      - eventRef: visa-approved-event
        transition: handle-approved-visa
        name: approved-condition
      - eventRef: visa-rejected-event
        transition: handle-rejected-visa
        name: rejected-condition
    timeouts:
      eventTimeout: PT1H
    defaultCondition:
      transition: handle-no-visa-decision
  - name: handle-approved-visa
    type: operation
    actions:
      - name: handle-approved-action
        subFlowRef: handleApprovedVisaWorkflowID
    end: true
  - name: handle-rejected-visa
    type: operation
    actions:
      - name: handle-rejected-action
        subFlowRef: handleRejectedVisaWorkflowID
    end: true
  - name: handle-no-visa-decision
    type: operation
    actions:
      - name: handle-novisa-action
        subFlowRef: handleNoVisaDecisionWorkflowId
    end: true
```

</td>
</tr>
</table>

### Applicant Request Decision Example

#### Description

This example shows off the [Switch State](../specification.md#switch-state) and the subflow action. The workflow is started with application information data as input:

```json
    {
      "applicant": {
        "fname": "John",
        "lname": "Stockton",
        "age": 22,
        "email": "js@something.com"
      }
    }
```

We use the switch state with two conditions to determine if the application should be made based on the applicants age.
If the applicants age is over 18 we start the application (subflow action). Otherwise the workflow notifies the
 applicant of the rejection.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-switchstate.png" height="500px" alt="Switch State Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "applicantrequest",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "applicant-request-decision-workflow",
    "description": "Determine if applicant request is valid",
    "start": "check-application",
    "functions": [
        {
            "name": "send-rejection-email-function",
            "operation": "http://myapis.org/applicationapi.json#emailRejection"
        }
    ],
    "states": [
        {
            "name": "check-application",
            "type": "switch",
            "dataConditions": [
                {
                    "condition": "${ .applicants | .age >= 18 }",
                    "transition": "start-application",
                    "name": "adult-condition"
                },
                {
                    "condition": "${ .applicants | .age < 18 }",
                    "transition": "reject-application",
                    "name": "minor-condition"
                }
            ],
            "defaultCondition": {
                "transition": "reject-application"
            }
        },
        {
            "name": "start-application",
            "type": "operation",
            "actions": [
                {
                    "name": "start-app-action",
                    "subFlowRef": "startApplicationWorkflowId"
                }
            ],
            "end": true
        },
        {
            "name": "reject-application",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "send-reject-action",
                    "functionRef": {
                        "refName": "send-rejection-email-function",
                        "arguments": {
                            "applicant": "${ .applicant }"
                        }
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: applicantrequest
version: 1.0.0
specVersion: "0.8"
name: applicant-request-decision-workflow
description: Determine if applicant request is valid
start: check-application
functions:
  - name: send-rejection-email-function
    operation: http://myapis.org/applicationapi.json#emailRejection
states:
  - name: check-application
    type: switch
    dataConditions:
      - condition: ${ .applicants | .age >= 18 }
        transition: start-application
        name: adult-condition
      - condition: ${ .applicants | .age < 18 }
        transition: reject-application
        name: minor-condition
    defaultCondition:
      transition: reject-application
  - name: start-application
    type: operation
    actions:
      - name: start-app-action
        subFlowRef: startApplicationWorkflowId
    end: true
  - name: reject-application
    type: operation
    actionMode: sequential
    actions:
      - name: send-reject-action
        functionRef:
          refName: send-rejection-email-function
          arguments:
            applicant: ${ .applicant }
    end: true
```

</td>
</tr>
</table>

### Provision Orders Example

#### Description

In this example we show off the states error handling capability. The workflow data input that's passed in contains
missing order information that causes the function in the "ProvisionOrder" state to throw a runtime exception. With the "onErrors" definition we
can transition the workflow to different error handling states. Each type of error
in this example is handled by simple delay states. If no errors are encountered the workflow can transition to the "ApplyOrder" state.

Workflow data is assumed to me:

```json
    {
      "order": {
        "id": "",
        "item": "laptop",
        "quantity": "10"
      }
    }
```

The data output of the workflow contains the information of the exception caught during workflow execution.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-handlerrors.png" height="500px" alt="Handle Errors Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "provisionorders",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "provision-orders",
    "description": "Provision Orders and handle errors thrown",
    "start": "provision-order",
    "functions": [
        {
            "name": "provision-order-function",
            "operation": "http://myapis.org/provisioningapi.json#doProvision"
        }
    ],
    "errors": [
        {
            "name": "missing-order-id"
        },
        {
            "name": "missing-order-item"
        },
        {
            "name": "missing-order-quantity"
        }
    ],
    "states": [
        {
            "name": "provision-order",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "provision-action",
                    "functionRef": {
                        "refName": "provision-order-function",
                        "arguments": {
                            "order": "${ .order }"
                        }
                    }
                }
            ],
            "stateDataFilter": {
                "output": "${ .exceptions }"
            },
            "transition": "apply-order",
            "onErrors": [
                {
                    "errorRef": "missing-order-id",
                    "transition": "missing-id"
                },
                {
                    "errorRef": "missing-order-item",
                    "transition": "missing-item"
                },
                {
                    "errorRef": "missing-order-quantity",
                    "transition": "missing-quantity"
                }
            ]
        },
        {
            "name": "missing-id",
            "type": "operation",
            "actions": [
                {
                    "name": "missing-action",
                    "subFlowRef": "handleMissingIdExceptionWorkflow"
                }
            ],
            "end": true
        },
        {
            "name": "missing-item",
            "type": "operation",
            "actions": [
                {
                    "name": "missing-item",
                    "subFlowRef": "handleMissingItemExceptionWorkflow"
                }
            ],
            "end": true
        },
        {
            "name": "missing-quantity",
            "type": "operation",
            "actions": [
                {
                    "name": "missing-quantity",
                    "subFlowRef": "handleMissingQuantityExceptionWorkflow"
                }
            ],
            "end": true
        },
        {
            "name": "apply-order",
            "type": "operation",
            "actions": [
                {
                    "name": "apply-order",
                    "subFlowRef": "applyOrderWorkflowId"
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: provisionorders
version: 1.0.0
specVersion: "0.8"
name: provision-orders
description: Provision Orders and handle errors thrown
start: provision-order
functions:
  - name: provision-order-function
    operation: http://myapis.org/provisioningapi.json#doProvision
errors:
  - name: missing-order-id
  - name: missing-order-item
  - name: missing-order-quantity
states:
  - name: provision-order
    type: operation
    actionMode: sequential
    actions:
      - name: provision-action
        functionRef:
          refName: provision-order-function
          arguments:
            order: ${ .order }
    stateDataFilter:
      output: ${ .exceptions }
    transition: apply-order
    onErrors:
      - errorRef: missing-order-id
        transition: missing-id
      - errorRef: missing-order-item
        transition: missing-item
      - errorRef: missing-order-quantity
        transition: missing-quantity
  - name: missing-id
    type: operation
    actions:
      - name: missing-action
        subFlowRef: handleMissingIdExceptionWorkflow
    end: true
  - name: missing-item
    type: operation
    actions:
      - name: missing-item
        subFlowRef: handleMissingItemExceptionWorkflow
    end: true
  - name: missing-quantity
    type: operation
    actions:
      - name: missing-quantity
        subFlowRef: handleMissingQuantityExceptionWorkflow
    end: true
  - name: apply-order
    type: operation
    actions:
      - name: apply-order
        subFlowRef: applyOrderWorkflowId
    end: true
```

</td>
</tr>
</table>

### Monitor Job Example

#### Description

In this example we submit a job via an operation state action (serverless function call). It is assumed that it takes some time for
the submitted job to complete and that it's completion can be checked via another separate serverless function call.

To check for completion we first wait 5 seconds and then get the results of the "CheckJob" serverless function.
Depending on the results of this we either return the results or transition back to waiting and checking the job completion.
This is done until the job submission returns "SUCCEEDED" or "FAILED" and the job submission results are reported before workflow
finishes execution.

In the case job submission raises a runtime error, we transition to an Operation state which invokes
 a sub-flow responsible for handling the job submission issue.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/examples-jobmonitoring.png" height="500px" alt="Job Monitoring Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "jobmonitoring",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "jobmonitoring",
    "description": "Monitor finished execution of a submitted job",
    "start": "submit-job",
    "functions": [
        {
            "name": "submit-job",
            "operation": "http://myapis.org/monitorapi.json#doSubmit"
        },
        {
            "name": "check-job-status",
            "operation": "http://myapis.org/monitorapi.json#checkStatus"
        },
        {
            "name": "report-job-suceeded",
            "operation": "http://myapis.org/monitorapi.json#reportSucceeded"
        },
        {
            "name": "report-job-failed",
            "operation": "http://myapis.org/monitorapi.json#reportFailure"
        }
    ],
    "states": [
        {
            "name": "submit-job",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "submit-job",
                    "functionRef": {
                        "refName": "submit-job",
                        "arguments": {
                            "name": "${ .job.name }"
                        }
                    },
                    "actionDataFilter": {
                        "results": "${ .jobuid }"
                    }
                }
            ],
            "stateDataFilter": {
                "output": "${ .jobuid }"
            },
            "transition": "wait-for-completion"
        },
        {
            "name": "wait-for-completion",
            "type": "sleep",
            "duration": "PT5S",
            "transition": "get-job-status"
        },
        {
            "name": "get-job-status",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "get-job-status",
                    "functionRef": {
                        "refName": "check-job-status",
                        "arguments": {
                            "name": "${ .jobuid }"
                        }
                    },
                    "actionDataFilter": {
                        "results": "${ .jobstatus }"
                    }
                }
            ],
            "stateDataFilter": {
                "output": "${ .jobstatus }"
            },
            "transition": "determine-completion"
        },
        {
            "name": "determine-completion",
            "type": "switch",
            "dataConditions": [
                {
                    "condition": "${ .jobStatus == \"SUCCEEDED\" }",
                    "transition": "job-succeeded",
                    "name": "succeed"
                },
                {
                    "condition": "${ .jobStatus == \"FAILED\" }",
                    "transition": "job-failed",
                    "name": "failed"
                }
            ],
            "defaultCondition": {
                "transition": "wait-for-completion"
            }
        },
        {
            "name": "job-succeeded",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "job-succeeded",
                    "functionRef": {
                        "refName": "report-job-suceeded",
                        "arguments": {
                            "name": "${ .jobuid }"
                        }
                    }
                }
            ],
            "end": true
        },
        {
            "name": "job-failed",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "job-failed",
                    "functionRef": {
                        "refName": "report-job-failed",
                        "arguments": {
                            "name": "${ .jobuid }"
                        }
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: jobmonitoring
version: 1.0.0
specVersion: "0.8"
name: jobmonitoring
description: Monitor finished execution of a submitted job
start: submit-job
functions:
  - name: submit-job
    operation: http://myapis.org/monitorapi.json#doSubmit
  - name: check-job-status
    operation: http://myapis.org/monitorapi.json#checkStatus
  - name: report-job-suceeded
    operation: http://myapis.org/monitorapi.json#reportSucceeded
  - name: report-job-failed
    operation: http://myapis.org/monitorapi.json#reportFailure
states:
  - name: submit-job
    type: operation
    actionMode: sequential
    actions:
      - name: submit-job
        functionRef:
          refName: submit-job
          arguments:
            name: ${ .job.name }
        actionDataFilter:
          results: ${ .jobuid }
    stateDataFilter:
      output: ${ .jobuid }
    transition: wait-for-completion
  - name: wait-for-completion
    type: sleep
    duration: PT5S
    transition: get-job-status
  - name: get-job-status
    type: operation
    actionMode: sequential
    actions:
      - name: get-job-status
        functionRef:
          refName: check-job-status
          arguments:
            name: ${ .jobuid }
        actionDataFilter:
          results: ${ .jobstatus }
    stateDataFilter:
      output: ${ .jobstatus }
    transition: determine-completion
  - name: determine-completion
    type: switch
    dataConditions:
      - condition: ${ .jobStatus == "SUCCEEDED" }
        transition: job-succeeded
        name: succeed
      - condition: ${ .jobStatus == "FAILED" }
        transition: job-failed
        name: failed
    defaultCondition:
      transition: wait-for-completion
  - name: job-succeeded
    type: operation
    actionMode: sequential
    actions:
      - name: job-succeeded
        functionRef:
          refName: report-job-suceeded
          arguments:
            name: ${ .jobuid }
    end: true
  - name: job-failed
    type: operation
    actionMode: sequential
    actions:
      - name: job-failed
        functionRef:
          refName: report-job-failed
          arguments:
            name: ${ .jobuid }
    end: true
```

</td>
</tr>
</table>

### Send CloudEvent On Workflow Completion Example

#### Description

This example shows how we can produce a CloudEvent on completion of a workflow. Let's say we have the following
workflow data containing orders that need to be provisioned by our workflow:

```json
{
  "orders": [{
    "id": "123",
    "item": "laptop",
    "quantity": "10"
  },
  {
      "id": "456",
      "item": "desktop",
      "quantity": "4"
    }]
}
```

Our workflow in this example uses a ForEach state to provision the orders in parallel. The "provisionOrder" function
used is assumed to have the following results:

```json
{
   "id": "123",
   "outcome": "SUCCESS"
}
```

After orders have been provisioned the ForEach states defines the end property which stops workflow execution.
It defines its end definition to be of type "event" in which case a CloudEvent will be produced which can be consumed
by other orchestration workflows or other interested consumers.

Note that we define the event to be produced in the workflows "events" property.

The data attached to the event contains the information on provisioned orders by this workflow. So the produced
CloudEvent upon completion of the workflow could look like:

```json
{
  "specversion" : "1.0",
  "type" : "provisionCompleteType",
  "datacontenttype" : "application/json",
  ...
  "data": {
    "provisionedOrders": [
        {
          "id": "123",
          "outcome": "SUCCESS"
        },
        {
          "id": "456",
          "outcome": "FAILURE"
        }
      ]
  }
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-sendcloudeentonworkflowcompletion.png" height="500px" alt="Send CloudEvent on Workflow Completion Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "sendcloudeventonprovision",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "sendcloudeventonprovision",
    "start": "provision-orders-state",
    "events": [
        {
            "name": "provisioning-complete-event",
            "type": "provisionCompleteType",
            "kind": "produced"
        }
    ],
    "functions": [
        {
            "name": "provision-order-function",
            "operation": "http://myapis.org/provisioning.json#doProvision"
        }
    ],
    "states": [
        {
            "name": "provision-orders-state",
            "type": "foreach",
            "inputCollection": "${ .orders }",
            "iterationParam": "singleorder",
            "outputCollection": "${ .provisionedOrders }",
            "actions": [
                {
                    "name": "provision-order-function",
                    "functionRef": {
                        "refName": "provision-order-function",
                        "arguments": {
                            "order": "${ .singleorder }"
                        }
                    }
                }
            ],
            "end": {
                "produceEvents": [
                    {
                        "eventRef": "provisioning-complete-event",
                        "data": "${ .provisionedOrders }"
                    }
                ]
            }
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: sendcloudeventonprovision
version: 1.0.0
specVersion: "0.8"
name: sendcloudeventonprovision
start: provision-orders-state
events:
  - name: provisioning-complete-event
    type: provisionCompleteType
    kind: produced
functions:
  - name: provision-order-function
    operation: http://myapis.org/provisioning.json#doProvision
states:
  - name: provision-orders-state
    type: foreach
    inputCollection: ${ .orders }
    iterationParam: singleorder
    outputCollection: ${ .provisionedOrders }
    actions:
      - name: provision-order-function
        functionRef:
          refName: provision-order-function
          arguments:
            order: ${ .singleorder }
    end:
      produceEvents:
        - eventRef: provisioning-complete-event
          data: ${ .provisionedOrders }
```

</td>
</tr>
</table>

### Monitor Patient Vital Signs Example

#### Description

In this example a hospital patient is monitored by a Vial Sign Monitoring system. This device can produce three different Cloud Events, namely
"High Body Temperature", "High Blood Pressure", and "High Respiration Rate".
Our workflow which needs to take proper actions depending on the event the Vital Sign Monitor produces needs to start
if any of these events occur. For each of these events a new instance of the workflow is started.

Since the hospital may include many patients that are being monitored it is assumed that all events include a patientId context attribute in the event
 message. We can use the value of this context attribute to associate the incoming events with the same patient as well as
 use the patient id to pass as parameter to the functions called by event activities. Here is an example of such event:

```json
{
    "specversion" : "1.0",
    "type" : "org.monitor.highBodyTemp",
    "source" : "monitoringSource",
    "subject" : "BodyTemperatureReading",
    "id" : "A234-1234-1234",
    "time" : "2020-01-05T17:31:00Z",
    "patientId" : "PID-12345",
    "data" : {
      "value": "98.6F"
    }
}
```

As you can see the "patientId" context attribute of the event includes our correlation key which is the unique
patient id. If we set it to be the correlation key in our events definition, all events that are considered must
have the matching patient id.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-monitorpatientvitalsigns.png" height="500px" alt="Monitor Patient Vital Signs Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "patientVitalsWorkflow",
    "name": "patientVitalsWorkflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "monitor-vitals",
    "events": [
        {
            "name": "high-body-temperature",
            "type": "org.monitor.highBodyTemp",
            "source": "monitoringSource",
            "correlation": [
                {
                    "contextAttributeName": "patientId"
                }
            ]
        },
        {
            "name": "high-blood-pressure",
            "type": "org.monitor.highBloodPressure",
            "source": "monitoringSource",
            "correlation": [
                {
                    "contextAttributeName": "patientId"
                }
            ]
        },
        {
            "name": "high-respiration-rate",
            "type": "org.monitor.highRespirationRate",
            "source": "monitoringSource",
            "correlation": [
                {
                    "contextAttributeName": "patientId"
                }
            ]
        }
    ],
    "functions": [
        {
            "name": "call-pulmonologist",
            "operation": "http://myapis.org/patientapis.json#callPulmonologist"
        },
        {
            "name": "send-tylenol-order",
            "operation": "http://myapis.org/patientapis.json#tylenolOrder"
        },
        {
            "name": "call-nurse",
            "operation": "http://myapis.org/patientapis.json#callNurse"
        }
    ],
    "states": [
        {
            "name": "monitor-vitals",
            "type": "event",
            "exclusive": true,
            "onEvents": [
                {
                    "eventRefs": [
                        "high-body-temperature"
                    ],
                    "actions": [
                        {
                            "name": "send-tylenol-order",
                            "functionRef": {
                                "refName": "send-tylenol-order",
                                "arguments": {
                                    "patientid": "${ .patientId }"
                                }
                            }
                        }
                    ]
                },
                {
                    "eventRefs": [
                        "high-blood-pressure"
                    ],
                    "actions": [
                        {
                            "name": "call-nurse",
                            "functionRef": {
                                "refName": "call-nurse",
                                "arguments": {
                                    "patientid": "${ .patientId }"
                                }
                            }
                        }
                    ]
                },
                {
                    "eventRefs": [
                        "high-respiration-rate"
                    ],
                    "actions": [
                        {
                            "name": "call-pulmonologist",
                            "functionRef": {
                                "refName": "call-pulmonologist",
                                "arguments": {
                                    "patientid": "${ .patientId }"
                                }
                            }
                        }
                    ]
                }
            ],
            "end": {
                "terminate": true
            }
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: patientVitalsWorkflow
name: patientVitalsWorkflow
version: 1.0.0
specVersion: "0.8"
start: monitor-vitals
events:
  - name: high-body-temperature
    type: org.monitor.highBodyTemp
    source: monitoringSource
    correlation:
      - contextAttributeName: patientId
  - name: high-blood-pressure
    type: org.monitor.highBloodPressure
    source: monitoringSource
    correlation:
      - contextAttributeName: patientId
  - name: high-respiration-rate
    type: org.monitor.highRespirationRate
    source: monitoringSource
    correlation:
      - contextAttributeName: patientId
functions:
  - name: call-pulmonologist
    operation: http://myapis.org/patientapis.json#callPulmonologist
  - name: send-tylenol-order
    operation: http://myapis.org/patientapis.json#tylenolOrder
  - name: call-nurse
    operation: http://myapis.org/patientapis.json#callNurse
states:
  - name: monitor-vitals
    type: event
    exclusive: true
    onEvents:
      - eventRefs:
          - high-body-temperature
        actions:
          - name: send-tylenol-order
            functionRef:
              refName: send-tylenol-order
              arguments:
                patientid: ${ .patientId }
      - eventRefs:
          - high-blood-pressure
        actions:
          - name: call-nurse
            functionRef:
              refName: call-nurse
              arguments:
                patientid: ${ .patientId }
      - eventRefs:
          - high-respiration-rate
        actions:
          - name: call-pulmonologist
            functionRef:
              refName: call-pulmonologist
              arguments:
                patientid: ${ .patientId }
    end:
      terminate: true
```

</td>
</tr>
</table>

### Finalize College Application Example

#### Description

In this example our workflow is instantiated when all requirements of a college application are completed.
These requirements include a student submitting an application, the college receiving the students SAT scores, as well
as a student recommendation letter from a former teacher.

We assume three Cloud Events "ApplicationSubmitted", "SATScoresReceived" and "RecommendationLetterReceived".
Each include the applicant id in their "applicantId" context attribute, so we can use it to associate these events with an individual applicant.

Our workflow is instantiated and performs the actions to finalize the college application for a student only
when all three of these events happened (in no particular order).

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-finalizecollegeapplication.png" height="500px" alt="Finalize College Application Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "finalize-college-application",
    "name": "finalizeCollegeApplication",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "finalize-application",
    "events": [
        {
            "name": "application-submitted",
            "type": "org.application.submitted",
            "source": "applicationsource",
            "correlation": [
                {
                    "contextAttributeName": "applicantId"
                }
            ]
        },
        {
            "name": "sat-scores-received",
            "type": "org.application.satscores",
            "source": "applicationsource",
            "correlation": [
                {
                    "contextAttributeName": "applicantId"
                }
            ]
        },
        {
            "name": "recommendation-letter-received",
            "type": "org.application.recommendationLetter",
            "source": "applicationsource",
            "correlation": [
                {
                    "contextAttributeName": "applicantId"
                }
            ]
        }
    ],
    "functions": [
        {
            "name": "finalize-application-function",
            "operation": "http://myapis.org/collegeapplicationapi.json#finalize"
        }
    ],
    "states": [
        {
            "name": "finalize-application",
            "type": "event",
            "exclusive": false,
            "onEvents": [
                {
                    "eventRefs": [
                        "application-submitted",
                        "sat-scores-received",
                        "recommendation-letter-received"
                    ],
                    "actions": [
                        {
                            "name": "finalize-application",
                            "functionRef": {
                                "refName": "finalize-application-function",
                                "arguments": {
                                    "student": "${ .applicantId }"
                                }
                            }
                        }
                    ]
                }
            ],
            "end": {
                "terminate": true
            }
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: finalize-college-application
name: finalizeCollegeApplication
version: 1.0.0
specVersion: "0.8"
start: finalize-application
events:
  - name: application-submitted
    type: org.application.submitted
    source: applicationsource
    correlation:
      - contextAttributeName: applicantId
  - name: sat-scores-received
    type: org.application.satscores
    source: applicationsource
    correlation:
      - contextAttributeName: applicantId
  - name: recommendation-letter-received
    type: org.application.recommendationLetter
    source: applicationsource
    correlation:
      - contextAttributeName: applicantId
functions:
  - name: finalize-application-function
    operation: http://myapis.org/collegeapplicationapi.json#finalize
states:
  - name: finalize-application
    type: event
    exclusive: false
    onEvents:
      - eventRefs:
          - application-submitted
          - sat-scores-received
          - recommendation-letter-received
        actions:
          - name: finalize-application
            functionRef:
              refName: finalize-application-function
              arguments:
                student: ${ .applicantId }
    end:
      terminate: true
```

</td>
</tr>
</table>

### Perform Customer Credit Check Example

#### Description

In this example our serverless workflow needs to integrate with an external microservice to perform
a credit check. We assume that this external microservice notifies a human actor which has to make
the approval decision based on customer information. Once this decision is made the service emits a CloudEvent which
includes the decision information as part of its payload.
The workflow waits for this callback event and then triggers workflow transitions based on the
credit check decision results.

The workflow data input is assumed to be:

```json
{
  "customer": {
    "id": "customer123",
    "name": "John Doe",
    "SSN": 123456,
    "yearlyIncome": 50000,
    "address": "123 MyLane, MyCity, MyCountry",
    "employer": "MyCompany"
  }
}
```

The callback event that our workflow will wait on is assumed to have the following formats.
For approved credit check, for example:

```json
{
  "specversion" : "1.0",
  "type" : "creditCheckCompleteType",
  "datacontenttype" : "application/json",
  ...
  "data": {
    "creditCheck": [
        {
          "id": "customer123",
          "score": 700,
          "decision": "Approved",
          "reason": "Good credit score"
        }
      ]
  }
}
```

And for denied credit check, for example:

```json
{
  "specversion" : "1.0",
  "type" : "creditCheckCompleteType",
  "datacontenttype" : "application/json",
  ...
  "data": {
    "creditCheck": [
        {
          "id": "customer123",
          "score": 580,
          "decision": "Denied",
          "reason": "Low credit score. Recent late payments"
        }
      ]
  }
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-customercreditcheck.png" height="500px" alt="Perform Customer Credit Check Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "customercreditcheck",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "customercreditcheck",
    "description": "Perform Customer Credit Check",
    "start": "check-credit",
    "functions": [
        {
            "name": "credit-check-function",
            "operation": "http://myapis.org/creditcheckapi.json#doCreditCheck"
        },
        {
            "name": "send-rejection-email-function",
            "operation": "http://myapis.org/creditcheckapi.json#rejectionEmail"
        }
    ],
    "events": [
        {
            "name": "credit-check-completed-event",
            "type": "creditCheckCompleteType",
            "source": "creditCheckSource",
            "correlation": [
                {
                    "contextAttributeName": "customerId"
                }
            ]
        }
    ],
    "states": [
        {
            "name": "check-credit",
            "type": "callback",
            "action": {
                "name": "check-credit",
                "functionRef": {
                    "refName": "call-credit-check-microservice",
                    "arguments": {
                        "customer": "${ .customer }"
                    }
                }
            },
            "eventRef": "credit-check-completed-event",
            "timeouts": {
                "stateExecTimeout": "PT15M"
            },
            "transition": "evaluate-decision"
        },
        {
            "name": "evaluate-decision",
            "type": "switch",
            "dataConditions": [
                {
                    "condition": "${ .creditCheck | .decision == \"Approved\" }",
                    "transition": "start-application",
                    "name": "start-application"
                },
                {
                    "condition": "${ .creditCheck | .decision == \"Denied\" }",
                    "transition": "reject-application",
                    "name": "reject-application"
                }
            ],
            "defaultCondition": {
                "transition": "reject-application"
            }
        },
        {
            "name": "start-application",
            "type": "operation",
            "actions": [
                {
                    "name": "start-application",
                    "subFlowRef": "startApplicationWorkflowId"
                }
            ],
            "end": true
        },
        {
            "name": "reject-application",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name": "reject-application",
                    "functionRef": {
                        "refName": "send-rejection-email-function",
                        "arguments": {
                            "applicant": "${ .customer }"
                        }
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: customercreditcheck
version: 1.0.0
specVersion: "0.8"
name: customercreditcheck
description: Perform Customer Credit Check
start: check-credit
functions:
  - name: credit-check-function
    operation: http://myapis.org/creditcheckapi.json#doCreditCheck
  - name: send-rejection-email-function
    operation: http://myapis.org/creditcheckapi.json#rejectionEmail
events:
  - name: credit-check-completed-event
    type: creditCheckCompleteType
    source: creditCheckSource
    correlation:
      - contextAttributeName: customerId
states:
  - name: check-credit
    type: callback
    action:
      name: check-credit
      functionRef:
        refName: call-credit-check-microservice
        arguments:
          customer: ${ .customer }
    eventRef: credit-check-completed-event
    timeouts:
      stateExecTimeout: PT15M
    transition: evaluate-decision
  - name: evaluate-decision
    type: switch
    dataConditions:
      - condition: ${ .creditCheck | .decision == "Approved" }
        transition: start-application
        name: start-application
      - condition: ${ .creditCheck | .decision == "Denied" }
        transition: reject-application
        name: reject-application
    defaultCondition:
      transition: reject-application
  - name: start-application
    type: operation
    actions:
      - name: start-application
        subFlowRef: startApplicationWorkflowId
    end: true
  - name: reject-application
    type: operation
    actionMode: sequential
    actions:
      - name: reject-application
        functionRef:
          refName: send-rejection-email-function
          arguments:
            applicant: ${ .customer }
    end: true
```

</td>
</tr>
</table>

### Handle Car Auction Bids Example

#### Description

In this example our serverless workflow needs to handle bits for an online car auction. The car auction has a specific start
and end time. Bids are only allowed to be made during this time period. All bids before or after this time should not be considered.
We assume that the car auction starts at 9am UTC on March 20th 2020 and ends at 3pm UTC on March 20th 2020.

Bidding is done via an online application and bids are received as events are assumed to have the following format:

```json
{
  "specversion" : "1.0",
  "type" : "carBidType",
  "datacontenttype" : "application/json",
  ...
  "data": {
    "bid": [
        {
          "carid": "car123",
          "amount": 3000,
          "bidder": {
            "id": "xyz",
            "firstName": "John",
            "lastName": "Wayne"
          }
        }
      ]
  }
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-carauctionbid.png" height="500px" alt="Handle Car Auction Bid Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "handle-car-auction-bid",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "handle-car-auction-bid",
    "description": "Store a single bid whole the car auction is active",
    "start": {
        "stateName": "store-car-auction-bid",
        "schedule": "R/PT2H"
    },
    "functions": [
        {
            "name": "store-bid-function",
            "operation": "http://myapis.org/carauctionapi.json#storeBid"
        }
    ],
    "events": [
        {
            "name": "car-bid-event",
            "type": "carBidMadeType",
            "source": "carBidEventSource"
        }
    ],
    "states": [
        {
            "name": "store-car-auction-bid",
            "type": "event",
            "exclusive": true,
            "onEvents": [
                {
                    "eventRefs": [
                        "car-bid-event"
                    ],
                    "actions": [
                        {
                            "name": "car-bid-event",
                            "functionRef": {
                                "refName": "store-bid-function",
                                "arguments": {
                                    "bid": "${ .bid }"
                                }
                            }
                        }
                    ]
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: handle-car-auction-bid
version: 1.0.0
specVersion: "0.8"
name: handle-car-auction-bid
description: Store a single bid whole the car auction is active
start:
  stateName: store-car-auction-bid
  schedule: R/PT2H
functions:
  - name: store-bid-function
    operation: http://myapis.org/carauctionapi.json#storeBid
events:
  - name: car-bid-event
    type: carBidMadeType
    source: carBidEventSource
states:
  - name: store-car-auction-bid
    type: event
    exclusive: true
    onEvents:
      - eventRefs:
          - car-bid-event
        actions:
          - name: car-bid-event
            functionRef:
              refName: store-bid-function
              arguments:
                bid: ${ .bid }
    end: true
```

</td>
</tr>
</table>

### Check Inbox Periodically

#### Description

In this example we show the use of scheduled cron-based start event property. The example workflow checks the users inbox every 15 minutes
and send them a text message when there are important emails.

The results of the inbox service called is expected to be for example:

```json
{
    "messages": [
    {
      "title": "Update your health benefits",
      "from": "HR",
      "priority": "high"
    },
    {
      "title": "New job candidate resume",
      "from": "Recruiting",
      "priority": "medium"
    },
    ...
   ]
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-periodicalexec.png" height="400px" alt="Check Inbox Periodically Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "check-inbox",
    "name": "check-inbox",
    "version": "1.0.0",
    "specVersion": "0.8",
    "description": "Periodically Check Inbox",
    "start": {
        "stateName": "check-inbox",
        "schedule": {
            "cron": "0 0/15 * * * ?"
        }
    },
    "functions": [
        {
            "name": "check-inbox-function",
            "operation": "http://myapis.org/inboxapi.json#checkNewMessages"
        },
        {
            "name": "send-text-function",
            "operation": "http://myapis.org/inboxapi.json#sendText"
        }
    ],
    "states": [
        {
            "name": "check-inbox",
            "type": "operation",
            "actionMode": "sequential",
            "actions": [
                {
                    "name":"check-inbox",
                    "functionRef": "check-inbox-function"
                }
            ],
            "transition": "send-text-for-high-priority"
        },
        {
            "name": "send-text-for-high-priority",
            "type": "foreach",
            "inputCollection": "${ .messages }",
            "iterationParam": "singlemessage",
            "actions": [
                {
                    "name": "send-text-for-high-priority",
                    "functionRef": {
                        "refName": "send-text-function",
                        "arguments": {
                            "message": "${ .singlemessage }"
                        }
                    }
                }
            ],
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: check-inbox
name: check-inbox
version: 1.0.0
specVersion: "0.8"
description: Periodically Check Inbox
start:
  stateName: check-inbox
  schedule:
    cron: 0 0/15 * * * ?
functions:
  - name: check-inbox-function
    operation: http://myapis.org/inboxapi.json#checkNewMessages
  - name: send-text-function
    operation: http://myapis.org/inboxapi.json#sendText
states:
  - name: check-inbox
    type: operation
    actionMode: sequential
    actions:
      - name: check-inbox
        functionRef: check-inbox-function
    transition: send-text-for-high-priority
  - name: send-text-for-high-priority
    type: foreach
    inputCollection: ${ .messages }
    iterationParam: singlemessage
    actions:
      - name: send-text-for-high-priority
        functionRef:
          refName: send-text-function
          arguments:
            message: ${ .singlemessage }
    end: true
```

</td>
</tr>
</table>

### Event Based Service Invocation

#### Description

In this example we want to make a Veterinary appointment for our dog Mia. The vet service can be invoked only
via an event, and its completion results with the appointment day and time is returned via an event as well.

This shows a common scenario especially inside container environments where some services may not be exposed via
a resource URI, but only accessible by submitting an event to the underlying container events manager.

For this example we assume that that payload of the Vet service response event includes an "appointment"
object which contains our appointment info.

This info is then filtered to become the workflow data output. It could also be used to for example send us an
appointment email, a text message reminder, etc.

For this example we assume that the workflow instance is started given the following workflow data input:

```json
    {
      "patientInfo": {
        "name": "Mia",
        "breed": "German Shepherd",
        "age": 5,
        "reason": "Bee sting",
        "patientId": "Mia1"
      }
    }
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-vetappointment.png" height="400px" alt="Vet Appointment Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "vet-appointment-workflow",
    "name": "vet-appointment-workflow",
    "description": "Vet service call via events",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "make-vet-appointment-state",
    "events": [
        {
            "name": "make-vet-appointment",
            "source": "VetServiceSource",
            "type": "events.vet.appointments",
            "kind": "produced"
        },
        {
            "name": "vet-appointment-info",
            "source": "VetServiceSource",
            "type": "events.vet.appointments",
            "kind": "consumed"
        }
    ],
    "states": [
        {
            "name": "make-vet-appointment-state",
            "type": "operation",
            "actions": [
                {
                    "name": "make-appointment-action",
                    "eventRef": {
                        "produceEventRef": "make-vet-appointment",
                        "data": "${ .patientInfo }",
                        "consumeEventRef": "vet-appointment-info"
                    },
                    "actionDataFilter": {
                        "results": "${ .appointmentInfo }"
                    }
                }
            ],
            "timeouts": {
                "actionExecTimeout": "PT15M"
            },
            "end": true
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: vet-appointment-workflow
name: vet-appointment-workflow
description: Vet service call via events
version: 1.0.0
specVersion: "0.8"
start: make-vet-appointment-state
events:
  - name: make-vet-appointment
    source: VetServiceSource
    type: events.vet.appointments
    kind: produced
  - name: vet-appointment-info
    source: VetServiceSource
    type: events.vet.appointments
    kind: consumed
states:
  - name: make-vet-appointment-state
    type: operation
    actions:
      - name: make-appointment-action
        eventRef:
          produceEventRef: make-vet-appointment
          data: ${ .patientInfo }
          consumeEventRef: vet-appointment-info
        actionDataFilter:
          results: ${ .appointmentInfo }
    timeouts:
      actionExecTimeout: PT15M
    end: true
```

</td>
</tr>
</table>

### Reusing Function And Event Definitions

#### Description

This example shows how [function](../specification.md#Function-Definition) and [event](../specification.md#Event-Definition) definitions
can be declared independently and referenced by workflow definitions.
This is useful when you would like to reuse event and function definitions across multiple workflows. In those scenarios it allows you to make
changed/updates to these definitions in a single place without having to modify multiple workflows.

For the example we have two files, namely our "functiondefs.json" and "eventdefs.yml" (to show that they can be expressed in either JSON or YAML).
These hold our function and event definitions which then can be referenced by multiple workflows.

* functiondefs.json

```json
{
  "functions": [
      {
        "name": "checkFundsAvailability",
        "operation": "file://myapis/billingapis.json#checkFunds"
      },
      {
        "name": "sendSuccessEmail",
        "operation": "file://myapis/emailapis.json#paymentSuccess"
      },
      {
        "name": "sendInsufficientFundsEmail",
        "operation": "file://myapis/emailapis.json#paymentInsufficientFunds"
      }
    ]
}
```

* eventdefs.yml

```yaml
events:
- name: PaymentReceivedEvent
  type: payment.receive
  source: paymentEventSource
  correlation:
  - contextAttributeName: accountId
- name: ConfirmationCompletedEvent
  type: payment.confirmation
  kind: produced

```

In our workflow definition then we can reference these files rather than defining function and events in-line.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-reusefunceventdefs.png" height="400px" alt="Reusing Function and Event Definitions Example"/>
</p>

#### Workflow Definitions

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "paymentconfirmation",
    "version": "1.0.0",
    "specVersion": "0.8",
    "name": "paymentconfirmation",
    "description": "Performs Payment Confirmation",
    "functions": "file://functiondefs.json",
    "events": "file://eventdefs.yml",
    "states": [
        {
            "name": "payment-received",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "payment-received-event"
                    ],
                    "actions": [
                        {
                            "name": "checkfunds",
                            "functionRef": {
                                "refName": "check-funds-availability",
                                "arguments": {
                                    "account": "${ .accountId }",
                                    "paymentamount": "${ .payment.amount }"
                                }
                            }
                        }
                    ]
                }
            ],
            "transition": "confirm-based-on-funds"
        },
        {
            "name": "confirm-based-on-funds",
            "type": "switch",
            "dataConditions": [
                {
                    "condition": "${ .funds | .available == \"true\" }",
                    "transition": "send-payment-success",
                    "name": "success"
                },
                {
                    "condition": "${ .funds | .available == \"false\" }",
                    "transition": "send-insufficient-results",
                    "name": "failed"
                }
            ],
            "defaultCondition": {
                "transition": "send-payment-success"
            }
        },
        {
            "name": "send-payment-success",
            "type": "operation",
            "actions": [
                {
                    "name": "send-payment-success",
                    "functionRef": {
                        "refName": "send-success-email",
                        "arguments": {
                            "applicant": "${ .customer }"
                        }
                    }
                }
            ],
            "end": {
                "produceEvents": [
                    {
                        "eventRef": "confirmation-completed-event",
                        "data": "${ .payment }"
                    }
                ]
            }
        },
        {
            "name": "send-insufficient-results",
            "type": "operation",
            "actions": [
                {
                    "name": "send-insufficient-results",
                    "functionRef": {
                        "refName": "send-insufficient-funds-email",
                        "arguments": {
                            "applicant": "${ .customer }"
                        }
                    }
                }
            ],
            "end": {
                "produceEvents": [
                    {
                        "eventRef": "confirmation-completed-event",
                        "data": "${ .payment }"
                    }
                ]
            }
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: paymentconfirmation
version: 1.0.0
specVersion: "0.8"
name: paymentconfirmation
description: Performs Payment Confirmation
functions: file://functiondefs.json
events: file://eventdefs.yml
states:
  - name: payment-received
    type: event
    onEvents:
      - eventRefs:
          - payment-received-event
        actions:
          - name: checkfunds
            functionRef:
              refName: check-funds-availability
              arguments:
                account: ${ .accountId }
                paymentamount: ${ .payment.amount }
    transition: confirm-based-on-funds
  - name: confirm-based-on-funds
    type: switch
    dataConditions:
      - condition: ${ .funds | .available == "true" }
        transition: send-payment-success
        name: success
      - condition: ${ .funds | .available == "false" }
        transition: send-insufficient-results
        name: failed
    defaultCondition:
      transition: send-payment-success
  - name: send-payment-success
    type: operation
    actions:
      - name: send-payment-success
        functionRef:
          refName: send-success-email
          arguments:
            applicant: ${ .customer }
    end:
      produceEvents:
        - eventRef: confirmation-completed-event
          data: ${ .payment }
  - name: send-insufficient-results
    type: operation
    actions:
      - name: send-insufficient-results
        functionRef:
          refName: send-insufficient-funds-email
          arguments:
            applicant: ${ .customer }
    end:
      produceEvents:
        - eventRef: confirmation-completed-event
          data: ${ .payment }
```

</td>
</tr>
</table>

### New Patient Onboarding

#### Description

In this example we want to use a workflow to onboard a new patient (at a hospital for example).
To onboard a patient our workflow is invoked via a "NewPatientEvent" event. This events payload contains the
patient information, for example:

```json
{
  "name": "John",
  "condition": "chest pains"
}
```

When this event is received we want to create a new workflow instance and invoke three services
sequentially. The first service we want to invoke is responsible to store patient information,
second is to assign a doctor to a patient given the patient condition, and third to assign a
new appoitment with the patient and the assigned doctor.

In addition, in this example we need to handle a possible situation where one or all of the needed
services are not available (the server returns a http 503 (Service Unavailable) error). If our workflow
catches this error, we want to try to recover from this by issuing retries for the particular
service invocation that caused the error up to 10 times with three seconds in-between retries.
If the retries are not successful, we want to just gracefully end workflow execution.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-patientonboarding.png" height="400px" alt="Patient Onboarding Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "patientonboarding",
    "name": "patientonboarding",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "onboard",
    "states": [
        {
            "name": "onboard",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "new-patient-event"
                    ],
                    "actions": [
                        {
                            "name": "store-patient",
                            "functionRef": "store-patient",
                            "retryRef": "services-not-available-retry-strategy",
                            "retryableErrors": [
                                "service-not-available"
                            ]
                        },
                        {
                            "name": "assign-doctor",
                            "functionRef": "assign-doctor",
                            "retryRef": "services-not-available-retry-strategy",
                            "retryableErrors": [
                                "service-not-available"
                            ]
                        },
                        {
                            "name": "schedule-appt",
                            "functionRef": "schedule-appt",
                            "retryRef": "services-not-available-retry-strategy",
                            "retryableErrors": [
                                "service-not-available"
                            ]
                        }
                    ]
                }
            ],
            "onErrors": [
                {
                    "errorRef": "service-not-available",
                    "end": true
                }
            ],
            "end": true
        }
    ],
    "events": [
        {
            "name": "store-patient",
            "type": "new.patients.event",
            "source": "newpatient/+"
        }
    ],
    "functions": [
        {
            "name": "store-new-patient-info",
            "operation": "api/services.json#addPatient"
        },
        {
            "name": "assign-doctor",
            "operation": "api/services.json#assignDoctor"
        },
        {
            "name": "schedule-appt",
            "operation": "api/services.json#scheduleAppointment"
        }
    ],
    "errors": [
        {
            "name": "service-not-available",
            "code": "503"
        }
    ],
    "retries": [
        {
            "name": "services-not-available-retry-strategy",
            "delay": "PT3S",
            "maxAttempts": 10
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: patientonboarding
name: patientonboarding
version: 1.0.0
specVersion: "0.8"
start: onboard
states:
  - name: onboard
    type: event
    onEvents:
      - eventRefs:
          - new-patient-event
        actions:
          - name: store-patient
            functionRef: store-patient
            retryRef: services-not-available-retry-strategy
            retryableErrors:
              - service-not-available
          - name: assign-doctor
            functionRef: assign-doctor
            retryRef: services-not-available-retry-strategy
            retryableErrors:
              - service-not-available
          - name: schedule-appt
            functionRef: schedule-appt
            retryRef: services-not-available-retry-strategy
            retryableErrors:
              - service-not-available
    onErrors:
      - errorRef: service-not-available
        end: true
    end: true
events:
  - name: store-patient
    type: new.patients.event
    source: newpatient/+
functions:
  - name: store-new-patient-info
    operation: api/services.json#addPatient
  - name: assign-doctor
    operation: api/services.json#assignDoctor
  - name: schedule-appt
    operation: api/services.json#scheduleAppointment
errors:
  - name: service-not-available
    code: "503"
retries:
  - name: services-not-available-retry-strategy
    delay: PT3S
    maxAttempts: 10
```

</td>
</tr>
</table>

#### Workflow Demo

This example is used in our Serverless Workflow Hands-on series videos [#1](https://www.youtube.com/watch?v=0gmpuGLP-_o)
and [#2](https://www.youtube.com/watch?v=6A6OYp5nygg).

### Purchase order deadline

#### Description

In this example our workflow processes purchase orders. An order event triggers instance of our workflow.
To complete the created order, our workflow must first wait for an order confirmation event (correlated to the
order id), and then wait for the shipment sent event (also correlated to initial order id).
We do not want to place an exact timeout limit for waiting for the confirmation and shipment events,
as this might take a different amount of time depending on the size of the order. However we do have the requirement
that a total amount of time for the order to be confirmed, once its created, is 30 days.
If the created order is not completed within 30 days it needs to be automatically closed.

This example shows the use of the workflow [execTimeout definition](../specification.md#ExecTimeout-Definition).

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-purchaseorderdeadline.png" height="400px" alt="Purchase Order Deadline Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "order",
    "name": "order",
    "description": "Purchase Order Workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "start-new-order",
    "timeouts": {
        "workflowExecTimeout": {
            "duration": "PT30D",
            "runBefore": "CancelOrder"
        }
    },
    "states": [
        {
            "name": "start-new-order",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "order-created-event"
                    ],
                    "actions": [
                        {
                            "name": "log-new-order-created",
                            "functionRef": {
                                "refName": "log-new-order-created"
                            }
                        }
                    ]
                }
            ],
            "transition": {
                "nextState": "wait-for-order-confirmation"
            }
        },
        {
            "name": "wait-for-order-confirmation",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "order-confirmed-event"
                    ],
                    "actions": [
                        {
                            "name": "log-order-confirmed",
                            "functionRef": {
                                "refName": "log-order-confirmed"
                            }
                        }
                    ]
                }
            ],
            "transition": {
                "nextState": "wait-order-shipped"
            }
        },
        {
            "name": "wait-order-shipped",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "shipment-sent-event"
                    ],
                    "actions": [
                        {
                            "name": "log-order-shipped",
                            "functionRef": {
                                "refName": "log-order-shipped"
                            }
                        }
                    ]
                }
            ],
            "end": {
                "terminate": true,
                "produceEvents": [
                    {
                        "eventRef": "order-finished-event"
                    }
                ]
            }
        },
        {
            "name": "cancel-order",
            "type": "operation",
            "actions": [
                {
                    "name": "cancel-order",
                    "functionRef": {
                        "refName": "cancel-order"
                    }
                }
            ],
            "end": {
                "terminate": true,
                "produceEvents": [
                    {
                        "eventRef": "order-cancelled-event"
                    }
                ]
            }
        }
    ],
    "events": [
        {
            "name": "order-created-event",
            "type": "my.company.orders",
            "source": "/orders/new",
            "correlation": [
                {
                    "contextAttributeName": "orderid"
                }
            ]
        },
        {
            "name": "order-confirmed-event",
            "type": "my.company.orders",
            "source": "/orders/confirmed",
            "correlation": [
                {
                    "contextAttributeName": "orderid"
                }
            ]
        },
        {
            "name": "shipment-sent-event",
            "type": "my.company.orders",
            "source": "/orders/shipped",
            "correlation": [
                {
                    "contextAttributeName": "orderid"
                }
            ]
        },
        {
            "name": "order-finished-event",
            "type": "my.company.orders",
            "kind": "produced"
        },
        {
            "name": "order-cancelled-event",
            "type": "my.company.orders",
            "kind": "produced"
        }
    ],
    "functions": [
        {
            "name": "log-new-order-created",
            "operation": "http.myorg.io/ordersservices.json#logcreated"
        },
        {
            "name": "log-order-confirmed",
            "operation": "http.myorg.io/ordersservices.json#logconfirmed"
        },
        {
            "name": "log-order-shipped",
            "operation": "http.myorg.io/ordersservices.json#logshipped"
        },
        {
            "name": "cancel-order",
            "operation": "http.myorg.io/ordersservices.json#calcelorder"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: order
name: order
description: Purchase Order Workflow
version: 1.0.0
specVersion: "0.8"
start: start-new-order
timeouts:
  workflowExecTimeout:
    duration: PT30D
    runBefore: CancelOrder
states:
  - name: start-new-order
    type: event
    onEvents:
      - eventRefs:
          - order-created-event
        actions:
          - name: log-new-order-created
            functionRef:
              refName: log-new-order-created
    transition:
      nextState: wait-for-order-confirmation
  - name: wait-for-order-confirmation
    type: event
    onEvents:
      - eventRefs:
          - order-confirmed-event
        actions:
          - name: log-order-confirmed
            functionRef:
              refName: log-order-confirmed
    transition:
      nextState: wait-order-shipped
  - name: wait-order-shipped
    type: event
    onEvents:
      - eventRefs:
          - shipment-sent-event
        actions:
          - name: log-order-shipped
            functionRef:
              refName: log-order-shipped
    end:
      terminate: true
      produceEvents:
        - eventRef: order-finished-event
  - name: cancel-order
    type: operation
    actions:
      - name: cancel-order
        functionRef:
          refName: cancel-order
    end:
      terminate: true
      produceEvents:
        - eventRef: order-cancelled-event
events:
  - name: order-created-event
    type: my.company.orders
    source: /orders/new
    correlation:
      - contextAttributeName: orderid
  - name: order-confirmed-event
    type: my.company.orders
    source: /orders/confirmed
    correlation:
      - contextAttributeName: orderid
  - name: shipment-sent-event
    type: my.company.orders
    source: /orders/shipped
    correlation:
      - contextAttributeName: orderid
  - name: order-finished-event
    type: my.company.orders
    kind: produced
  - name: order-cancelled-event
    type: my.company.orders
    kind: produced
functions:
  - name: log-new-order-created
    operation: http.myorg.io/ordersservices.json#logcreated
  - name: log-order-confirmed
    operation: http.myorg.io/ordersservices.json#logconfirmed
  - name: log-order-shipped
    operation: http.myorg.io/ordersservices.json#logshipped
  - name: cancel-order
    operation: http.myorg.io/ordersservices.json#calcelorder
```

</td>
</tr>
</table>

### Accumulate room readings

#### Description

In this example we have two IoT sensors for each room in our house. One reads temperature values
and the other humidity values of each room. We get these measurements for each of our rooms
as CloudEvents. We can correlate events send by our sensors by the room it is in.

For the example we want to accumulate the temperature and humidity values for each and send hourly reports
to the home owner for each room.

**Note:** In this example each rooms measurements will be accumulated by a single workflow instance per room.
Once we receive events for 1 hour (per room) each of the room-based workflow instances will create the report. Events
consumed after the report is created will trigger a new instance of our workflow (again, per room), accumulate
the data for an hour, send report, and so on.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-accumulateroomreadings.png" height="400px" alt="Accumulate Room Readings Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "roomreadings",
    "name": "roomreadings",
    "description": "Room Temp and Humidity Workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "consume-reading",
    "timeouts": {
        "workflowExecTimeout": {
            "duration": "PT1H",
            "runBefore": "generate-report"
        }
    },
    "keepActive": true,
    "states": [
        {
            "name": "consume-reading",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "temperature-event",
                        "humidity-event"
                    ],
                    "actions": [
                        {
                            "name": "log-reading",
                            "functionRef": {
                                "refName": "log-reading"
                            }
                        }
                    ],
                    "eventDataFilter": {
                        "toStateData": "${ .readings }"
                    }
                }
            ],
            "end": true
        },
        {
            "name": "generate-report",
            "type": "operation",
            "actions": [
                {
                    "name": "generate-report",
                    "functionRef": {
                        "refName": "produce-report",
                        "arguments": {
                            "data": "${ .readings }"
                        }
                    }
                }
            ],
            "end": {
                "terminate": true
            }
        }
    ],
    "events": [
        {
            "name": "temperature-event",
            "type": "my.home.sensors",
            "source": "/home/rooms/+",
            "correlation": [
                {
                    "contextAttributeName": "roomId"
                }
            ]
        },
        {
            "name": "humidity-event",
            "type": "my.home.sensors",
            "source": "/home/rooms/+",
            "correlation": [
                {
                    "contextAttributeName": "roomId"
                }
            ]
        }
    ],
    "functions": [
        {
            "name": "log-reading",
            "operation": "http.myorg.io/ordersservices.json#logreading"
        },
        {
            "name": "produce-report",
            "operation": "http.myorg.io/ordersservices.json#produceReport"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: roomreadings
name: roomreadings
description: Room Temp and Humidity Workflow
version: 1.0.0
specVersion: "0.8"
start: consume-reading
timeouts:
  workflowExecTimeout:
    duration: PT1H
    runBefore: generate-report
keepActive: true
states:
  - name: consume-reading
    type: event
    onEvents:
      - eventRefs:
          - temperature-event
          - humidity-event
        actions:
          - name: log-reading
            functionRef:
              refName: log-reading
        eventDataFilter:
          toStateData: ${ .readings }
    end: true
  - name: generate-report
    type: operation
    actions:
      - name: generate-report
        functionRef:
          refName: produce-report
          arguments:
            data: ${ .readings }
    end:
      terminate: true
events:
  - name: temperature-event
    type: my.home.sensors
    source: /home/rooms/+
    correlation:
      - contextAttributeName: roomId
  - name: humidity-event
    type: my.home.sensors
    source: /home/rooms/+
    correlation:
      - contextAttributeName: roomId
functions:
  - name: log-reading
    operation: http.myorg.io/ordersservices.json#logreading
  - name: produce-report
    operation: http.myorg.io/ordersservices.json#produceReport
```

</td>
</tr>
</table>

### Car Vitals Checks

#### Description

In this example we need to check car vital signs while our car is driving.
The workflow should start when we receive the "CarTurnedOnEvent" event and stop when the "CarTurnedOffEvent" event is consumed.
While the car is driving our workflow should repeatedly check the vitals every 1 second.

For this example we use the workflow [SubFlow](../specification.md#SubFlow-Action) actions to perform the vital checks.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-checkcarvitals.png" height="550px" alt="Check Car Vitals Example"/>
</p>

#### Workflow Definition

We fist define our top-level workflow for this example:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "checkcarvitals",
    "name": "checkcarvitals",
    "description": "Check Car Vitals Workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "when-car-is-on",
    "states": [
        {
            "name": "when-car-is-on",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "car-turned-on-event"
                    ]
                }
            ],
            "transition": "do-car-vital-checks"
        },
        {
            "name": "do-car-vital-checks",
            "type": "operation",
            "actions": [
                {
                    "name": "do-car-vital-checks",
                    "subFlowRef": "vitalscheck",
                    "sleep": {
                        "after": "PT1S"
                    }
                }
            ],
            "transition": "check-continue-vital-checks"
        },
        {
            "name": "check-continue-vital-checks",
            "type": "switch",
            "eventConditions": [
                {
                    "name": "car-turned-off-condition",
                    "eventRef": "car-turned-off-event",
                    "end": true
                }
            ],
            "defaultCondition": {
                "transition": "do-car-vital-checks"
            }
        }
    ],
    "events": [
        {
            "name": "car-turned-on-event",
            "type": "car.events",
            "source": "my/car"
        },
        {
            "name": "car-turned-off-event",
            "type": "car.events",
            "source": "my/car"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: checkcarvitals
name: checkcarvitals
description: Check Car Vitals Workflow
version: 1.0.0
specVersion: "0.8"
start: when-car-is-on
states:
  - name: when-car-is-on
    type: event
    onEvents:
      - eventRefs:
          - car-turned-on-event
    transition: do-car-vital-checks
  - name: do-car-vital-checks
    type: operation
    actions:
      - name: do-car-vital-checks
        subFlowRef: vitalscheck
        sleep:
          after: PT1S
    transition: check-continue-vital-checks
  - name: check-continue-vital-checks
    type: switch
    eventConditions:
      - name: car-turned-off-condition
        eventRef: car-turned-off-event
        end: true
    defaultCondition:
      transition: do-car-vital-checks
events:
  - name: car-turned-on-event
    type: car.events
    source: my/car
  - name: car-turned-off-event
    type: car.events
    source: my/car
```

</td>
</tr>
</table>

And then our reusable sub-workflow which performs the checking of our car vitals:

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "vitalscheck",
    "name": "vitalscheck",
    "description": "Car Vitals Check",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "check-vitals",
    "states": [
        {
            "name": "check-vitals",
            "type": "operation",
            "actions": [
                {
                    "name": "check-tire-pressure",
                    "functionRef": "check-tire-pressure"
                },
                {
                    "name": "check-oil-pressure",
                    "functionRef": "check-oil-pressure"
                },
                {
                    "name": "check-coolant-level",
                    "functionRef": "check-coolant-level"
                },
                {
                    "name": "check-battery",
                    "functionRef": "check-battery"
                }
            ],
            "end": {
                "produceEvents": [
                    {
                        "eventRef": "display-checks-on-dashboard",
                        "data": "${ .evaluations }"
                    }
                ]
            }
        }
    ],
    "functions": [
        {
            "name": "check-tire-pressure",
            "operation": "mycarservices.json#checktirepressure"
        },
        {
            "name": "check-oil-pressure",
            "operation": "mycarservices.json#checkoilpressure"
        },
        {
            "name": "check-coolant-level",
            "operation": "mycarservices.json#checkcoolantlevel"
        },
        {
            "name": "check-battery",
            "operation": "mycarservices.json#checkbattery"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: vitalscheck
name: vitalscheck
description: Car Vitals Check
version: 1.0.0
specVersion: "0.8"
start: check-vitals
states:
  - name: check-vitals
    type: operation
    actions:
      - name: check-tire-pressure
        functionRef: check-tire-pressure
      - name: check-oil-pressure
        functionRef: check-oil-pressure
      - name: check-coolant-level
        functionRef: check-coolant-level
      - name: check-battery
        functionRef: check-battery
    end:
      produceEvents:
        - eventRef: display-checks-on-dashboard
          data: ${ .evaluations }
functions:
  - name: check-tire-pressure
    operation: mycarservices.json#checktirepressure
  - name: check-oil-pressure
    operation: mycarservices.json#checkoilpressure
  - name: check-coolant-level
    operation: mycarservices.json#checkcoolantlevel
  - name: check-battery
    operation: mycarservices.json#checkbattery
```

</td>
<td valign="top">

```yaml
id: vitalscheck
name: Car Vitals Check
version: '1.0.0'
specVersion: '0.8'
start: CheckVitals
states:
 - name: CheckVitals
   type: operation
   actions:
    - functionRef: Check Tire Pressure
    - functionRef: Check Oil Pressure
    - functionRef: Check Coolant Level
    - functionRef: Check Battery
   end:
    produceEvents:
     - eventRef: DisplayChecksOnDashboard
       data: "${ .evaluations }"
functions:
 - name: checkTirePressure
   operation: mycarservices.json#checktirepressure
 - name: checkOilPressure
   operation: mycarservices.json#checkoilpressure
 - name: checkCoolantLevel
   operation: mycarservices.json#checkcoolantlevel
 - name: checkBattery
   operation: mycarservices.json#checkbattery
```

</td>
</tr>
</table>

### Book Lending

#### Description

In this example we want to create a book lending workflow. The workflow starts when a lender
submits a book lending request (via event "Book Lending Request Event").
The workflow describes our business logic around lending a book, from checking its current availability,
to waiting on the lender's response if the book is currently not available, to checking out the book and notifying
the lender.

This example expects the "Book Lending Request Event" event to have a payload of for example:

```json
{
    "book": {
        "title": " ... ",
        "id": " ... "
    },
    "lender": {
        "name": "John Doe",
        "address": " ... ",
        "phone": " ... "
    }
}
```

where the "book" property defines the book to be lent out, and the "lender" property provides info
about the person wanting to lend the book.

For the sake of the example we assume the functions and event definitions are defined in separate JSON files.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-booklending.png" height="400px" alt="Book Lending Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "booklending",
    "name": "booklending",
    "description": "Book Lending Workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "book-lending-request",
    "states": [
        {
            "name": "book-lending-request",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "book-lending-request-event"
                    ]
                }
            ],
            "transition": "get-book-status"
        },
        {
            "name": "get-book-status",
            "type": "operation",
            "actions": [
                {
                    "name": "get-book-status",
                    "functionRef": {
                        "refName": "get-status-for-book",
                        "arguments": {
                            "bookid": "${ .book.id }"
                        }
                    }
                }
            ],
            "transition": "book-status-decision"
        },
        {
            "name": "book-status-decision",
            "type": "switch",
            "dataConditions": [
                {
                    "name": "book-is-on-loan",
                    "condition": "${ .book.status == \"onloan\" }",
                    "transition": "report-status-to-lender"
                },
                {
                    "name": "check-is-available",
                    "condition": "${ .book.status == \"available\" }",
                    "transition": "check-out-book"
                }
            ],
            "defaultCondition": {
                "end": true
            }
        },
        {
            "name": "report-status-to-lender",
            "type": "operation",
            "actions": [
                {
                    "name": "report-status-to-lender",
                    "functionRef": {
                        "refName": "send-status-to-lender",
                        "arguments": {
                            "bookid": "${ .book.id }",
                            "message": "Book ${ .book.title } is already on loan"
                        }
                    }
                }
            ],
            "transition": "wait-for-lender-response"
        },
        {
            "name": "wait-for-lender-response",
            "type": "switch",
            "eventConditions": [
                {
                    "name": "hold-book",
                    "eventRef": "hold-book-event",
                    "transition": "request-hold"
                },
                {
                    "name": "decline-book-hold",
                    "eventRef": "decline-hold-event",
                    "transition": "cancel-request"
                }
            ],
            "defaultCondition": {
                "end": true
            }
        },
        {
            "name": "request-hold",
            "type": "operation",
            "actions": [
                {
                    "name": "request-hold",
                    "functionRef": {
                        "refName": "request-hold-for-lender",
                        "arguments": {
                            "bookid": "${ .book.id }",
                            "lender": "${ .lender }"
                        }
                    }
                }
            ],
            "transition": "sleep-two-weeks"
        },
        {
            "name": "cancel-request",
            "type": "operation",
            "actions": [
                {
                    "name": "cancel-request",
                    "functionRef": {
                        "refName": "cancel-hold-request-for-lender",
                        "arguments": {
                            "bookid": "${ .book.id }",
                            "lender": "${ .lender }"
                        }
                    }
                }
            ],
            "transition": "sleep-two-weeks"
        },
        {
            "name": "sleep-two-weeks",
            "type": "sleep",
            "duration": "PT2W",
            "transition": "get-book-status"
        },
        {
            "name": "check-out-book",
            "type": "operation",
            "actions": [
                {
                    "name": "check-out-book",
                    "functionRef": {
                        "refName": "check-out-book-with-id",
                        "arguments": {
                            "bookid": "${ .book.id }"
                        }
                    }
                },
                {
                    "name": "notify-lender-for-checkout",
                    "functionRef": {
                        "refName": "notify-lender-for-checkout",
                        "arguments": {
                            "bookid": "${ .book.id }",
                            "lender": "${ .lender }"
                        }
                    }
                }
            ],
            "end": true
        }
    ],
    "functions": "file://books/lending/functions.json",
    "events": "file://books/lending/events.json"
}```

</td>
<td valign="top">

```yaml
id: booklending
name: booklending
description: Book Lending Workflow
version: 1.0.0
specVersion: "0.8"
start: book-lending-request
states:
  - name: book-lending-request
    type: event
    onEvents:
      - eventRefs:
          - book-lending-request-event
    transition: get-book-status
  - name: get-book-status
    type: operation
    actions:
      - name: get-book-status
        functionRef:
          refName: get-status-for-book
          arguments:
            bookid: ${ .book.id }
    transition: book-status-decision
  - name: book-status-decision
    type: switch
    dataConditions:
      - name: book-is-on-loan
        condition: ${ .book.status == "onloan" }
        transition: report-status-to-lender
      - name: check-is-available
        condition: ${ .book.status == "available" }
        transition: check-out-book
    defaultCondition:
      end: true
  - name: report-status-to-lender
    type: operation
    actions:
      - name: report-status-to-lender
        functionRef:
          refName: send-status-to-lender
          arguments:
            bookid: ${ .book.id }
            message: Book ${ .book.title } is already on loan
    transition: wait-for-lender-response
  - name: wait-for-lender-response
    type: switch
    eventConditions:
      - name: hold-book
        eventRef: hold-book-event
        transition: request-hold
      - name: decline-book-hold
        eventRef: decline-hold-event
        transition: cancel-request
    defaultCondition:
      end: true
  - name: request-hold
    type: operation
    actions:
      - name: request-hold
        functionRef:
          refName: request-hold-for-lender
          arguments:
            bookid: ${ .book.id }
            lender: ${ .lender }
    transition: sleep-two-weeks
  - name: cancel-request
    type: operation
    actions:
      - name: cancel-request
        functionRef:
          refName: cancel-hold-request-for-lender
          arguments:
            bookid: ${ .book.id }
            lender: ${ .lender }
    transition: sleep-two-weeks
  - name: sleep-two-weeks
    type: sleep
    duration: PT2W
    transition: get-book-status
  - name: check-out-book
    type: operation
    actions:
      - name: check-out-book
        functionRef:
          refName: check-out-book-with-id
          arguments:
            bookid: ${ .book.id }
      - name: notify-lender-for-checkout
        functionRef:
          refName: notify-lender-for-checkout
          arguments:
            bookid: ${ .book.id }
            lender: ${ .lender }
    end: true
functions: file://books/lending/functions.json
events: file://books/lending/events.json
```

</td>
</tr>
</table>

### Filling a glass of water

#### Description

In this example we showcase the power of [expression functions](../specification.md#Using-Functions-For-Expression-Evaluation).
Our workflow definition is assumed to have the following data input:

```json
{
  "counts": {
    "current": 0,
    "max": 10
  }
}
```

Our workflow simulates filling up a glass of water one "count" at a time until "max" count is reached which
represents our glass is full.
Each time we increment the current count, the workflow checks if we need to keep refilling the glass.
If the current count reaches the max count, the workflow execution ends.
To increment the current count, the workflow invokes the "IncrementCurrent" expression function.
Its results are then merged back into the state data according to the "toStateData" property of the event data filter.

#### Workflow Diagram

<p align="center">
<img src="../media/examples/examples-fill-glass.png" height="300px" alt="Fill Glass of Water Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "fillglassofwater",
    "name": "fillglassofwater",
    "description": "Fill glass of water workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "check-if-full",
    "functions": [
        {
            "name": "increment-current-count-function",
            "type": "expression",
            "operation": ".counts.current += 1 | .counts.current"
        }
    ],
    "states": [
        {
            "name": "check-if-full",
            "type": "switch",
            "dataConditions": [
                {
                    "name": "need-to-fill-more",
                    "condition": "${ .counts.current < .counts.max }",
                    "transition": "add-water"
                },
                {
                    "name": "glass-full",
                    "condition": ".counts.current >= .counts.max",
                    "end": true
                }
            ],
            "defaultCondition": {
                "end": true
            }
        },
        {
            "name": "add-water",
            "type": "operation",
            "actions": [
                {
                    "name": "add-water",
                    "functionRef": "increment-current-count-function",
                    "actionDataFilter": {
                        "toStateData": ".counts.current"
                    }
                }
            ],
            "transition": "check-if-full"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: fillglassofwater
name: fillglassofwater
description: Fill glass of water workflow
version: 1.0.0
specVersion: "0.8"
start: check-if-full
functions:
  - name: increment-current-count-function
    type: expression
    operation: .counts.current += 1 | .counts.current
states:
  - name: check-if-full
    type: switch
    dataConditions:
      - name: need-to-fill-more
        condition: ${ .counts.current < .counts.max }
        transition: add-water
      - name: glass-full
        condition: .counts.current >= .counts.max
        end: true
    defaultCondition:
      end: true
  - name: add-water
    type: operation
    actions:
      - name: add-water
        functionRef: increment-current-count-function
        actionDataFilter:
          toStateData: .counts.current
    transition: check-if-full
```

</td>
</tr>
</table>

### Online Food Ordering

#### Description

In this example we want to create an online food ordering workflow. The below image outlines the workflow
structure and the available services:

<p align="center">
<img src="../media/examples/example-foodorder-outline.png" height="450px" alt="Online Food Ordering Structure"/>
</p>

Our workflow starts with the "Place Order" [Subflow](../specification.md#SubFlow-Action), which is responsible
to send the received order to the requested restaurant and the estimated order ETA.
We then wait for the ETA time when our workflow should go into the "Deliver Order" SubFlow, responsible
for dispatching a Courier and sending her/him off to pick up the order. Once the order is picked up, the Courier needs to deliver the order to the customer.
After the order has been delivered to the customer, our workflow needs to charge the customer.

Our workflow needs to communicate with three services during its execution, namely the Order, Delivery, and
the Payment services.

For the sake of the example, we assume that our workflow can communicate to the Order and Delivery services via REST and the Payment service via gRPC.
Let's start by defining an example CloudEvent which triggers an instance of our workflow.
This event can be sent by a web UI, for example, or be pushed onto a Kafka/MQTT topic to start our order workflow.

```json
{
   "specversion": "1.0",
   "type": "org.orders",
   "source": "/orders/",
   "subject": "Food Order",
   "id": "A234-1234-1234",
   "time": "2021-03-05T17:31:00Z",
   "orderid": "ORDER-12345",
   "data": {
      "id": "ORDER-12345",
      "customerId": "CUSTOMER-12345",
      "status": [],
      "order": {
         "restaurantId": "RESTAURANT-54321",
         "items": [
            {
               "itemId": "ITEM-8765",
               "amount": 1,
               "addons": ""
            }
         ]
      },
      "delivery":{
         "address": "1234 MyStreet, MyCountry",
         "type": "contactless",
         "requestedTime": "ASAP",
         "location": "Front door",
         "instructions": ""
      }
   }
}
```

Note the `orderid` CloudEvent context attribute, which contains the unique ID of the order specified in this event. [Event correlation](../specification.md#Correlation-Definition) is done against CE context attributes, and as such, to be able
to correlate multiple order events to the same order id, it needs to be part of the CE context attributes, and
not its data (payload).

Now let's start defining our workflow. For the sake of this example, let's define our function and event definitions
as separate YAML files (and then reference them inside our workflow definition). This is useful in cases
when you want to reuse them between multiple workflow definitions.

#### Workflow Event Definition

``` yaml
events:
- name: Food Order Event
  source: "/orders/"
  type: org.orders
  correlation:
  - contextAttributeName: orderid
- name: ETA Deadline Event
  source: "/orderseta"
  type: org.orders.eta
  correlation:
  - contextAttributeName: orderid
- name: Order Picked Up Event
  source: "/orderspickup"
  type: org.orders.delivery
  correlation:
  - contextAttributeName: orderid
- name: Order Delievered Event
  source: "/orderdelivery"
  type: org.orders.delivery
  correlation:
  - contextAttributeName: orderid
```

#### Workflow Function Definition

``` yaml
functions:
- name: Submit Order Function
  operation: http://myorderservice.org/orders.json#submit
- name: Get Order ETA Function
  operation: http://myorderservice.org/orders.json#orderETA
- name: Dispatch Courrier Function
  operation: http://mydeliveryservice.org/deliveries.json#dispatch
- name: Deliver Order Function
  operation: http://mydeliveryservice.org/deliveries.json#deliver
- name: Charge For Order Function
  operation: http://mypaymentservice.org/payments.proto#PaymentService#ChargeUser
```

#### Main Workflow Definition

With the function and event definitions in place we can now start writing our main workflow definition:

```yaml
id: foodorderworkflow
name: Food Order Workflow
version: '1.0.0'
specVersion: '0.8'
start: Place Order
functions: file://orderfunctions.yml
events: file://orderevents.yml
states:
- name: Place Order
  type: operation
  actions:
  - subFlowRef: placeorderworkflow
  transition: Wait for ETA Deadline
- name: Wait for ETA Deadline
  type: event
  onEvents:
  - eventRefs:
    - ETA Deadline Event
    eventDataFilter:
      data: "${ .results.status }"
      toStateData: "${ .status }"
  transition: Deliver Order
- name: Deliver Order
  type: operation
  actions:
  - subFlowRef: deliverorderworkflow
  transition: Charge For Order
- name: Charge For Order
  type: operation
  actions:
  - functionRef:
      refName: Charge For Order Function
      arguments:
        order: "${ .order.id }"
    actionDataFilter:
      results: "${ .outcome.status }"
      toStateData: "${ .status }"
  stateDataFilter:
    output: '${ . | {"orderid": .id, "orderstatus": .status} | .orderstatus += ["Order
      Completed"] }'
  end: true
```

With this in place we can start defining our sub-workflows:

#### Place Order Sub-Workflow

```yaml
id: placeorderworkflow
name: Place Order Workflow
version: '1.0.0'
specVersion: '0.8'
start: Submit Order
states:
- name: Submit Order
  type: event
  onEvents:
  - eventRefs:
    - Food Order Event
    actions:
    - functionRef:
        refName: Submit Order Function
        arguments:
          order: "${ .order }"
      actionDataFilter:
        results: "${ .results.status }"
        toStateData: "${ .status }"
    - functionRef:
        refName: Get Order ETA Function
        arguments:
          customer: "${ .customerId }"
          restaurantid: "${ .order.restaurantId }"
          delivery: " ${ .delivery }"
      actionDataFilter:
        results: "${ .results.status }"
        toStateData: "${ .status }"
  end: true
```

#### Deliver Order Sub-Workflow

```yaml
id: deliverorderworkflow
name: Deliver Order Workflow
version: '1.0.0'
specVersion: '0.8'
start: Dispatch Courier
states:
- name: Dispatch Courier
  type: operation
  actions:
  - functionRef: Dispatch Courrier Function
  transition: Wait for Order Pickup
- name: Wait for Order Pickup
  type: event
  onEvents:
  - eventRefs:
    - Order Picked Up Event
    eventDataFilter:
      data: "${ .data.status }"
      toStateData: "${ .status }"
    actions:
    - functionRef: Deliver Order Function
  transition: Wait for Delivery Confirmation
- name: Wait for Delivery Confirmation
  type: event
  onEvents:
  - eventRefs:
    - Order Delievered Event
    eventDataFilter:
      data: "${ .data.status }"
      toStateData: "${ .status }"
  end: true
```

#### Workflow Results

For the example order event, the workflow output for a successful completion would look like for example:

```json
{
  "orderid": "ORDER-12345",
  "orderstatus": [
    "Order Submitted",
    "Order ETA Received",
    "Order Picked up",
    "Order Delievered",
    "Order Charged",
    "Order Completed"
  ]
}
```

### Continuing as a new Execution

#### Description

Some runtime implementations on which we run our workflows can have different quotas, such as maximum execution durations, maximum consumed events, etc. We can use the Serverless workflow "continueAs" functionality that can be used to stop the current workflow execution and start another one (of the same or a different type). This is very useful in cases where we have to ensure we don't reach the imposed quotas of single workflow execution.

This example assumes that the runtime we are using has a quota set to a maximum of one thousand consumed events per single workflow execution. 
Our sample workflow consumes a single customer event at a time and invokes the `emailCustomer` function. 
Note that we do not set a workflow `workflowExecTimeout`, so we intend to have a long-running workflow. However, because of the runtime restriction, in this case, we would run into the event consume limit, and our workflow would have to terminate. We can fix this problem by using [`continueAs`](../specification.md#Continuing-as-a-new-Execution), which will allow us to make sure that we reach the given limit and then continue our workflow execution as a new run.

We assume that our workflow input has the runtime-imposed quota:

```json
{
  "quota": {
   "maxConsumedEvents": 1000
  }
}
```

#### Workflow Diagram

<p align="center">
<img src="../media/examples/example-continueas.png" height="400px" alt="ContinueAs Example"/>
</p>

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "notifycustomerworkflow",
    "name": "notifycustomerworkflow",
    "description": "Notify Customer",
    "version": "1.0.0",
    "specVersion": "0.8",
    "start": "wait-for-customer-event",
    "states": [
        {
            "name": "wait-for-customer-event",
            "type": "event",
            "onEvents": [
                {
                    "eventRefs": [
                        "customer-event"
                    ],
                    "eventDataFilter": {
                        "data": "${ .customerId }",
                        "toStateData": "${ .eventCustomerId }"
                    },
                    "actions": [
                        {
                            "name": "notify-customer-function",
                            "functionRef": {
                                "refName": "notify-customer-function",
                                "arguments": {
                                    "customerId": "${ .eventCustomerId }"
                                }
                            }
                        }
                    ]
                }
            ],
            "stateDataFilter": {
                "output": "${ .count = .count + 1 }"
            },
            "transition": "check-event-quota"
        },
        {
            "name": "check-event-quota",
            "type": "switch",
            "dataConditions": [
                {
                    "name": "ready",
                    "condition": "${ try(.customerCount) != null and .customerCount > .quota.maxConsumedEvents }",
                    "end": {
                        "continueAs": {
                            "workflowId": "notifycustomerworkflow",
                            "version": "1.0.0",
                            "data": "${ del(.customerCount) }"
                        }
                    }
                }
            ],
            "defaultCondition": {
                "transition": "wait-for-customer-event"
            }
        }
    ],
    "events": [
        {
            "name": "customer-event",
            "type": "org.events.customerEvent",
            "source": "customerSource"
        }
    ],
    "functions": [
        {
            "name": "notify-customer-function",
            "operation": "http://myapis.org/customerapis.json#notifyCustomer"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: notifycustomerworkflow
name: notifycustomerworkflow
description: Notify Customer
version: 1.0.0
specVersion: "0.8"
start: wait-for-customer-event
states:
  - name: wait-for-customer-event
    type: event
    onEvents:
      - eventRefs:
          - customer-event
        eventDataFilter:
          data: ${ .customerId }
          toStateData: ${ .eventCustomerId }
        actions:
          - name: notify-customer-function
            functionRef:
              refName: notify-customer-function
              arguments:
                customerId: ${ .eventCustomerId }
    stateDataFilter:
      output: ${ .count = .count + 1 }
    transition: check-event-quota
  - name: check-event-quota
    type: switch
    dataConditions:
      - name: ready
        condition: ${ try(.customerCount) != null and .customerCount >
          .quota.maxConsumedEvents }
        end:
          continueAs:
            workflowId: notifycustomerworkflow
            version: 1.0.0
            data: ${ del(.customerCount) }
    defaultCondition:
      transition: wait-for-customer-event
events:
  - name: customer-event
    type: org.events.customerEvent
    source: customerSource
functions:
  - name: notify-customer-function
    operation: http://myapis.org/customerapis.json#notifyCustomer
```

</td>
</tr>
</table>

### Process Transactions

#### Description

This example shows how we can loop through a data input array (in parallel), and decide which action to perform
depending on the value of each element in the input array. 
We use the [action definition](../specification.md#Action-Definition) `condition` property to perform the action that 
is best suited for the transaction value.
Note that in this example we set the "large transaction amount" as a [workflow constant](../specification.md#Workflow-Constants). 
There are other ways to set 
this value, for example passing it as [workflow data input](../specification.md#Workflow-Data-Input), 
or if this data is sensitive, to use [workflow secrets](../specification.md#Workflow-Secrets). 

For the example, we assume the following workflow data input:

```json
{
  "customer": {
   "id": "abc123",
   "name": "John Doe",
   "transactions": [1000, 400, 60, 7000, 12000, 250]
  }
}
```

We use the [ForeEach workflow state](../specification.md#ForEach-State) to iterate through customer transactions (in parallel), and 
decide which activity to perform based on the transaction value.

#### Workflow Definition

<table>
<tr>
    <th>JSON</th>
    <th>YAML</th>
</tr>
<tr>
<td valign="top">

```json
{
    "id": "customerbankingtransactions",
    "name": "customerbankingtransactions",
    "description": "Customer Banking Transactions Workflow",
    "version": "1.0.0",
    "specVersion": "0.8",
    "autoRetries": true,
    "constants": {
        "largetxamount": 5000
    },
    "states": [
        {
            "name": "process-transactions",
            "type": "foreach",
            "inputCollection": "${ .customer.transactions }",
            "iterationParam": "${ .tx }",
            "actions": [
                {
                    "name": "process-larger-transaction",
                    "functionRef": "banking-service-larger-tx",
                    "condition": "${ .tx >= $CONST.largetxamount }"
                },
                {
                    "name": "process-smaller-transaction",
                    "functionRef": "banking-service-smaller-tx",
                    "condition": "${ .tx < $CONST.largetxamount }"
                }
            ],
            "end": true
        }
    ],
    "functions": [
        {
            "name": "banking-service-larger-tx",
            "type": "asyncapi",
            "operation": "banking.yaml#largerTransation"
        },
        {
            "name": "banking-service-smaller-tx",
            "type": "asyncapi",
            "operation": "banking.yaml#smallerTransation"
        }
    ]
}```

</td>
<td valign="top">

```yaml
id: customerbankingtransactions
name: customerbankingtransactions
description: Customer Banking Transactions Workflow
version: 1.0.0
specVersion: "0.8"
autoRetries: true
constants:
  largetxamount: 5000
states:
  - name: process-transactions
    type: foreach
    inputCollection: ${ .customer.transactions }
    iterationParam: ${ .tx }
    actions:
      - name: process-larger-transaction
        functionRef: banking-service-larger-tx
        condition: ${ .tx >= $CONST.largetxamount }
      - name: process-smaller-transaction
        functionRef: banking-service-smaller-tx
        condition: ${ .tx < $CONST.largetxamount }
    end: true
functions:
  - name: banking-service-larger-tx
    type: asyncapi
    operation: banking.yaml#largerTransation
  - name: banking-service-smaller-tx
    type: asyncapi
    operation: banking.yaml#smallerTransation
```

</td>
</tr>
</table>
