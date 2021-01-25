# Examples

Provides Serverless Workflow language examples

## Table of Contents

- [Hello World](#Hello-World-Example)
- [Greeting](#Greeting-Example)
- [Event-based greeting (Event State)](#Event-Based-Greeting-Example)
- [Solving Math Problems (ForEach state)](#Solving-Math-Problems-Example)
- [Parallel Execution](#Parallel-Execution-Example)
- [Event Based Transitions (Event-based Switch)](#Event-Based-Transitions-Example)
- [Applicant Request Decision (Data-based Switch + SubFlow states)](#Applicant-Request-Decision-Example)
- [Provision Orders (Error Handling)](#Provision-Orders-Example)
- [Monitor Job for completion (Polling)](#Monitor-Job-Example)
- [Send CloudEvent on Workflow Completion](#Send-CloudEvent-On-Workfow-Completion-Example)
- [Monitor Patient Vital Signs (Event state)](#Monitor-Patient-Vital-Signs-Example)
- [Finalize College Application (Event state)](#Finalize-College-Application-Example)
- [Perform Customer Credit Check (Callback state)](#Perform-Customer-Credit-Check-Example)
- [Handle Car Auction Bids (Scheduled start Event state)](#Handle-Car-Auction-Bids-Example)
- [Check Inbox Periodically (Cron-based Workflow start)](#Check-Inbox-Periodically)
- [Event-based service invocation (Event triggered actions)](#Event-Based-Service-Invocation)
- [Reusing Function and Event Definitions](#Reusing-Function-And-Event-Definitions)
- [New Patient Onboarding (Error checking and Retries)](#New-Patient-Onboarding)

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
"version": "1.0",
"name": "Hello World Workflow",
"description": "Inject Hello World",
"states":[  
  {  
     "name":"Hello State",
     "type":"inject",
     "start": true,
     "data": {
        "result": "Hello World!"
     },
     "end": true
  }
]
}
```

</td>
<td valign="top">

```yaml
id: helloworld
version: '1.0'
name: Hello World Workflow
description: Inject Hello World
states:
- name: Hello State
  type: inject
  start: true
  data:
    result: Hello World!
  end: true
```

</td>
</tr>
</table>

### Greeting Example

#### Description

This example shows a single Operation state with one action that calls the "greeting" function.
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
"version": "1.0",
"name": "Greeting Workflow",
"description": "Greet Someone",
"functions": [
  {
     "name": "greetingFunction",
     "operation": "file://myapis/greetingapis.json#greeting"
  }
],
"states":[  
  {  
     "name":"Greet",
     "type":"operation",
     "start": true,
     "actions":[  
        {  
           "functionRef": {
              "refName": "greetingFunction",
              "parameters": {
                "name": "{{ $.person.name }}"
              }
           },
           "actionDataFilter": {
              "dataResultsPath": "{{ $.greeting }}"
           }
        }
     ],
     "end": true
  }
]
}
```

</td>
<td valign="top">

```yaml
id: greeting
version: '1.0'
name: Greeting Workflow
description: Greet Someone
functions:
- name: greetingFunction
  operation: file://myapis/greetingapis.json#greeting
states:
- name: Greet
  type: operation
  start: true
  actions:
  - functionRef:
      refName: greetingFunction
      parameters:
        name: "{{ $.person.name }}"
    actionDataFilter:
      dataResultsPath: "{{ $.greeting }}"
  end: true
```

</td>
</tr>
</table>

### Event Based Greeting Example

#### Description

This example shows a single Event state with one action that calls the "greeting" function.
The event state consumes cloud events of type "greetingEventType". When an even with this type
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
    "dataOutputPath": "{{ $.data.greet }} "
  }
}
```

which is triggered when the greeting event is consumed. It extracts its "data.greet" of the event and
merges it with the states data.

The second, a state data filter, which is defined on the event state itself:

```json
{
  "stateDataFilter": {
     "dataOutputPath": "{{ $.payload.greeting }}"
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
"version": "1.0",
"name": "Event Based Greeting Workflow",
"description": "Event Based Greeting",
"events": [
 {
  "name": "GreetingEvent",
  "type": "greetingEventType",
  "source": "greetingEventSource"
 }
],
"functions": [
  {
     "name": "greetingFunction",
     "operation": "file://myapis/greetingapis.json#greeting"
  }
],
"states":[  
  {  
     "name":"Greet",
     "type":"event",
     "start": true,
     "onEvents": [{
         "eventRefs": ["GreetingEvent"],
         "eventDataFilter": {
            "dataOutputPath": "{{ $.data.greet }}"
         },
         "actions":[  
            {  
               "functionRef": {
                  "refName": "greetingFunction",
                  "parameters": {
                    "name": "{{ $.greet.name }}"
                  }
               }
            }
         ]
     }],
     "stateDataFilter": {
        "dataOutputPath": "{{ $.payload.greeting }}"
     },
     "end": true
  }
]
}
```

</td>
<td valign="top">

```yaml
id: eventbasedgreeting
version: '1.0'
name: Event Based Greeting Workflow
description: Event Based Greeting
events:
- name: GreetingEvent
  type: greetingEventType
  source: greetingEventSource
functions:
- name: greetingFunction
  operation: file://myapis/greetingapis.json#greeting
states:
- name: Greet
  type: event
  start: true
  onEvents:
  - eventRefs:
    - GreetingEvent
    eventDataFilter:
      dataOutputPath: "{{ $.data.greet }}"
    actions:
    - functionRef:
        refName: greetingFunction
        parameters:
          name: "{{ $.greet.name }}"
  stateDataFilter:
    dataOutputPath: "{{ $.payload.greeting }}"
  end: true
```

</td>
</tr>
</table>

### Solving Math Problems Example

#### Description

In this example we show how to iterate over data using the ForEach state.
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
"version": "1.0",
"name": "Solve Math Problems Workflow",
"description": "Solve math problems",
"functions": [
{
  "name": "solveMathExpressionFunction",
  "operation": "http://myapis.org/mapthapis.json#solveExpression"
}
],
"states":[  
{
 "name":"Solve",
 "start": true,
 "type":"foreach",
 "inputCollection": "{{ $.expressions }}",
 "iterationParam": "singleexpression",
 "outputCollection": "{{ $.results }}",
 "actions":[  
   {  
      "functionRef": {
         "refName": "solveMathExpressionFunction",
         "parameters": {
           "expression": "{{ $.singleexpression }}"
         }
      }
   }
 ],
 "stateDataFilter": {
    "dataOutputPath": "{{ $.results }}"
 },
 "end": true
}
]
}
```

</td>
<td valign="top">

```yaml
id: solvemathproblems
version: '1.0'
name: Solve Math Problems Workflow
description: Solve math problems
functions:
- name: solveMathExpressionFunction
  operation: http://myapis.org/mapthapis.json#solveExpression
states:
- name: Solve
  start: true
  type: foreach
  inputCollection: "{{ $.expressions }}"
  iterationParam: singleexpression
  outputCollection: "{{ $.results }}"
  actions:
  - functionRef:
      refName: solveMathExpressionFunction
      parameters:
        expression: "{{ $.singleexpression }}"
  stateDataFilter:
    dataOutputPath: "{{ $.results }}"
  end: true
```

</td>
</tr>
</table>

### Parallel Execution Example

#### Description

This example uses a parallel state to execute two branches (simple wait states) at the same time.
The completionType type is set to "and", which means the parallel state has to wait for both branches
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
"version": "1.0",
"name": "Parallel Execution Workflow",
"description": "Executes two branches in parallel",
"states":[  
  {  
     "name": "ParallelExec",
     "type": "parallel",
     "start": true,
     "completionType": "and",
     "branches": [
        {
          "name": "ShortDelayBranch",
          "workflowId": "shortdelayworkflowid"
        },
        {
          "name": "LongDelayBranch",
          "workflowId": "longdelayworkflowid"
        }
     ],
     "end": true
  }
]
}
```

</td>
<td valign="top">

```yaml
id: parallelexec
version: '1.0'
name: Parallel Execution Workflow
description: Executes two branches in parallel
states:
- name: ParallelExec
  type: parallel
  start: true
  completionType: and
  branches:
  - name: ShortDelayBranch
    workflowId: shortdelayworkflowid
  - name: LongDelayBranch
    workflowId: longdelayworkflowid
  end: true
```

</td>
</tr>
</table>

We assume that the two referenced workflows, namely `shortdelayworkflowid` and `longdelayworkflowid` both include a single delay state,
with the `shortdelayworkflowid` workflow delay state defining its `timeDelay` property to be shorter than that of the `longdelayworkflowid` workflows
delay state.

### Event Based Transitions Example

#### Description

In this example we use an Event-based Switch state to wait for arrival
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
"id": "eventbasedswitch",
"version": "1.0",
"name": "Event Based Switch Transitions",
"description": "Event Based Switch Transitions",
"events": [
{
    "name": "visaApprovedEvent",
    "type": "VisaApproved",
    "source": "visaCheckSource"
},
{
    "name": "visaRejectedEvent",
    "type": "VisaRejected",
    "source": "visaCheckSource"
}
],
"states":[  
  {  
     "name":"CheckVisaStatus",
     "type":"switch",
     "start": true,
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
  },
  {
    "name": "HandleApprovedVisa",
    "type": "subflow",
    "workflowId": "handleApprovedVisaWorkflowID",
    "end": true
  },
  {
      "name": "HandleRejectedVisa",
      "type": "subflow",
      "workflowId": "handleRejectedVisaWorkflowID",
      "end": true
  },
  {
      "name": "HandleNoVisaDecision",
      "type": "subflow",
      "workflowId": "handleNoVisaDecisionWorkfowId",
      "end": true
  }
]
}
```

</td>
<td valign="top">

```yaml
id: eventbasedswitch
version: '1.0'
name: Event Based Switch Transitions
description: Event Based Switch Transitions
events:
- name: visaApprovedEvent
  type: VisaApproved
  source: visaCheckSource
- name: visaRejectedEvent
  type: VisaRejected
  source: visaCheckSource
states:
- name: CheckVisaStatus
  type: switch
  start: true
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
- name: HandleApprovedVisa
  type: subflow
  workflowId: handleApprovedVisaWorkflowID
  end: true
- name: HandleRejectedVisa
  type: subflow
  workflowId: handleRejectedVisaWorkflowID
  end: true
- name: HandleNoVisaDecision
  type: subflow
  workflowId: handleNoVisaDecisionWorkfowId
  end: true
```

</td>
</tr>
</table>

### Applicant Request Decision Example

#### Description

This example shows off the switch state and the subflow state. The workflow is started with application information data as input:

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
If the applicants age is over 18 we start the application (subflow state). Otherwise the workflow notifies the
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
   "version": "1.0",
   "name": "Applicant Request Decision Workflow",
   "description": "Determine if applicant request is valid",
   "functions": [
     {
        "name": "sendRejectionEmailFunction",
        "operation": "http://myapis.org/applicationapi.json#emailRejection"
     }
   ],
   "states":[  
      {  
         "name":"CheckApplication",
         "type":"switch",
         "start": true,
         "dataConditions": [
            {
              "condition": "{{ $.applicants[?(@.age >= 18)] }}",
              "transition": {
                "nextState": "StartApplication"
              }
            },
            {
              "condition": "{{ $.applicants[?(@.age < 18)] }}",
              "transition": {
                "nextState": "RejectApplication"
              }
            }
         ],
         "default": {
            "transition": {
               "nextState": "RejectApplication"
            }
         }
      },
      {
        "name": "StartApplication",
        "type": "subflow",
        "workflowId": "startApplicationWorkflowId",
        "end": true
      },
      {  
        "name":"RejectApplication",
        "type":"operation",
        "actionMode":"sequential",
        "actions":[  
           {  
              "functionRef": {
                 "refName": "sendRejectionEmailFunction",
                 "parameters": {
                   "applicant": "{{ $.applicant }}"
                 }
              }
           }
        ],
        "end": true
    }
   ]
}
```

</td>
<td valign="top">

```yaml
id: applicantrequest
version: '1.0'
name: Applicant Request Decision Workflow
description: Determine if applicant request is valid
functions:
- name: sendRejectionEmailFunction
  operation: http://myapis.org/applicationapi.json#emailRejection
states:
- name: CheckApplication
  type: switch
  start: true
  dataConditions:
  - condition: "{{ $.applicants[?(@.age >= 18)] }}"
    transition:
      nextState: StartApplication
  - condition: "{{ $.applicants[?(@.age < 18)] }}"
    transition:
      nextState: RejectApplication
  default:
    transition:
      nextState: RejectApplication
- name: StartApplication
  type: subflow
  workflowId: startApplicationWorkflowId
  end: true
- name: RejectApplication
  type: operation
  actionMode: sequential
  actions:
  - functionRef:
      refName: sendRejectionEmailFunction
      parameters:
        applicant: "{{ $.applicant }}"
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
"version": "1.0",
"name": "Provision Orders",
"description": "Provision Orders and handle errors thrown",
"functions": [
  {
     "name": "provisionOrderFunction",
     "operation": "http://myapis.org/provisioningapi.json#doProvision"
  }
],
"states":[  
  {  
    "name":"ProvisionOrder",
    "type":"operation",
    "start": true,
    "actionMode":"sequential",
    "actions":[  
       {  
          "functionRef": {
             "refName": "provisionOrderFunction",
             "parameters": {
               "order": "{{ $.order }}"
             }
          }
       }
    ],
    "stateDataFilter": {
       "dataOutputPath": "{{ $.exceptions }}"
    },
    "transition": {
       "nextState":"ApplyOrder"
    },
    "onErrors": [
       {
         "error": "Missing order id",
         "transition": {
           "nextState": "MissingId"
         }
       },
       {
         "error": "Missing order item",
         "transition": {
           "nextState": "MissingItem"
         }
       },
       {
        "error": "Missing order quantity",
        "transition": {
          "nextState": "MissingQuantity"
        }
       }
    ]
},
{
   "name": "MissingId",
   "type": "subflow",
   "workflowId": "handleMissingIdExceptionWorkflow",
   "end": true
},
{
   "name": "MissingItem",
   "type": "subflow",
   "workflowId": "handleMissingItemExceptionWorkflow",
   "end": true
},
{
   "name": "MissingQuantity",
   "type": "subflow",
   "workflowId": "handleMissingQuantityExceptionWorkflow",
   "end": true
},
{
   "name": "ApplyOrder",
   "type": "subflow",
   "workflowId": "applyOrderWorkflowId",
   "end": true
}
]
}
```

</td>
<td valign="top">

```yaml
id: provisionorders
version: '1.0'
name: Provision Orders
description: Provision Orders and handle errors thrown
functions:
- name: provisionOrderFunction
  operation: http://myapis.org/provisioningapi.json#doProvision
states:
- name: ProvisionOrder
  type: operation
  start: true
  actionMode: sequential
  actions:
  - functionRef:
      refName: provisionOrderFunction
      parameters:
        order: "{{ $.order }}"
  stateDataFilter:
    dataOutputPath: "{{ $.exceptions }}"
  transition:
    nextState: ApplyOrder
  onErrors:
  - error: Missing order id
    transition:
      nextState: MissingId
  - error: Missing order item
    transition:
      nextState: MissingItem
  - error: Missing order quantity
    transition:
      nextState: MissingQuantity
- name: MissingId
  type: subflow
  workflowId: handleMissingIdExceptionWorkflow
  end: true
- name: MissingItem
  type: subflow
  workflowId: handleMissingItemExceptionWorkflow
  end: true
- name: MissingQuantity
  type: subflow
  workflowId: handleMissingQuantityExceptionWorkflow
  end: true
- name: ApplyOrder
  type: subflow
  workflowId: applyOrderWorkflowId
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

In the case job submission raises a runtime error, we transition to a SubFlow state which handles the job submission issue.

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
  "version": "1.0",
  "name": "Job Monitoring",
  "description": "Monitor finished execution of a submitted job",
  "functions": [
    {
      "name": "submitJob",
      "operation": "http://myapis.org/monitorapi.json#doSubmit"
    },
    {
      "name": "checkJobStatus",
      "operation": "http://myapis.org/monitorapi.json#checkStatus"
    },
    {
      "name": "reportJobSuceeded",
      "operation": "http://myapis.org/monitorapi.json#reportSucceeded"
    },
    {
      "name": "reportJobFailed",
      "operation": "http://myapis.org/monitorapi.json#reportFailure"
    }
  ],
  "states":[  
    {  
      "name":"SubmitJob",
      "type":"operation",
      "start": true,
      "actionMode":"sequential",
      "actions":[  
      {  
          "functionRef": {
            "refName": "submitJob",
            "parameters": {
              "name": "{{ $.job.name }}"
            }
          },
          "actionDataFilter": {
            "dataResultsPath": "{{ $.jobuid }}"
          }
      }
      ],
      "onErrors": [
      {
        "error": "*",
        "transition": {
          "nextState": "SubmitError"
        }
      }
      ],
      "stateDataFilter": {
          "dataOutputPath": "{{ $.jobuid }}"
      },
      "transition": {
          "nextState":"WaitForCompletion"
      }
  },
  {
      "name": "SubmitError",
      "type": "subflow",
      "workflowId": "handleJobSubmissionErrorWorkflow",
      "end": true
  },
  {
      "name": "WaitForCompletion",
      "type": "delay",
      "timeDelay": "PT5S",
      "transition": {
        "nextState":"GetJobStatus"
      }
  },
  {  
      "name":"GetJobStatus",
      "type":"operation",
      "actionMode":"sequential",
      "actions":[  
      {  
        "functionRef": {
            "refName": "checkJobStatus",
            "parameters": {
              "name": "{{ $.jobuid }}"
            }
          },
          "actionDataFilter": {
          "dataResultsPath": "{{ $.jobstatus }}"
          }
      }
      ],
      "stateDataFilter": {
          "dataOutputPath": "{{ $.jobstatus }}"
      },
      "transition": {
          "nextState":"DetermineCompletion"
      }
  },
  {  
    "name":"DetermineCompletion",
    "type":"switch",
    "dataConditions": [
      {
        "condition": "{{ $[?(@.jobstatus == 'SUCCEEDED')] }}",
        "transition": {
          "nextState": "JobSucceeded"
        }
      },
      {
        "condition": "{{ $[?(@.jobstatus == 'FAILED')] }}",
        "transition": {
          "nextState": "JobFailed"
        }
      }
    ],
    "default": {
      "transition": {
         "nextState": "WaitForCompletion"
       }
    }
  },
  {  
      "name":"JobSucceeded",
      "type":"operation",
      "actionMode":"sequential",
      "actions":[  
      {  
        "functionRef": {
            "refName": "reportJobSuceeded",
            "parameters": {
              "name": "{{ $.jobuid }}"
            }
        }
      }
      ],
      "end": true
  },
  {  
    "name":"JobFailed",
    "type":"operation",
    "actionMode":"sequential",
    "actions":[  
    {  
        "functionRef": {
          "refName": "reportJobFailed",
          "parameters": {
            "name": "{{ $.jobuid }}"
          }
        }
    }
    ],
    "end": true
  }
  ]
}
```

</td>
<td valign="top">

```yaml
id: jobmonitoring
version: '1.0'
name: Job Monitoring
description: Monitor finished execution of a submitted job
functions:
- name: submitJob
  operation: http://myapis.org/monitorapi.json#doSubmit
- name: checkJobStatus
  operation: http://myapis.org/monitorapi.json#checkStatus
- name: reportJobSuceeded
  operation: http://myapis.org/monitorapi.json#reportSucceeded
- name: reportJobFailed
  operation: http://myapis.org/monitorapi.json#reportFailure
states:
- name: SubmitJob
  type: operation
  start: true
  actionMode: sequential
  actions:
  - functionRef:
      refName: submitJob
      parameters:
        name: "{{ $.job.name }}"
    actionDataFilter:
      dataResultsPath: "{{ $.jobuid }}"
  onErrors:
  - error: "*"
    transition:
      nextState: SubmitError
  stateDataFilter:
    dataOutputPath: "{{ $.jobuid }}"
  transition:
    nextState: WaitForCompletion
- name: SubmitError
  type: subflow
  workflowId: handleJobSubmissionErrorWorkflow
  end: true
- name: WaitForCompletion
  type: delay
  timeDelay: PT5S
  transition:
    nextState: GetJobStatus
- name: GetJobStatus
  type: operation
  actionMode: sequential
  actions:
  - functionRef:
      refName: checkJobStatus
      parameters:
        name: "{{ $.jobuid }}"
    actionDataFilter:
      dataResultsPath: "{{ $.jobstatus }}"
  stateDataFilter:
    dataOutputPath: "{{ $.jobstatus }}"
  transition:
    nextState: DetermineCompletion
- name: DetermineCompletion
  type: switch
  dataConditions:
  - condition: "{{ $[?(@.jobstatus == 'SUCCEEDED')] }}"
    transition:
      nextState: JobSucceeded
  - condition: "{{ $[?(@.jobstatus == 'FAILED')] }}"
    transition:
      nextState: JobFailed
  default:
    transition:
      nextState: WaitForCompletion
- name: JobSucceeded
  type: operation
  actionMode: sequential
  actions:
  - functionRef:
      refName: reportJobSuceeded
      parameters:
        name: "{{ $.jobuid }}"
  end: true
- name: JobFailed
  type: operation
  actionMode: sequential
  actions:
  - functionRef:
      refName: reportJobFailed
      parameters:
        name: "{{ $.jobuid }}"
  end: true
```

</td>
</tr>
</table>

### Send CloudEvent On Workfow Completion Example

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
"version": "1.0",
"name": "Send CloudEvent on provision completion",
"events": [
{
    "name": "provisioningCompleteEvent",
    "type": "provisionCompleteType",
    "kind": "produced"
}
],
"functions": [
{
    "name": "provisionOrderFunction",
    "operation": "http://myapis.org/provisioning.json#doProvision"
}
],
"states": [
{
    "name": "ProvisionOrdersState",
    "type": "foreach",
    "start": true,
    "inputCollection": "{{ $.orders }}",
    "iterationParam": "singleorder",
    "outputCollection": "{{ $.provisionedOrders }}",
    "actions": [
        {
            "functionRef": {
                "refName": "provisionOrderFunction",
                "parameters": {
                    "order": "{{ $.singleorder }}"
                }
            }
        }
    ],
    "end": {
        "produceEvents": [{
            "eventRef": "provisioningCompleteEvent",
            "data": "{{ $.provisionedOrders }}"
        }]
    }
}
]
}
```

</td>
<td valign="top">

```yaml
id: sendcloudeventonprovision
version: '1.0'
name: Send CloudEvent on provision completion
events:
- name: provisioningCompleteEvent
  type: provisionCompleteType
  kind: produced
functions:
- name: provisionOrderFunction
  operation: http://myapis.org/provisioning.json#doProvision
states:
- name: ProvisionOrdersState
  type: foreach
  start: true
  inputCollection: "{{ $.orders }}"
  iterationParam: singleorder
  outputCollection: "{{ $.provisionedOrders }}"
  actions:
  - functionRef:
      refName: provisionOrderFunction
      parameters:
        order: "{{ $.singleorder }}"
  end:
    produceEvents:
    - eventRef: provisioningCompleteEvent
      data: "{{ $.provisionedOrders }}"
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
"name": "Monitor Patient Vitals",
"version": "1.0",
"events": [
{
    "name": "HighBodyTemperature",
    "type": "org.monitor.highBodyTemp",
    "source": "monitoringSource",
    "correlation": [
      { 
        "contextAttributeName": "patientId"
      } 
    ]
},
{
    "name": "HighBloodPressure",
    "type": "org.monitor.highBloodPressure",
    "source": "monitoringSource",
    "correlation": [
      { 
        "contextAttributeName": "patientId"
      } 
    ]
},
{
    "name": "HighRespirationRate",
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
    "name": "callPulmonologist",
    "operation": "http://myapis.org/patientapis.json#callPulmonologist"
},
{
    "name": "sendTylenolOrder",
    "operation": "http://myapis.org/patientapis.json#tylenolOrder"
},
{
    "name": "callNurse",
    "operation": "http://myapis.org/patientapis.json#callNurse"
}
],
"states": [
{
"name": "MonitorVitals",
"type": "event",
"start": true,
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
    "terminate": true
}
}]
}
```

</td>
<td valign="top">

```yaml
id: patientVitalsWorkflow
name: Monitor Patient Vitals
version: '1.0'
events:
- name: HighBodyTemperature
  type: org.monitor.highBodyTemp
  source: monitoringSource
  correlation:
  - contextAttributeName: patientId
- name: HighBloodPressure
  type: org.monitor.highBloodPressure
  source: monitoringSource
  correlation:
  - contextAttributeName: patientId
- name: HighRespirationRate
  type: org.monitor.highRespirationRate
  source: monitoringSource
  correlation:
  - contextAttributeName: patientId
functions:
- name: callPulmonologist
  operation: http://myapis.org/patientapis.json#callPulmonologist
- name: sendTylenolOrder
  operation: http://myapis.org/patientapis.json#tylenolOrder
- name: callNurse
  operation: http://myapis.org/patientapis.json#callNurse
states:
- name: MonitorVitals
  type: event
  start: true
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
"id": "finalizeCollegeApplication",
"name": "Finalize College Application",
"version": "1.0",
"events": [
{
    "name": "ApplicationSubmitted",
    "type": "org.application.submitted",
    "source": "applicationsource",
    "correlation": [
    { 
      "contextAttributeName": "applicantId"
    } 
   ]
},
{
    "name": "SATScoresReceived",
    "type": "org.application.satscores",
    "source": "applicationsource",
    "correlation": [
      { 
      "contextAttributeName": "applicantId"
      } 
    ]
},
{
    "name": "RecommendationLetterReceived",
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
    "name": "finalizeApplicationFunction",
    "operation": "http://myapis.org/collegeapplicationapi.json#finalize"
}
],
"states": [
{
    "name": "FinalizeApplication",
    "type": "event",
    "start": true,
    "exclusive": false,
    "onEvents": [
        {
            "eventRefs": [
                "ApplicationSubmitted",
                "SATScoresReceived",
                "RecommendationLetterReceived"
            ],
            "actions": [
                {
                    "functionRef": {
                        "refName": "finalizeApplicationFunction",
                        "parameters": {
                            "student": "{{ $.applicantId }}"
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
}
```

</td>
<td valign="top">

```yaml
id: finalizeCollegeApplication
name: Finalize College Application
version: '1.0'
events:
- name: ApplicationSubmitted
  type: org.application.submitted
  source: applicationsource
  correlation:
  - contextAttributeName: applicantId
- name: SATScoresReceived
  type: org.application.satscores
  source: applicationsource
  correlation:
  - contextAttributeName: applicantId
- name: RecommendationLetterReceived
  type: org.application.recommendationLetter
  source: applicationsource
  correlation:
  - contextAttributeName: applicantId
functions:
- name: finalizeApplicationFunction
  operation: http://myapis.org/collegeapplicationapi.json#finalize
states:
- name: FinalizeApplication
  type: event
  start: true
  exclusive: false
  onEvents:
  - eventRefs:
    - ApplicationSubmitted
    - SATScoresReceived
    - RecommendationLetterReceived
    actions:
    - functionRef:
        refName: finalizeApplicationFunction
        parameters:
          student: "{{ $.applicantId }}"
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
    "version": "1.0",
    "name": "Customer Credit Check Workflow",
    "description": "Perform Customer Credit Check",
    "functions": [
        {
            "name": "creditCheckFunction",
            "operation": "http://myapis.org/creditcheckapi.json#doCreditCheck"
        },
        {
            "name": "sendRejectionEmailFunction",
            "operation": "http://myapis.org/creditcheckapi.json#rejectionEmail"
        }
    ],
    "events": [
        {
            "name": "CreditCheckCompletedEvent",
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
            "name": "CheckCredit",
            "type": "callback",
            "start": true,
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
        },
        {
            "name": "EvaluateDecision",
            "type": "switch",
            "dataConditions": [
                {
                    "condition": "{{ $.creditCheck[?(@.decision == 'Approved')] }}",
                    "transition": {
                        "nextState": "StartApplication"
                    }
                },
                {
                    "condition": "{{ $.creditCheck[?(@.decision == 'Denied')] }}",
                    "transition": {
                        "nextState": "RejectApplication"
                    }
                }
            ],
            "default": {
               "transition": {
                 "nextState": "RejectApplication"
                }
            }
        },
        {
            "name": "StartApplication",
            "type": "subflow",
            "workflowId": "startApplicationWorkflowId",
            "end": true
        },
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
            "end": true
        }
    ]
}
```

</td>
<td valign="top">

```yaml
id: customercreditcheck
version: '1.0'
name: Customer Credit Check Workflow
description: Perform Customer Credit Check
functions:
- name: creditCheckFunction
  operation: http://myapis.org/creditcheckapi.json#doCreditCheck
- name: sendRejectionEmailFunction
  operation: http://myapis.org/creditcheckapi.json#rejectionEmail
events:
- name: CreditCheckCompletedEvent
  type: creditCheckCompleteType
  source: creditCheckSource
  correlation:
  - contextAttributeName: customerId
states:
- name: CheckCredit
  type: callback
  start: true
  action:
    functionRef:
      refName: callCreditCheckMicroservice
      parameters:
        customer: "{{ $.customer }}"
  eventRef: CreditCheckCompletedEvent
  timeout: PT15M
  transition:
    nextState: EvaluateDecision
- name: EvaluateDecision
  type: switch
  dataConditions:
  - condition: "{{ $.creditCheck[?(@.decision == 'Approved')] }}"
    transition:
      nextState: StartApplication
  - condition: "{{ $.creditCheck[?(@.decision == 'Denied')] }}"
    transition:
      nextState: RejectApplication
  default:
    transition:
      nextState: RejectApplication
- name: StartApplication
  type: subflow
  workflowId: startApplicationWorkflowId
  end: true
- name: RejectApplication
  type: operation
  actionMode: sequential
  actions:
  - functionRef:
      refName: sendRejectionEmailFunction
      parameters:
        applicant: "{{ $.customer }}"
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
    "id": "handleCarAuctionBid",
    "version": "1.0",
    "name": "Car Auction Bidding Workflow",
    "description": "Store a single bid whole the car auction is active",
    "functions": [
        {
            "name": "StoreBidFunction",
            "operation": "http://myapis.org/carauctionapi.json#storeBid"
        }
    ],
    "events": [
        {
            "name": "CarBidEvent",
            "type": "carBidMadeType",
            "source": "carBidEventSource"
        }
    ],
    "states": [
        {
          "name": "StoreCarAuctionBid",
          "type": "event",
          "start": {
              "schedule": {
                "interval": "2020-03-20T09:00:00Z/2020-03-20T15:00:00Z"
              }
          },
          "exclusive": true,
          "onEvents": [
            {
                "eventRefs": ["CarBidEvent"],
                "actions": [{
                    "functionRef": {
                        "refName": "StoreBidFunction",
                        "parameters": {
                            "bid": "{{ $.bid }}"
                        }
                    }
                }]
            }
          ],
          "end": true
        }
    ]
}
```

</td>
<td valign="top">

```yaml
id: handleCarAuctionBid
version: '1.0'
name: Car Auction Bidding Workflow
description: Store a single bid whole the car auction is active
functions:
- name: StoreBidFunction
  operation: http://myapis.org/carauctionapi.json#storeBid
events:
- name: CarBidEvent
  type: carBidMadeType
  source: carBidEventSource
states:
- name: StoreCarAuctionBid
  type: event
  start:
    schedule:
      interval: 2020-03-20T09:00:00Z/2020-03-20T15:00:00Z
  exclusive: true
  onEvents:
  - eventRefs:
    - CarBidEvent
    actions:
    - functionRef:
        refName: StoreBidFunction
        parameters:
          bid: "{{ $.bid }}"
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
"id": "checkInbox",
"name": "Check Inbox Workflow",
"description": "Periodically Check Inbox",
"version": "1.0",
"functions": [
    {
        "name": "checkInboxFunction",
        "operation": "http://myapis.org/inboxapi.json#checkNewMessages"
    },
    {
        "name": "sendTextFunction",
        "operation": "http://myapis.org/inboxapi.json#sendText"
    }
],
"states": [
    {
        "name": "CheckInbox",
        "type": "operation",
        "start": {
            "schedule": {
                "cron": {
                   "expression": "0 0/15 * * * ?"
                }
            }
        },
        "actionMode": "sequential",
        "actions": [
            {
                "functionRef": {
                    "refName": "checkInboxFunction"
                }
            }
        ],
        "transition": {
            "nextState": "SendTextForHighPriority"
        }
    },
    {
        "name": "SendTextForHighPriority",
        "type": "foreach",
        "inputCollection": "{{ $.messages }}",
        "iterationParam": "singlemessage",
        "actions": [
            {
                "functionRef": {
                    "refName": "sendTextFunction",
                    "parameters": {
                        "message": "{{ $.singlemessage }}"
                    }
                }
            }
        ],
        "end": true
    }
]
}
```

</td>
<td valign="top">

```yaml
id: checkInbox
name: Check Inbox Workflow
description: Periodically Check Inbox
version: '1.0'
functions:
- name: checkInboxFunction
  operation: http://myapis.org/inboxapi.json#checkNewMessages
- name: sendTextFunction
  operation: http://myapis.org/inboxapi.json#sendText
states:
- name: CheckInbox
  type: operation
  start:
    schedule:
      cron:
        expression: 0 0/15 * * * ?
  actionMode: sequential
  actions:
  - functionRef:
      refName: checkInboxFunction
  transition:
    nextState: SendTextForHighPriority
- name: SendTextForHighPriority
  type: foreach
  inputCollection: "{{ $.messages }}"
  iterationParam: singlemessage
  actions:
  - functionRef:
      refName: sendTextFunction
      parameters:
        message: "{{ $.singlemessage }}"
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
    "id": "VetAppointmentWorkflow",
    "name": "Vet Appointment Workflow",
    "description": "Vet service call via events",
    "version": "1.0",
    "events": [
        {
            "name": "MakeVetAppointment",
            "source": "VetServiceSoure",
            "kind": "produced"
        },
        {
            "name": "VetAppointmentInfo",
            "source": "VetServiceSource",
            "kind": "consumed"
        }
    ],
    "states": [
        {
            "name": "MakeVetAppointmentState",
            "type": "operation",
            "start": true,
            "actions": [
                {
                    "name": "MakeAppointmentAction",
                    "eventRef": {
                       "triggerEventRef": "MakeVetAppointment",
                       "data": "{{ $.patientInfo }}",
                       "resultEventRef":  "VetAppointmentInfo"
                    },
                    "actionDataFilter": {
                        "dataResultsPath": "{{ $.appointmentInfo }}"
                    },
                    "timeout": "PT15M"
                }
            ],
            "end": true
        }
    ]
}
```

</td>
<td valign="top">

```yaml
id: VetAppointmentWorkflow
name: Vet Appointment Workflow
description: Vet service call via events
version: '1.0'
events:
- name: MakeVetAppointment
  source: VetServiceSoure
  kind: produced
- name: VetAppointmentInfo
  source: VetServiceSource
  kind: consumed
states:
- name: MakeVetAppointmentState
  type: operation
  start: true
  actions:
  - name: MakeAppointmentAction
    eventRef:
      triggerEventRef: MakeVetAppointment
      data: "{{ $.patientInfo }}"
      resultEventRef: VetAppointmentInfo
    actionDataFilter:
      dataResultsPath: "{{ $.appointmentInfo }}"
    timeout: PT15M
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
  "version": "1.0",
  "name": "Payment Confirmation Workflow",
  "description": "Performs Payment Confirmation",
  "functions": "functiondefs.json",
  "events": "eventdefs.yml",
  "states": [
    {
      "name": "PaymentReceived",
      "type": "event",
      "onEvents": [
        {
          "eventRefs": [
            "PaymentReceivedEvent"
          ],
          "actions": [
            {
              "name": "checkfunds",
              "functionRef": {
                "refName": "checkFundsAvailability",
                "parameters": {
                  "account": "{{ $.accountId }}",
                  "paymentamount": "{{ $.payment.amount }}"
                }
              }
            }
          ]
        }
      ],
      "transition": {
        "nextState": "ConfirmBasedOnFunds"
      }
    },
    {
      "name": "ConfirmBasedOnFunds",
      "type": "switch",
      "dataConditions": [
        {
          "condition": "{{ $.funds[?(@.available == 'true')] }}",
          "transition": {
            "nextState": "SendPaymentSuccess"
          }
        },
        {
          "condition": "{{ $.funds[?(@.available == 'false')] }}",
          "transition": {
            "nextState": "SendInsufficientResults"
          }
        }
      ],
      "default": {
        "transition": {
          "nextState": "SendPaymentSuccess"
        }
      }
    },
    {
      "name": "SendPaymentSuccess",
      "type": "operation",
      "actions": [
        {
          "functionRef": {
            "refName": "sendSuccessEmail",
            "parameters": {
              "applicant": "{{ $.customer }}"
            }
          }
        }
      ],
      "end": {
        "produceEvents": [
          {
            "eventRef": "ConfirmationCompletedEvent",
            "data": "{{ $.payment }}"
          }
        ]
      }
    },
    {
      "name": "SendInsufficientResults",
      "type": "operation",
      "actions": [
        {
          "functionRef": {
            "refName": "sendInsufficientFundsEmail",
            "parameters": {
              "applicant": "{{ $.customer }}"
            }
          }
        }
      ],
      "end": {
        "produceEvents": [
          {
            "eventRef": "ConfirmationCompletedEvent",
            "data": "{{ $.payment }}"
          }
        ]
      }
    }
  ]
}
```

</td>
<td valign="top">

```yaml
id: paymentconfirmation
version: '1.0'
name: Payment Confirmation Workflow
description: Performs Payment Confirmation
functions: functiondefs.json
events: eventdefs.yml
states:
- name: PaymentReceived
  type: event
  onEvents:
  - eventRefs:
    - PaymentReceivedEvent
    actions:
    - name: checkfunds
      functionRef:
        refName: checkFundsAvailability
        parameters:
          account: "{{ $.accountId }}"
          paymentamount: "{{ $.payment.amount }}"
  transition:
    nextState: ConfirmBasedOnFunds
- name: ConfirmBasedOnFunds
  type: switch
  dataConditions:
  - condition: "{{ $.funds[?(@.available == 'true')] }}"
    transition:
      nextState: SendPaymentSuccess
  - condition: "{{ $.funds[?(@.available == 'false')] }}"
    transition:
      nextState: SendInsufficientResults
  default:
    transition:
      nextState: SendPaymentSuccess
- name: SendPaymentSuccess
  type: operation
  actions:
  - functionRef:
      refName: sendSuccessEmail
      parameters:
        applicant: "{{ $.customer }}"
  end:
    produceEvents:
    - eventRef: ConfirmationCompletedEvent
      data: "{{ $.payment }}"
- name: SendInsufficientResults
  type: operation
  actions:
  - functionRef:
      refName: sendInsufficientFundsEmail
      parameters:
        applicant: "{{ $.customer }}"
  end:
    produceEvents:
    - eventRef: ConfirmationCompletedEvent
      data: "{{ $.payment }}"
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
  "name": "Patient Onboarding Workflow",
  "version": "1.0",
  "states": [
    {
      "name": "Onboard",
      "type": "event",
      "start": true,
      "onEvents": [
        {
          "eventRefs": [
            "NewPatientEvent"
          ],
          "actions": [
            {
              "functionRef": {
                "refName": "StorePatient"
              }
            },
            {
              "functionRef": {
                "refName": "AssignDoctor"
              }
            },
            {
              "functionRef": {
                "refName": "ScheduleAppt"
              }
            }
          ]
        }
      ],
      "onErrors": [
        {
          "error": "ServiceNotAvailable",
          "code": "503",
          "retryRef": "ServicesNotAvailableRetryStrategy",
          "end": true
        }
      ],
      "end": true
    }
  ],
  "events": [
    {
      "name": "StorePatient",
      "type": "new.patients.event",
      "source": "newpatient/+"
    }
  ],
  "functions": [
    {
      "name": "StoreNewPatientInfo",
      "operation": "api/services.json#addPatient"
    },
    {
      "name": "AssignDoctor",
      "operation": "api/services.json#assignDoctor"
    },
    {
      "name": "ScheduleAppt",
      "operation": "api/services.json#scheduleAppointment"
    }
  ],
  "retries": [
    {
      "name": "ServicesNotAvailableRetryStrategy",
      "delay": "PT3S",
      "maxAttempts": 10
    }
  ]
}
```

</td>
<td valign="top">

```yaml
id: patientonboarding
name: Patient Onboarding Workflow
version: '1.0'
states:
- name: Onboard
  type: event
  start: true
  onEvents:
  - eventRefs:
    - NewPatientEvent
    actions:
    - functionRef:
        refName: StorePatient
    - functionRef:
        refName: AssignDoctor
    - functionRef:
        refName: ScheduleAppt
  onErrors:
  - error: ServiceNotAvailable
    code: '503'
    retryRef: ServicesNotAvailableRetryStrategy
    end: true
  end: true
events:
- name: StorePatient
  type: new.patients.event
  source: newpatient/+
functions:
- name: StoreNewPatientInfo
  operation: api/services.json#addPatient
- name: AssignDoctor
  operation: api/services.json#assignDoctor
- name: ScheduleAppt
  operation: api/services.json#scheduleAppointment
retries:
- name: ServicesNotAvailableRetryStrategy
  delay: PT3S
  maxAttempts: 10
```

</td>
</tr>
</table>

#### Workflow Demo

This example is used in our Serverless Workflow Hands-on series videos [#1](https://www.youtube.com/watch?v=0gmpuGLP-_o)
and [#2](https://www.youtube.com/watch?v=6A6OYp5nygg).
