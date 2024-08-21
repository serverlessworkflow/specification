# Serverless Workflow DSL

## Table of Contents

- [Abstract](#abstract)
- [Motivation](#motivation)
- [Priority of Constituencies](#priority-of-constituencies)
- [Design](#design)
- [Concepts](#concepts)
  + [Workflow](#workflow)
    - [Status Phases](#status-phases)
    - [Components](#components)
      + [Task](#task)
    - [Scheduling](#scheduling)
      + [Event-Driven Scheduling](#event-driven-scheduling)
  + [Task Flow](#task-flow)
  + [Data Flow](#data-flow)
  + [Runtime Expressions](#runtime-expressions)
    - [Arguments](#runtime-expression-arguments)
  + [Fault Tolerance](#fault-tolerance)
  + [Timeouts](#timeouts)
  + [Interoperability](#interoperability)
    - [Supported Protocols](#supported-protocols)
    - [Custom and Non-Standard Interactions](#custom-and-non-standard-interactions)
      + [Creating a Custom Function](#creating-a-custom-function)
      + [Using a Custom Function](#using-a-custom-function)
      + [Publishing a Custom Function](#publishing-a-custom-function)
  + [Events](#events)
  + [Extensions](#extensions)
  + [External Resources](#external-resources)
  + [Authentication](#authentication)

## Abstract

This document proposes the creation of a Domain Specific Language (DSL) called Serverless Workflow, tailored for building platform agnostic workflows. 

Serverless Workflow aims to simplify the orchestration of complex processes across diverse environments, providing developers with a unified syntax and set of tools for defining and executing serverless workflows.

## Motivation

Serverless computing has gained popularity for its ability to abstract away infrastructure management tasks, enabling developers to focus on application logic. However, orchestrating serverless workflows across multiple environments often involves dealing with disparate tools and platforms, leading to complexity and inefficiency.

Serverless Workflow addresses this challenge by providing a DSL specifically designed for serverless workflow orchestration. By abstracting away the underlying infrastructure complexities and offering a modular and extensible framework, Serverless Workflow aims to streamline the development, deployment, and management of serverless workflows.

## Priority of Constituencies

Inspired by the [Priority of Constituencies](https://www.w3.org/TR/2024/NOTE-design-principles-20240718/#priority-of-constituencies) principle from the W3C Design Principles, the Serverless Workflow DSL prioritizes the following constituencies (collectively referred to as "users"):

- Authors: people authoring and reading workflows
- Operators: people running and operating a runtime implementation of the specification
- Implementors: people implementing a specification compliant runtime
- Specifications writers: people working on the specifications of Serverless Workflow

If a trade-off needs to be made, always put author's needs above all.

Similarly, when beginning to design an API, be sure to understand and document the user needs that the API aims to address.

Author needs come before the needs of operators, which come before the needs of runtime implementors, which come before the needs of specification writers, which come before theoretical purity.

Like all principles, this isn’t absolute. Ease of operations affects the perceived reliability of authors' workflows. Implementors have to prioritize finite engineering resources, which affects how features reach authors. Specification writers also have finite resources, and theoretical concerns reflect the underlying needs of all of these groups.

## Design

The Serverless Workflow DSL is crafted with a design philosophy that prioritizes clarity, expressiveness, and ease of use. Its foundation lies in linguistic fluency, emphasizing readability and comprehension. By adopting a fluent style, the DSL promotes intuitive understanding through natural language constructs. Verbs are employed in the imperative tense to denote actions, enhancing clarity and directness in expressing workflow logic. This imperative approach empowers developers to articulate their intentions succinctly and effectively.

The DSL also embraces the principle of implicit default behaviors, sparing authors from unnecessary repetition and enhancing the conciseness of workflow definitions. For instance, default settings alleviate the burden of explicitly defining every detail, streamlining the workflow design process. Furthermore, the DSL allows both inline declaration of components or the creation of reusable elements, granting flexibility in workflow composition. This flexibility allows developers to seamlessly integrate inline task definitions without imposing rigid structural requirements.

Moreover, the DSL eschews strong-typed enumerations wherever feasible, fostering extensibility and adaptability across different runtime environments. While maintaining portability is crucial, the DSL prioritizes customization options for extensions and runtimes, enabling tailored implementations to suit diverse use cases. Additionally, the DSL favors universally understood terms over technical jargon, enhancing accessibility and comprehension for a broader audience.

- Embrace linguistic fluency for enhanced readability and understanding.
- Utilize imperative verbs to convey actions directly and clearly.
- Employ implicit default behaviors to reduce redundancy and streamline workflow definitions.
- Enable the declaration and effortless import of shared components by supporting external references
- Encourage the declaration of components inline for situations where reusability is unnecessary, prioritizing ease of use in such cases.
- Prioritize flexibility over strong-typed enumerations for enhanced extensibility.
- Opt for universally understood terms to improve accessibility and comprehension.

## Concepts

### Workflow

A Serverless Workflow is a sequence of specific [tasks](dsl-reference.md#tasks) that are executed in a defined order. By default, this order follows the declaration sequence within the workflow definition. Workflows are designed to automate processes and orchestrate various serverless functions and services. 

Workflows can be triggered in different ways: upon request, scheduled using CRON expressions, or initiated upon correlation with specific events. 

Additionally, workflows may optionally accept inputs and produce outputs, allowing for data processing and transformation within the workflow execution.

#### Status Phases

Workflows in the Serverless Workflow DSL can exist in several phases, each indicating the current state of the workflow execution. These phases include:

| Phase |	Description |
| --- | --- |
| `pending` |	The workflow has been initiated and is pending execution. |
| `running` |	The workflow is currently in progress. |
| `waiting` |	The workflow execution has been paused or halted temporarily and is waiting for something to happen. |
| `cancelled` |	The workflow execution has been terminated before completion. |
| `faulted` |	The workflow execution has encountered an error. |
| `completed` |	The workflow execution has successfully finished all tasks. |

Additionally, the flow of execution within a workflow can be controlled using [directives*](dsl-reference.md#flow-directive), which provide instructions to the workflow engine on how to manage and handle specific aspects of workflow execution.

\**To learn more about flow directives and how they can be utilized to control the execution and behavior of workflows, please refer to [Flow Directives](dsl-reference.md#flow-directive).*

#### Components

Serverless Workflow DSL allows for defining reusable components that can be referenced across the workflow. These include:

- [Authentications](dsl-reference.md#authentication)
- [Errors](dsl-reference.md#error)
- [Extensions](dsl-reference.md#extension)
- [Functions](dsl-reference.md#task)
- [Retries](dsl-reference.md#retry)
- [Secrets](#secret)

##### Task

[Tasks](dsl-reference.md#tasks) are the fundamental computing units of a workflow. They define the different types of actions that a workflow can perform, including the ability to mutate their input and output data. Tasks can also write to and modify the context data, enabling complex and dynamic workflow behaviors.

The Serverless Workflow DSL defines several default [task](dsl-reference.md#tasks) types that runtimes **must** implement:

- [Call](dsl-reference.md#call), used to call services and/or functions.
- [Do](dsl-reference.md#do), used to define one or more subtasks to perform in sequence.
- [Emit](dsl-reference.md#emit), used to emit [events](dsl-reference.md#event).
- [For](dsl-reference.md#for), used to iterate over a collection of items, and conditionally perform a task for each of them.
- [Fork](dsl-reference.md#fork), used to define one or more two subtasks to perform in parallel.
- [Listen](dsl-reference.md#listen), used to listen for an [event](dsl-reference.md#event) or more.
- [Raise](dsl-reference.md#raise), used to raise an [error](dsl-reference.md#error) and potentially fault the [workflow](dsl-reference.md#workflow).
- [Run](dsl-reference.md#run), used to run a [container](dsl-reference.md#container-process), a [script](dsl-reference.md#script-process), a [shell](dsl-reference.md#shell-process) command or even another [workflow](dsl-reference.md#workflow-process). 
- [Set](dsl-reference.md#set), used to dynamically set or update the [workflow](dsl-reference.md#workflow)'s data during the its execution. 
- [Switch](dsl-reference.md#switch), used to dynamically select and execute one of multiple alternative paths based on specified conditions
- [Try](dsl-reference.md#try), used to attempt executing a specified [task](dsl-reference.md#task), and to handle any resulting [errors](dsl-reference.md#error) gracefully, allowing the [workflow](dsl-reference.md#workflow) to continue without interruption.
- [Wait](dsl-reference.md#wait), used to pause or wait for a specified duration before proceeding to the next task.

To ensure they conform to the DSL, runtimes **should** pass all the feature conformance test scenarios defined in the [ctk](ctk/README.md).

##### Secret

Secrets are sensitive information required by a workflow to securely access protected resources or services. They provide a way to securely store and manage credentials, tokens, or other sensitive data used during workflow execution.

Runtime **must** implement a mechanism capable of providing the workflow with the data contained within the defined secrets. If a workflow attempts to access a secret to which it does not have access rights or which does not exist, runtimes **must** raise an error with type `https://serverlessworkflow.io/spec/1.0.0/errors/authorization` and status `403`.

#### Scheduling

Workflow scheduling in ServerlessWorkflow allows developers to specify when and how their workflows should be executed, ensuring timely response to events and efficient resource utilization. It offers four key properties: `every`, `cron`, `after`, and `on`. 

- The `every` property defines the interval for workflow execution, ensuring periodic runs regardless of the previous run's status. 
- With `cron`, developers can use CRON expressions to schedule workflow execution at specific times or intervals.
- `after` specifies a delay duration before restarting the workflow after completion.
- `on` enables event-driven scheduling, triggering workflow execution based on specified events.

See the [DSL reference](dsl-reference.md#schedule) for more details about workflow scheduling.

##### Event-driven scheduling

###### Input of event-driven scheduled workflows

In event-driven scheduled workflows, the input is structured as an array containing the events that trigger the execution of the workflow. This array serves as a vital resource, providing workflow authors access to all relevant data associated with each triggering event. When an event activates the workflow, it populates this array with one or more occurrences, allowing authors to process multiple events simultaneously as needed.

Authors can reference individual events within the array using syntax such as $workflow.input[index], where index indicates the position of the event, starting from 0. For instance, $workflow.input[0] refers to the first event, while $workflow.input[1] refers to the second. This structure allows for easy access to specific event details, and if multiple events are received at once, authors can iterate through the array to handle each one appropriately. This flexibility ensures that workflows can respond effectively to various conditions and triggers, enhancing their overall responsiveness and functionality.

###### Distinguishing event-driven scheduling from start `listen` Tasks

While both `schedule.on` and a start listener task enable event-driven execution of workflows, they serve distinct purposes and have different implications:

- **`schedule.on`**: This property defines when a new instance of a workflow should be created based on an external event. When an event occurs that matches the criteria specified in `schedule.on`, a new workflow instance is initiated. The key point here is that `schedule.on` solely manages the creation of new workflow instances. Any faults or timeouts related to the scheduling process itself are typically invisible to the user and do not impact the workflow instance.

- **Start `listen` task**: A start listener task defines a task that must be undertaken after a new workflow instance has been created. This task listens for specific events and begins processing once the instance is active. The critical difference lies in the fact that a start listener task operates within the context of an already instantiated workflow. If a start listener task experiences a timeout or fault, it can cause the entire workflow instance to fail or behave unexpectedly, directly impacting the flow's execution and outcome.

In essence, while `schedule.on` is concerned with *when* a new workflow instance should be initiated, a start listener task deals with *what* should happen once the instance is active. This distinction is important because it influences how errors and timeouts are handled—`schedule.on` faults are typically invisible and do not affect the workflow, whereas start listener task failures can have a direct and potentially severe impact on the workflow instance they belong to.

### Task Flow

A workflow begins with the first task defined.

Once the task has been executed, different things can happen:

- `continue`: the task ran to completion, and the next task, if any, should be executed. The task to run next is implicitly the next in declaration order, or explicitly defined by the `then` property of the executed task. If the executed task is the last task, then the workflow's execution gracefully ends.
- `fault`: the task raised an uncaught error, which abruptly halts the workflow's execution and makes it transition to `faulted` [status phase](#status-phases).
- `end`: the task explicitly and gracefully ends the workflow's execution. 

### Data Flow

In Serverless Workflow DSL, data flow management is crucial to ensure that the right data is passed between tasks and to the workflow itself. 

Here's how data flows through a workflow based on various transformation stages:

1. **Transform Workflow Input**
Before the workflow starts, the input data provided to the workflow can be transformed to ensure only relevant data in the expected format is passed into the workflow context. This can be done using the top level `input.from` expression. It evaluates on the raw workflow input and defaults to the identity expression which leaves the input unchanged. This step allows the workflow to start with a clean and focused dataset, reducing potential overhead and complexity in subsequent tasks. The result of this expression will set as the initial value for the `$context` runtime expression argument and be passed to the first task.

*Example: If the workflow receives a JSON object as input, a transformation can be applied to remove unnecessary fields and retain only those that are required for the workflow's execution.*

2. **Transform First Task Input**
The input data for the first task can be transformed to match the specific requirements of that task. This ensures that the first task receives only the data required to perform its operations. This can be done using the task's `input.from` expression. It evaluates the transformed workflow input and defaults to the identity expression, which leaves the input unchanged. The result of this expression will be set as the `$input` runtime expression argument and be passed to the task. This transformed input will be evaluated against any runtime expressions used within the task definition.

*Example: If the first task is a function call that only needs a subset of the workflow input, a transformation can be applied to provide only those fields needed for the function to execute.*

3. **Transform First Task Output**
After completing the first task, its output can be transformed before passing it to the next task or storing it in the workflow context. Transformations are applied using the `output.as` runtime expression. It evaluates the raw task output and defaults to the identity expression, which leaves the output unchanged. Its result will be input for the next task. To update the context, one uses the `export.as` runtime expression. It evaluates the raw output and defaults to the expression that returns the existing context. The result of this runtime expression replaces the workflow's current context and the content of the `$context` runtime expression argument. This helps manage the data flow and keep the context clean by removing any unnecessary data produced by the task.

*Example: If the first task returns a large dataset, a transformation can be applied to retain only the relevant results needed for subsequent tasks.*

4. **Transform Last Task Input**
Before the last task in the workflow executes, its input data can be transformed to ensure it receives only the necessary information. This can be done using the task's `input.from` expression. It evaluates the transformed workflow input and defaults to the identity expression, which leaves the input unchanged. The result of this expression will be set as the `$input` runtime expression argument and be passed to the task. This transformed input will be evaluated against any runtime expressions used within the task definition. This step is crucial for ensuring the final task has all the required data to complete the workflow successfully.

*Example: If the last task involves generating a report, the input transformation can ensure that only the data required for the report generation is passed to the task.*

5. **Transform Last Task Output**
After the last task completes, its output can be transformed before it is considered the workflow output. Transformations are applied using the `output.as` runtime expression. It evaluates the raw task output and defaults to the identity expression, which leaves the output unchanged. Its result will be passed to the workflow `output.as` runtime expression. This ensures that the workflow produces a clean and relevant output, free from any extraneous data that might have been generated during the task execution.

*Example: If the last task outputs various statistics, a transformation can be applied to retain only the key metrics that are relevant to the stakeholders.*

6. **Transform Workflow Output**
Finally, the overall workflow output can be transformed before it is returned to the caller or stored. Transformations are applied using the `output.as` runtime expression. It evaluates the last task's output and defaults to the identity expression, which leaves the output unchanged. This step ensures that the final output of the workflow is concise and relevant, containing only the necessary information that needs to be communicated or recorded.

*Example: If the workflow's final output is a summary report, a transformation can ensure that the report contains only the most important summaries and conclusions, excluding any intermediate data.*

By applying transformations at these strategic points, Serverless Workflow DSL ensures that data flows through the workflow in a controlled and efficient manner, maintaining clarity and relevance at each execution stage. This approach helps manage complex workflows and ensures that each task operates with the precise data required, leading to more predictable and reliable workflow outcomes.

Visually, this can be represented as follows:

```mermaid
flowchart TD

  initial_context_arg([<code>$context</code>])
  context_arg([<code>$context</code>])
  input_arg([<code>$input</code>])
  output_arg([<code>$output</code>])

  workflow_raw_input{{Raw Workflow Input}}
  workflow_input_from[Workflow: <code>input.from</code>]
  workflow_transformed_input{{Transformed Workflow Input}}

  task_raw_input{{Raw Task Input}}
  task_input_from[Task: <code>input.from</code>]
  task_transformed_input{{Transformed Task Input}}
  task_definition[Task definition]
  task_raw_output{{Raw Task output}}
  task_output_as[Task: <code>output.as</code>]
  task_transformed_output{{Transformed Task output}}
  task_export_as[Task: <code>export.as</code>]
  
  workflow_raw_output{{Raw Workflow Output}}
  workflow_output_as[Workflow: <code>output.as</code>]
  workflow_transformed_output{{Transformed Workflow Output}}

  workflow_raw_input --> workflow_input_from
  workflow_input_from -- Produces --> workflow_transformed_input
  workflow_transformed_input -- Set as --> initial_context_arg
  workflow_transformed_input -- Passed to --> task_raw_input

  subgraph Task

    task_raw_input -- Passed to --> task_input_from
    task_input_from -- Produces --> task_transformed_input
    task_transformed_input -- Set as --> input_arg
    task_transformed_input -- Passed to --> task_definition

    task_definition -- Execution produces --> task_raw_output
    task_raw_output -- Passed to --> task_output_as
    task_output_as -- Produces --> task_transformed_output
    task_output_as -- Set as --> output_arg
    task_transformed_output -- Passed to --> task_export_as
  end

  task_transformed_output -- Passed as raw input to --> next_task

  subgraph next_task [Next Task]
  end

  task_export_as -- Result set as --> context_arg

  next_task -- Transformed output becomes --> workflow_raw_output
  workflow_raw_output -- Passed to --> workflow_output_as
  workflow_output_as -- Produces --> workflow_transformed_output
```

### Runtime Expressions

Runtime expressions serve as dynamic elements that enable flexible and adaptable workflow behaviors. These expressions provide a means to evaluate conditions, transform data, and make decisions during the execution of workflows.

Runtime expressions allow for the incorporation of variables, functions, and operators to create logic that responds to changing conditions and input data. These expressions can range from simple comparisons, such as checking if a variable meets a certain condition, to complex computations and transformations involving multiple variables and data sources.

One key aspect of runtime expressions is their ability to adapt to runtime data and context. This means that expressions can access and manipulate data generated during the execution of a workflow, enabling dynamic decision-making and behavior based on real-time information.

Runtime expressions in Serverless Workflow can be evaluated using either the default `strict` mode or the `loose` mode. In `strict` mode, all expressions must be properly identified with `${}` syntax. Conversely, in `loose` mode, expressions are evaluated more liberally, allowing for a wider range of input formats. While `strict` mode ensures strict adherence to syntax rules, `loose` mode offers flexibility, allowing evaluation even if the syntax is not perfectly formed.

All runtimes **must** support the default runtime expression language, which is [`jq`](https://jqlang.github.io/jq/).

Runtimes **may** optionally support other runtime expression languages, which authors can specifically use by adequately configuring the workflow. See [`evaluate.language`](dsl-reference.md#evaluate) for more details.

CloudFlows defines [several arguments](#runtime-expression-arguments) that runtimes **must** provide during the evaluation of runtime expressions.

When the evaluation of an expression fails, runtimes **must** raise an error with type `https://serverlessworkflow.io/spec/1.0.0/errors/expression` and status `400`.

#### Runtime expression arguments

| Name | Type | Description |
|:-----|:----:|:------------|
| context | `map` | The task's context data. |
| input | `any` | The task's transformed input. |
| output | `any` | The task's transformed output. |
| secrets | `map` | A key/value map of the workflow secrets.<br>To avoid unintentional bleeding, secrets can only be used in the `input.from` runtime expression. |
| task | [`taskDescriptor`](#task-descriptor) | Describes the current task. |
| workflow | [`workflowDescriptor`](#workflow-descriptor) | Describes the current workflow. |
| runtime | [`runtimeDescriptor`](#runtime-descriptor) | Describes the runtime. |

##### Runtime Descriptor

This argument contains information about the runtime executing the workflow.

| Name | Type | Description | Example |
|:-----|:----:|:------------| ------- |
| name | `string` | A human friendly name for the runtime. | `Synapse`, `Sonata` |
| version | `string` | The version of the runtime. This can be an arbitrary string | a incrementing positive integer (`362`), semantic version (`1.4.78`), commit hash (`04cd3be6da98fc35422c8caa821e0aa1ef6b2c02`) or container image label (`v0.7.43-alpine`) |
| metadata | `map` | An object/map of implementation specific key-value pairs. This can be chosen by runtime implementors and usage of this argument signals that a given workflow definition might not be runtime agnostic | A Software as a Service (SaaS) provider might choose to expose information about the tenant the workflow is executed for e.g. `{ "organization": { "id": "org-ff51cff2-fc83-4d70-9af1-8dacdbbce0be", "name": "example-corp" }, "featureFlags": ["fastZip", "arm64"] }`.  |

##### Workflow Descriptor

| Name | Type | Description | Example |
|:-----|:----:|:------------|:--------|
| id | `string` | A unique id of the workflow execution. Now specific format is imposed | UUIDv4: `4a5c8422-5868-4e12-8dd9-220810d2b9ee`, ULID: `0000004JFGDSW1H037G7J7SFB9` |
| definition | `map` | The workflow's definition as a parsed object | `{ "document": { ... }, "do": [...] }` |
| input | `any` | The workflow's *raw* input (i.e *BEFORE* the `input.from` expression). For the result of `input.from` expression use the `$input` argument | |
| startedAt | [`dateTimeDescriptor`](#datetime-descriptor) | The start time of the execution | |

##### Task Descriptor

| Name | Type | Description | Example |
|:-----|:----:|:------------|:--------|
| name | `string` | The task's name. | `getPet` |
| reference | `string` | The task's reference. | `/do/2/myTask` |
| definition | `map` | The tasks definition (specified under the name) as a parsed object | `{ "call": "http", "with": { ... } }` |
| input | `any` | The task's *raw* input (i.e. *BEFORE* the `input.from` expression). For the result of `input.from` expression use the context of the runtime expression (for jq `.`) | |
| output | `any` | The task's *raw* output (i.e. *BEFORE* the `output.as` expression). | |
| startedAt | [`dateTimeDescriptor`](#datetime-descriptor) | The start time of the task | |

##### DateTime Descriptor

| Name | Type | Description | Example |
|:-----|:----:|:------------|:--------|
| iso8601 | `string` | The date time as a ISO 8601 date time string. It uses `T` as the date-time delimiter, either UTC (`Z`) or a time zone offset (`+01:00`). The precision can be either seconds, milliseconds or nanoseconds | `2022-01-01T12:00:00Z`, `2022-01-01T12:00:00.123456Z`, `2022-01-01T12:00:00.123+01:00` |
| epoch.seconds | `integer` | The date time as a integer value of seconds since midnight of 1970-01-01 UTC (i.e. [the Unix Epoch](https://en.wikipedia.org/wiki/Unix_time)) | `1641024000` (="2022-01-01T08:00:00Z") |
| epoch.milliseconds | `integer` | The date time as a integer value of milliseconds since midnight of 1970-01-01 UTC. This contains the *whole* timestamp, not just the millisecond portion | `1641024000123` (="2022-01-01T08:00:00.123Z") |

The following table shows which arguments are available for each runtime expression:

| Runtime Expression | Evaluated on | Produces | `$context` | `$input` | `$output` | `$secrets` | `$task` | `$workflow` |
|:-------------------|:---------:|:---------:|:---------:|:---------:|:-------:|:---------:|:-------:|:----------:|
| Workflow `input.from` | Raw workflow input | Transformed workflow input | | | | ✔ | | ✔ |
| Task `input.from` | Raw task input (i.e. transformed workflow input for the first task, transformed output from previous task otherwise) | Transformed task input | ✔ | | | ✔ | ✔ | ✔ |
| Task `if` | Transformed task input | | ✔ | ✔ | | ✔ | ✔ | ✔ |
| Task definition | Transformed task input | | ✔ | ✔ | | ✔ | ✔ | ✔ |
| Task `output.as` | Raw task output | Transformed task output | ✔ | ✔ | | ✔ | ✔ | ✔ |
| Task `export.as` | Transformed task output | `$context` | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Workflow `output.as` | Last task's transformed output | Transformed workflow output | ✔ | | | ✔ | | ✔ |

### Fault Tolerance

Serverless Workflow is designed with resilience in mind, acknowledging that errors are an inevitable part of any system. The DSL provides robust mechanisms to identify, describe, and handle errors effectively, ensuring the workflow can recover gracefully from failures.

Overall, the fault tolerance features in Serverless Workflow enhance its robustness and reliability, making it capable of handling a wide range of failure scenarios gracefully and effectively.

#### Errors

Errors in Serverless Workflow are described using the [Problem Details RFC](https://datatracker.ietf.org/doc/html/rfc7807). This specification helps to standardize the way errors are communicated, using the `instance` property as a [JSON Pointer](https://datatracker.ietf.org/doc/html/rfc6901) to identify the specific component of the workflow that has raised the error. By adhering to this standard, authors and runtimes can consistently describe and handle errors.

*Example error:*
```yaml
type: https://serverlessworkflow.io/spec/1.0.0/errors/communication
title: Service Unavailable
status: 503
detail: The service is currently unavailable. Please try again later.
instance: /do/getPetById
```

The Serverless Workflow specification defines several [standard error types](dsl-reference.md#standard-error-types) to describe commonly known errors, such as timeouts. Using these standard error types ensures that workflows behave consistently across different runtimes, and allows authors to rely on predictable error handling and recovery processes.

See the [DSL reference](dsl-reference.md#error) for more details about errors.

#### Retries

Errors are critical for both authors and runtimes as they provide a means to communicate and describe the occurrence of problems. This, in turn, enables the definition of mechanisms to catch and act upon these errors. For instance, some errors caught using a [`try`](dsl-reference.md#try) block may be transient and temporary, such as a `503 Service Unavailable`. In such cases, the DSL provides a mechanism to retry a faulted task, allowing for recovery from temporary issues.

*Retrying 5 times when an error with 503 is caught:*
```yaml
try:
  call: http
  with:
    method: get
    endpoint:
      uri: https://example-service.com/healthz
catch:
  errors:
    with:
      status: 503
  retry:
    delay:
      seconds: 3
    backoff:
      linear: {}
    limit:
      attempt:
        count: 5
```

### Timeouts

Workflows and tasks alike can be configured to timeout after a defined amount of time.

When a timeout occur, runtimes **must** abruptly interrupt the execution of the workflow/task, and **must** raise an error that, if uncaught, force the workflow/task to transition to the [`faulted` status phase](#status-phases).

A timeout error **must** have its `type` set to `https://serverlessworkflow.io/spec/1.0.0/errors/timeout` and **should** have its `status` set to `408`.

### Interoperability

Serverless Workflow DSL is designed to seamlessly interact with a variety of services, ensuring robust service interoperability.

#### Supported Protocols

- [**HTTP**](dsl-reference.md#http-call): Allows the workflow to make standard HTTP requests to web services. This is useful for RESTful services and web APIs that communicate over the HTTP protocol.
- [**gRPC**](dsl-reference.md#grpc-call): Supports Remote Procedure Call (RPC) using gRPC, a high-performance, open-source universal RPC framework. This is suitable for connecting to services that require low-latency and high-throughput communication.
- [**AsyncAPI**](dsl-reference.md#asyncapi-call): Facilitates interaction with asynchronous messaging protocols. AsyncAPI is designed for event-driven architectures, allowing workflows to publish and subscribe to events.
- [**OpenAPI**](dsl-reference.md#openapi-call): Enables communication with services that provide OpenAPI specifications, which is useful for defining and consuming RESTful APIs.

Runtimes **must** raise an error with type `https://serverlessworkflow.io/spec/1.0.0/errors/communication` if and when a problem occurs during a call.

#### Custom and Non-Standard Interactions

 In addition to the default supported protocols, the DSL also provides mechanisms to interact with services in non-standard or unsupported ways using custom processes. This flexibility allows workflows to extend their capabilities beyond the built-in protocols and integrate with any service, regardless of the communication method.

For custom interactions, the workflow can define [tasks](dsl-reference.md#run) that [execute shell commands](dsl-reference.md#shell-process), [execute scripts](dsl-reference.md#script-process) or [run containers](dsl-reference.md#container-process) to handle unique service communication requirements. This ensures that the workflow can still maintain interoperability even with services that do not adhere to the standard supported protocols.

##### Creating a Custom Function

Serverless Workflow DSL supports the creation and publication of custom functions to extend the DSL capabilities. 

Custom functions allow you to define specific tasks and interactions that are not covered by the default supported protocols. 

Here’s how you can define and use custom functions within your workflows:

1. In your repository, create a new directory for your function, for example, `/serverless-workflow/functions/my-custom-function`. It is strongly recommended that custom function authors include the semantic version of the function in its path. For instance, you might structure the path as `/serverless-workflow/functions/my-custom-function/1.0.0` to reflect the function's version.

2. Create a `function.yaml` file containing the [function's definition](dsl-reference.md#task). Ideally, the function should document both its input and output. This is important for documentation and validation purposes.

```yaml
#function.yaml
input:
  schema:
    document:
      type: object
      description: The function's input
      properties:
        emailAddress:
          type: string
          description: The email address to validate.
output:
  schema:
    document:
      type: object
      description: The function's output
      properties:
        isValid:
          type: boolean
          description: A boolean indicating whether or not the email address is valid.
run:
  script:
    language: javascript
    code: |
      function validateEmail(email) {
        const re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        return re.test(email);
      }
      return { isValid: validateEmail(emailAddress) };
    arguments:
      emailAddress: ${ .emailAddress }
```

3. Optionally, add all the local files your function might need into its directory. 

4. Commit and push your function to your repository.

5. Optionally, submit your function to the [Serverless Workflow Catalog](https://github.com/serverlessworkflow/catalog), allowing users to find your function.

For more information about authoring a new custom function, visit the [Serverless Workflow Catalog](https://github.com/serverlessworkflow/catalog).

##### Using a Custom Function

Once a custom function is defined, it can be used within a workflow to perform specific tasks. 

The following example demonstrates how to use the `validateEmailAddress` custom function in a workflow.

```yaml
# workflow.yaml
document:
  dsl: '1.0.0-alpha1'
  namespace: default
  name: customFunctionWorkflow
  version: '0.1.0'
do:
  - validateEmail:
      call: https://github.com/myorg/functions/validateEmailAddress@v1.0.0
      with:
        emailAddress: ${ .userEmail }
```

##### Publishing a Custom Function

Consider submitting your function to the [Serverless Workflow Function Catalog](https://github.com/serverlessworkflow/catalog). 

This optional step allows users to discover and utilize your function, enhancing its visibility and usability within the Serverless Workflow community. By registering your function, you contribute to a shared repository of resources that can streamline workflow development for others.

For detailed instructions on how to contribute your custom function, please refer to the [CONTRIBUTING.md](https://github.com/serverlessworkflow/catalog/blob/main/CONTRIBUTING.md) file.

### Events

Events play a crucial role in Serverless Workflow by facilitating communication and coordination between different components and services. They enable workflows to react to external stimuli, paving the way for event-driven architectures and real-time processing scenarios. Events are essentially messages that convey information about a specific occurrence or action, allowing workflows to respond dynamically to changes in their environment.

Events in Serverless Workflow adhere to the [Cloud Events specification](https://cloudevents.io/), ensuring interoperability and compatibility with event-driven systems. This standardization allows workflows to seamlessly interact with various event sources and consumers across different platforms and environments.

#### Emitting Events

The Emit task allows workflows to publish events to event brokers or messaging systems. This capability enables workflows to broadcast notifications about various events, such as order placements, data updates, or system events. 

By emitting events, workflows can seamlessly integrate with event-driven architectures, facilitating event-driven decision-making and enabling reactive behavior based on incoming events. For example, a workflow handling order processing might emit an event signaling the successful placement of an order, triggering downstream processes like inventory management or shipping.

See the [DSL reference](dsl-reference.md#emit) for more details about emit tasks.

#### Listening for Events

The Listen task provides a mechanism for workflows to await and react to external events. It enables workflows to subscribe to specific event types or patterns and trigger actions based on incoming events.

This capability allows workflows to implement event-driven behavior, where they respond dynamically to changes in their environment. For instance, a workflow responsible for monitoring vital signs in a healthcare application might listen for temperature or heart rate measurements. Upon receiving such measurements, the workflow can perform actions like alerting medical staff or updating patient records.

In summary, events in Serverless Workflow serve as the foundation for building event-driven architectures and enable workflows to communicate, coordinate, and react to changes in their environment effectively. They empower workflows to operate in real-time, making them well-suited for scenarios requiring dynamic, responsive behavior.

See the [DSL reference](dsl-reference.md#listen) for more details about listen tasks.

### Extensions

Extensions in Serverless Workflow offer a flexible way to extend the functionality of tasks within a workflow. They allow developers to inject custom logic, perform pre- or post-processing tasks, and modify task behavior dynamically based on runtime conditions. With extensions, developers can enhance workflow capabilities, promote code reuse, and maintain consistency across workflows.

For example, extensions can be used to:

1. Perform logging before and after task execution.
2. Intercept HTTP calls to mock service responses.
3. Implement custom error handling or retries.
4. Apply security checks or data transformations.

Extensions are defined with properties such as `extend`, `when`, `before`, and `after`, providing precise control over their application. Here's a brief summary:

- **`extend`**: Specifies the type of task to extend.
- **`when`**: Conditionally applies the extension based on runtime expressions.
- **`before`**: Executes tasks before the extended task.
- **`after`**: Executes tasks after the extended task completes.

Overall, extensions empower developers to build complex workflows with enhanced functionality and maintainability, making Serverless Workflow a powerful tool for orchestrating cloud-native applications.

See the [DSL reference](dsl-reference.md#extension) for more details about extensions.

*Sample logging extension:*
```yaml
document:
  dsl: '1.0.0-alpha1'
  namespace: test
  name: sample-workflow
  version: '0.1.0'
use:
  extensions:
    logging:
      extend: all
      before:
        - sendLog:
            call: http
            with:
              method: post
              uri: https://fake.log.collector.com
              body:
                message: "${ \"Executing task '\($task.reference)'...\" }"
      after:
        - sendLog:
            call: http
            with:
              method: post
              uri: https://fake.log.collector.com
              body:
                message: "${ \"Executed task '\($task.reference)'...\" }"
do:
  - sampleTask:
      call: http
      with:
        method: get
        uri: https://fake.com/sample
```

### External Resources

External resources in Serverless Workflow allow you to define and access external assets or services required for workflow execution. 

These resources can include APIs, databases, files, or any other external entities needed by the workflow. Each external resource can be identified by a unique name and is associated with a URI that specifies its location. 

Optionally, you can specify an authentication policy to ensure secure access to the resource. For instance, you can use basic authentication with a username and password, or you can reference a pre-configured authentication policy by name.

By defining external resources within the workflow, you centralize resource management and streamline access to external dependencies, enhancing the modularity and maintainability of your workflows.

### Authentication

Authentication in Serverless Workflow specifies the method by which the workflow accesses protected resources or services.

Amonst others, [external resources](dsl-reference.md#external-resource) and [calls](dsl-reference.md#call) may define authentication.

The Serverless Workflow DSL supports a suite of standard authentication mechanisms, amongst which are:

- **Basic Authentication**: Utilizes a username-password pair for authentication.
```yaml
sampleBasic:
  basic:
    username: admin
    password: 123
```

- **Bearer Authentication**: Uses a bearer token for authentication.
```yaml
sampleBearer:
  bearer: ${ .user.token }
```

- **OAuth2 Authentication**: Implements OAuth2 authorization framework for secure access.
```yaml
sampleOAuth2:
  oauth2:
    authority: http://keycloak/realms/fake-authority/.well-known/openid-configuration
    grant: client-credentials
    client:
      id: workflow-runtime
      secret: workflow-runtime-client-secret
    scopes: [ api ]
    audiences: [ runtime ]
```

These authentication schemes can be defined globally in the authentication section or associated with specific endpoints. They provide secure access to resources while ensuring proper authorization and identity verification.

See the [DSL reference](dsl-reference.md#authentication) for more details about authentication.