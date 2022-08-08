# Specification Roadmap

_Note: Items in tables for each milestone do not imply an order of implementation._

_Note: Milestone entries include the most notable updates only. For list of all commits see [link](https://github.com/cncf/wg-serverless/commits/master)_

_Status description:_

| Completed | In Progress | In Planning | On Hold |
| :--: | :--: |  :--: | :--: |
| ✔ | ✏️ | 🚩 | ❗️|

## Releases

- [Roadmap for next planned release](#v09)
- [v0.8 released Nov 2021](#v08)
- [v0.7 released Aug 2021](#v07)
- [v0.6 released March 2021](#v06)
- [v0.5 released November 2020](#v05)
- [v0.1 released April 2020](#v01)

## <a name="v09"></a> Next planned release

| Status | Description | Comments |
| --- | --- |  --- |
| ✔️| Fix support for workflow extensions | [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md)  |
| ✔️| Fix state execution timeout | [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md)  |
| ✔️| Update rules of retries increment and multiplier properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md)  |
| ✔️| Add clarification on mutually exclusive properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md)  |
| ✔️| Make the `resultEventRef` attribute in `EventRef` definition not required [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md#EventRef-Definition)  |
| ✔️| Make the `stateName` attribute in `start` definition not required [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md#EventRef-Definition)  |
| ✔️| Remove `id` attribute from `actions` and `states`. Now, the names from both attributes must be unique within the workflow definition  | [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md#transitions)
| ✔️| Update eventRef props to`produceEventRef` and `consumeEventRef` [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md#EventRef-Definition)  |
| ✔️| Update eventRef props to`resultEventTimeout` and `consumeEventTimeout` [spec doc](https://github.com/serverlessworkflow/specification/blob/main/specification.md#EventRef-Definition)  |
| ✔️| Apply fixes to auth spec schema [workflow schema](https://github.com/serverlessworkflow/specification/tree/main/schema)  |
| ✔️| Update the `dataInputSchema` top-level property by supporting the assignment of a JSON schema object [workflow schema](https://github.com/serverlessworkflow/specification/tree/main/specification.md#workflow-definition-structure)  |
| ✔️| Add the new `WORKFLOW` reserved keyword to workflow expressions  |
| ✔️| Add the new `http` function type [spec doc](https://github.com/serverlessworkflow/specification/tree/main/specification.md#using-functions-for-http-service-invocations) |
| ✏️️| Add inline state defs in branches |   |
| ✏️️| Add "completedBy" functionality |   |
| ✏️️| Define workflow context |   |
| ✏️️| Start work on TCK  |   |
| ✏️️| Add integration with open-source runtimes  |   |
| ✏️️| Add SDKs for more languages (Python, PHP, Rust, etc) |   |
| ✏️️| Add more samples  |   |

## <a name="v08"></a> Released version v0.8

| Status | Description | Comments |
| --- | --- |  --- |
| ✔️| Support custom function `type` definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| Workflow "name" no longer a required property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| Workflow "start" no longer a required property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| ForEach state "iterationParam" no longer a required property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| Added "useData" for eventDataFilter, and "useResults" for actionDataFilter | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| Added "resultEventTimeout" for action eventref | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️| Added example for "continueAs" | [examples doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/examples/README.md)  |
| ✔️️| Support for async action invocation | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |
| ✔️️| Support for action condition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.8.x/specification.md)  |


## <a name="v07"></a> Released version v0.7

| Status | Description | Comments |
| --- | --- |  --- |
| ✔️| Add workflow `key` and `annotations` properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Replaced SubFlow state with subflow action type | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Add workflow `dataInputSchema` property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Rename switch state `default` to `defaultCondition` to avoid keyword conflicts for SDK's | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Add description of additional properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Rename Parallel `completionType` values | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Removed `workflowId` from ParallelState and ForEach states (use subFlow action instead) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Add subflow actions `version` property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Renamed `schemaVersion` to `specVersion` and it is now a required parameter | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Add GraphQL support for function definitions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added "dataOnly" property to Event Definitions (allow event data filters to access entire event) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added support for Secrets and Constants | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Changed default value of execution timeout `interrupt` property. This is a non-backwards compatible changes. | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Updated workflow timeouts | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added Workflow Auth definitions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added State execution timeouts | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Temporarily removed `waitForCompletion` for subflows | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added function definition support for OData | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added function definition support for AsyncAPI | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Rename Delay state to Sleep state | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added 'sleep' property to action definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added Rate Limiting extension | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Update ForEach state - adding sequential exec option and batch size for parallel option | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Update to error handling and retries. Retries are now per action rather than per state. Added option of automatic retries for actions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |
| ✔️| Added "continueAs" property to end definitions | [spec doc](hhttps://github.com/serverlessworkflow/specification/blob/0.7.x/specification.md) |

## <a name="v06"></a> Released version v0.6

| Status | Description | Comments |
| --- | --- |  --- |
| ✔️| Adding Workflow Compensation capabilities (cmp [Compensating Transaction](https://docs.microsoft.com/en-us/azure/architecture/patterns/compensating-transaction), [SAGA pattern](https://microservices.io/patterns/data/saga.html)) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Adding comparison examples with Google Cloud Workflow language| [comparisons doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/comparisons/README.md) |
| ✔️| Updates to retry functionality | [retries: exponential backoff & max backoff](https://github.com/serverlessworkflow/specification/issues/137) [retries: max-attempts & interval](https://github.com/serverlessworkflow/specification/issues/136)|
| ✔️| Update "directInvoke" property type | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Data schema input/output update | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Updating start and end state definitions| [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Update cron definition (adding validUntil parameter)| [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Adding comparison examples with Temporal | [comparison doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/comparisons/README.md) |
| ✔️| Simplified functionRef and transition properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Adding comparison examples with Cadence | [comparison doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/comparisons/README.md) |
| ✔️| Adding workflow execTimeout and keepActive properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Adding SubFlow state repeat (loop) ability | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Adding comparison examples with BPMN | [comparison doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/comparisons/README.md) |
| ✔️| Adding RPC type to function definitions (gRPC) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Change function definition 'parameters' to 'arguments' | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Replace JsonPath with jq | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Update start definition (move to top-level worklow param) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Updated schedule definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |
| ✔️| Update data filters | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) |

## <a name="v05"></a> Released version v0.5

| Status | Description | Comments |
| --- | --- |  --- |
| ✔ | Update Switch State | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔ | Rename Relay to Inject state | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update waitForCompletion property of Parallel State | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add timeout property to actions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add examples comparing Argo workflow and spec markups | [examples doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/examples/examples-argo.md) |
| ✔️| Add ability to produce events during state transitions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add event-based condition capabilities to Switch State | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add examples comparing Brigade workflow and spec markups | [examples doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/examples/examples-brigade.md) |
| ✔️| Update produceEvent data property | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Change uppercase property and enum types to lowercase | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add Parallel State Exception Handling section | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add Go SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-go) |
| ✔️| Add Java SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-java) |
| ✔️| Allow to define events as produced or consumed | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add "triggered" start definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update scheduled start definition - adding cron def | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add ability to reference trigger and result events in actions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Expand event correlation capabilities | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Only use JsonPath expressions ( remove need for expression languages other than JsonPath) | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update workflow extensions | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add Workflow KPIs extension | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Add Workflow Validation to Java SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-java) |
| ✔️| Update Switch state conditions and default definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update transitions and end definition 'produceEvents' definition | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Events definition update - add convenience way to define multiple events that share properties | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update to function and events definitions - allow inline array def as well as uri reference to external resource | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Enforce use of OpenAPI specification in function definitions for portability | [spec doc](https://github.com/serverlessworkflow/specification/blob/0.5.x/specification.md) |
| ✔️| Update workflow Error Handling | [spec doc](../specification.md) |

## <a name="v01"></a> Released version v0.1

| Status | Description | Comments |
| :--: | --- |  --- |
| ✔ | Establish governance, contributing guidelines and initial stakeholder | [governance doc](https://github.com/cncf/wg-serverless/tree/v0.1/workflow/spec/governance)  |
| ✔ | Define specification goals | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Define specification functional scope | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Include set of use-cases for Serverless Workflow | [usecases doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/usecases.md) |
| ✔ | Include set of examples for Serverless Workflow | [examples doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/examples.md) |
| ✔ | Define specification JSON Schema | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add SubFlow state | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add Relay state | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add ForEach state | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Update Event state| [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Define Workflow data input/output | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Update state data filtering | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Clearly define workflow info passing | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add Workflow error handling | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add reusable function definitions | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add support for YAML definitions | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Update workflow end definition | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add Callback state | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔ | Add workflow metadata | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔️| Update workflow start definition | [spec doc](https://github.com/cncf/wg-serverless/blob/v0.1/workflow/spec/spec.md) |
| ✔️| Prepare github branch and docs for v0.1 | [branch](https://github.com/cncf/wg-serverless/tree/v0.1/workflow/spec) |
