# Serverless Workflow Roadmap

_Note: Items in tables for each milestone do not imply an order of implementation._

_Note: Milestone entries include the most notable updates only. For list of all commits see [link](https://github.com/cncf/wg-serverless/commits/master)_

_Status description:_

| Completed | In Progress | In Planning | On Hold |
| :--: | :--: |  :--: | :--: |
| ✔ | ✏️ | 🚩 | ❗️|

## Releases

- [v0.1 released April 2020](#v01)
- [v0.5 released November 2020](#v05)

## <a name="v01"></a> v0.1

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

## <a name="v05"></a> v0.5

| Status | Description | Comments |
| --- | --- |  --- |
| ✔ | Update Switch State | [spec doc](../specification.md) |
| ✔ | Rename Relay to Inject state | [spec doc](../specification.md) |
| ✔️| Update waitForCompletion property of Parallel State | [spec doc](../specification.md) |
| ✔️| Add timeout property to actions | [spec doc](../specification.md) |
| ✔️| Add examples comparing Argo workflow and spec markups | [examples doc](../examples/examples-argo.md) |
| ✔️| Add ability to produce events during state transitions | [spec doc](../specification.md) |
| ✔️| Add event-based condition capabilities to Switch State | [spec doc](../specification.md) |
| ✔️| Add examples comparing Brigade workflow and spec markups | [examples doc](../examples/examples-brigade.md) |
| ✔️| Update produceEvent data property | [spec doc](../specification.md) |
| ✔️| Change uppercase property and enum types to lowercase | [spec doc](../specification.md) |
| ✔️| Add Parallel State Exception Handling section | [spec doc](../specification.md) |
| ✔️| Add Go SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-go) |
| ✔️| Add Java SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-java) |
| ✔️| Allow to define events as produced or consumed | [spec doc](../specification.md) |
| ✔️| Add "triggered" start definition | [spec doc](../specification.md) |
| ✔️| Update scheduled start definition - adding cron def | [spec doc](../specification.md) |
| ✔️| Add ability to reference trigger and result events in actions | [spec doc](../specification.md) |
| ✔️| Expand event correlation capabilities | [spec doc](../specification.md) |
| ✔️| Only use JsonPath expressions ( remove need for expression languages other than JsonPath) | [spec doc](../specification.md) |
| ✔️| Update workflow extensions | [spec doc](../specification.md) |
| ✔️| Add Workflow KPIs extension | [spec doc](../specification.md) |
| ✔️| Add Workflow Validation to Java SDK | [sdk repo](https://github.com/serverlessworkflow/sdk-java) |
| ✔️| Update Switch state conditions and default definition | [spec doc](../specification.md) |
| ✔️| Update transitions and end definition 'produceEvents' definition | [spec doc](../specification.md) |
| ✔️| Events definition update - add convenience way to define multiple events that share properties | [spec doc](../specification.md) |
| ✔️| Update to function and events definitions - allow inline array def as well as uri reference to external resource | [spec doc](../specification.md) |
| ✔️| Enforce use of OpenAPI specification in function definitions for portability | [spec doc](../specification.md) |
| ✔️| Update workflow Error Handling | [spec doc](../specification.md) |
