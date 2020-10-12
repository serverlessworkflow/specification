# Extensions - KPI

## Table of Contents

- [Introduction](#Introduction)
- [Extension Definition](#Extension-Definition)
  - [Workflow KPIs Definition](#Workflow-KPIs-Definition)
  - [Event KPIs Definition](#Event-KPIs-Definition)
  - [Function KPIs Definition](#Function-KPIs-Definition)
  - [State KPIs Definition](#State-KPIs-Definition)
  - [Thresholds Definition](#Thresholds-Definition)
- [Example](#Example)

## Introduction

Key performance indicators (KPIs) are an important metric for analyzing statistical data of workflows.

KPIs can be used to
* Show workflow efficiencies and inefficiencies.
* Help improve specific workflow activities in terms of defined criteria (performance, cost, etc)
* Help demonstrate the overall workflow effectiveness against defined criteria (performance, cost, etc)
* Help show progress towards intended workflow objectives

The KPI extension allows you to define `expected` key performance indicators to the workflow model it references.
KPIs can be added for the model:
* [Workflow definition](../specification.md#Workflow-Definition)
* [Function (services) definition](../specification.md#Function-Definition)
* [Event definitions](../specification.md#Event-Definition)
* [State definitions](../specification.md#State-Definition)

## Extension Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| extensionid | Unique extension Id (default is 'workflow-kpi-extension') | string | yes |
| workflowid | Workflow definition unique identifier (workflow id property) | string | yes |
| [workflow](#Workflow-KPIs-Definition) | Workflow definition KPIs | object | no |
| [events](#Event-KPIs-Definition) | Workflow event definitions KPIs | array | no |
| [functions](#Function-KPIs-Definition) | Workflow function definitions KPIs | array | no |
| [states](#State-KPIs-Definition) | Workflow states definitions KPIs | array | no |

#### Workflow KPIs Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| [per](#Thresholds-Definition) | Define the kpi thresholds in terms of time and/or num of workflow instances| object | yes |
| workflowid | Workflow definition unique identifier (workflow id property) | string | yes |
| maxInvoked | Max number of workflow invocations | string | no |
| minInvoked | Min number of workflow invocations | string | no |
| avgInvoked | Average number of workflow invocations | string | no |
| maxDuration | ISO 8601. Max duration of workflow execution | string | no |
| minDuration | ISO 8601. Min duration of workflow execution | string | no |
| avgDuration | ISO 8601. Average duration of workflow execution | string | no |
| maxCost | Max workflow execution cost | string | no |
| minCost | Min workflow execution cost | string | no |
| avgCost | Average workflow execution cost | string | no |

#### Event KPIs Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| for | References an unique event name in the defined workflow events | string | yes |
| [per](#Thresholds-Definition) | Define the kpi thresholds in terms of time and/or num of workflow instances| object | yes |
| maxConsumed | If the referenced event kind is 'consumed', the max amount of times this event is consumed | string | yes |
| minConsumed | If the referenced event kind is 'consumed', the min amount of times this event is consumed | string | no |
| avgConsumed | If the referenced event kind is 'consumed', the average amount of times this event is consumed | string | no |
| maxProduced | If the referenced event kind is 'produced', the max amount of times this event is produced | string | no |
| minProduced | If the referenced event kind is 'produced', the min amount of times this event is produced | string | no |
| avgProduced | If the referenced event kind is 'produced', the average amount of times this event is produced | string | no |

#### Function KPIs Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| for | References an unique function name in the defined workflow functions | string | yes |
| [per](#Thresholds-Definition) | Define the kpi thresholds in terms of time and/or num of workflow instances| object | yes |
| maxErrors | Max number of errors during function invocation | string | yes |
| maxRetry | Max number of retries done for this function invocation | string | no |
| maxTimeout | Max number of times the function timeout time was reached| string | no |
| maxInvoked | Max number of invocations for the referenced function | string | no |
| minInvoked | Min number of invocations for the referenced function | string | no |
| avgInvoked | Average number of invocations for the referenced function | string | no |
| maxCost | Max function execution cost | string | no |
| minCost | Min function execution cost | string | no |
| avgCost | Average function execution cost | string | no |

#### State KPIs Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| for | References an unique state name in the defined workflow states | string | yes |
| [per](#Thresholds-Definition) | Define the kpi thresholds in terms of time and/or num of workflow instances| object | yes |
| maxTimeout | Max number of times the function timeout time was reached| string | no |
| maxExec | Max executions of the referenced state | string | no |
| minExec | Min executions of the referenced state | string | no |
| avgExec | Average executions of the referenced state | string | no |
| maxDuration | ISO 8601. Max duration of state execution | string | no |
| minDuration | ISO 8601. Min duration of state execution | string | no |
| avgDuration | ISO 8601. Average duration of state execution | string | no |
| maxCost | Max state execution cost | string | no |
| minCost | Min state execution cost | string | no |
| avgCost | Average state execution cost | string | no |

#### Thresholds Definition

| Parameter | Description | Type | Required |
| --- | --- | --- | --- |
| time | ISO_8601 time. Threshhold time. Default is 1 day | string | no |
| instances | Threshold number of workflow instances | integer | no |

## Example

The following example shows a workflow definition on the left and 
an associated sample KPIs extension definition on the right.

<table>
<tr>
    <th>Workflow</th>
    <th>KPIs Extension</th>
</tr>
<tr>
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
  operation: http://myapi.org/patientapi.json#callPulmonologist
- name: sendTylenolOrder
  operation: http://myapi.org/patientapi.json#sendTylenol
- name: callNurse
  operation: http://myapi.org/patientapi.json#callNurse
states:
- name: MonitorVitals
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
<td valign="top">

```yaml
extensionid: workflow-kpi-extension
workflowid: patientVitalsWorkflow
currency: USD
workflow:
  per:
    time: PT1D
  maxCost: '1300'
  maxInvoked: '500'
  minInvoked: '100'
events:
- for: HighBodyTemperature
  per:
    time: PT1D
  avgConsumed: '50'
- for: HighBloodPressure
  per:
    time: PT1D
  avgConsumed: '30'
functions:
- for: callPulmonologist
  per:
    instances: 1000
  maxCost: '400'
  maxErrors: '5'
  maxRetry: '10'
  maxTimeout: '15'
  avgInvoked: '40'
- for: sendTylenolOrder
  per:
    instances: 1000
  maxCost: '200'
  maxErrors: '5'
  maxRetry: '10'
  maxTimeout: '15'
  avgInvoked: '400'
states:
- for: MonitorVitals
  per:
    time: PT1D
  maxCost: '300'
  maxExec: '1000'
  minExec: '50'
```

</td>
</tr>
</table>