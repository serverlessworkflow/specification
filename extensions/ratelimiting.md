# Extensions - Rate Limiting

## Table of Contents

- [Introduction](#Introduction)
- [Extension Definition](#Extension-Definition)
    - [Workflow KPIs Definition](#Workflow-KPIs-Definition)
- [Example](#Example)

## Introduction

Out workflows can execute numerous downstream services. Rate limiting can be used to protect these 
downstream services from flooding. In addition, rate limiting can help us keep our cost 
at a desired rate in cases where downstream service invocations have an associated cost factor.

## Extension Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| extensionid | Unique extension Id (default is 'workflow-kpi-extension') | string | yes |
| workflowid | Workflow definition unique identifier (workflow id property) | string | yes |
| workflowVersions | Workflow versions. If not defined, applies to all workflow instances (regardless of their associated workflow version) | array | no |
| [singleInstance](#Single-Instance-Definition) | Rate limits per single workflow instance | object | no |
| [allInstances](#All-Instances-Definition) | Rate limits per all workflow instances | object | no |

### Single Instance Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| maxActionsPerSecond | Sets the rate limiting on number of actions that can be executed per second. Notice that the number is represented as number type, so that you can set it to less than 1 if needed. For example, set the number to 0.1 means you want workflow actions should be executed once every 10 seconds. Default zero value means 'unlimited'| number | no |
| maxConcurrentActions | Maximum number of actions that can be executed in parallel | string | no |
| maxProducedEventsPerSecond |Sets the rate limiting on number of events that can be produced per second. Notice that the number is represented as number type, so that you can set it to less than 1 if needed. For example, set the number to 0.1 means workflow can produce events once every 10 seconds. Default zero value means 'unlimited' | string | no |
| maxStates | Maximum number of workflow states that should be executed. Default is zero, meaning unlimited. | string | no |
| maxTransitions | Maximum number of workflow transitions that should be executed. Default is zero, meaning unlimited. | string | no |

### All Instances Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| maxActionsPerSecond | Sets the rate limiting on number of actions that can be executed per second. Notice that the number is represented as number type, so that you can set it to less than 1 if needed. For example, set the number to 0.1 means you want workflow actions should be executed once every 10 seconds. Default zero value means 'unlimited'| number | no |
| maxConcurrentActions | Maximum number of actions that can be executed in parallel | string | no |
| maxProducedEventsPerSecond |Sets the rate limiting on number of events that can be produced per second. Notice that the number is represented as number type, so that you can set it to less than 1 if needed. For example, set the number to 0.1 means workflow can produce events once every 10 seconds. Default zero value means 'unlimited' | string | no |
| maxStates | Maximum number of workflow states that should be executed. Default is zero, meaning unlimited. | string | no |
| maxTransitions | Maximum number of workflow transitions that should be executed. Default is zero, meaning unlimited. | string | no |

## Example

The following example shows a workflow definition on the left and
an associated sample Rate Limiting extension definition on the right.

<table>
<tr>
    <th>Workflow</th>
    <th>Rate Limiting Extension</th>
</tr>
<tr>
<td valign="top">

```yaml
id: processapplication
name: Process Application
version: '1.0'
specVersion: '0.8'
start: ProcessNewApplication
states:
  - name: ProcessNewApplication
    type: event
    onEvents:
      - eventRefs:
          - ApplicationReceivedEvent
        actions:
          - functionRef: processApplicationFunction
          - functionRef: acceptApplicantFunction
          - functionRef: depositFeesFunction
    end:
      produceEvents:
        - eventRef: NotifyApplicantEvent
functions:
  - name: processApplicationFunction
    operation: file://myservice.json#process
  - name: acceptApplicantFunction
    operation: file://myservice.json#accept
  - name: depositFeesFunction
    operation: file://myservice.json#deposit
events:
  - name: ApplicationReceivedEvent
    type: application
    source: "/applications/new"
  - name: NotifyApplicantEvent
    type: notifications
    source: "/applicants/notify"
```

</td>
<td valign="top">

```yaml
extensionid: workflow-ratelimiting-extension
workflowid: processapplication
singleInstance:
  maxActionsPerSecond: 0.1
  maxConcurrentActions: 200
  maxProducedEventsPerSecond: 2
  maxStates: '1000'
  maxTransitions: '1000'
allInstances:
  maxActionsPerSecond: 1
  maxConcurrentActions: 500
  maxProducedEventsPerSecond: 20
  maxStates: '10000'
  maxTransitions: '10000'

```

</td>
</tr>
</table>