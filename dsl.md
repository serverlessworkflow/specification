# Serverless Workflow DSL

## Table of Contents

- [Abstract](#abstract)
- [Motivation](#motivation)
- [Design](#design)
- [Concepts](#concepts)
  + [Workflow](#workflow)
    - [Status Phases](#status-phases)
    - [Components](#components)
      + [Task](#task)
    - [Scheduling](#scheduling)
  + [Task Flow](#task-flow)
  + [Data Flow](#data-flow)
  + [Runtime Expressions](#runtime-expressions)
    - [Arguments](#runtime-expression-arguments)
  + [Fault Tolerance](#fault-tolerance)
  + [Timeouts](#timeouts)
  + [Interoperability](#interoperability)
    - [Supported Protocols](#supported-protocols)
    - [Custom and Non-Standard Interactions](#custom-and-non-standard-interactions)
    - [Creating a Custom Function](#creating-a-custom-function)
    - [Using a Custom Function](#using-a-custom-function)
    - [Publishing a Custom Function](#publishing-a-custom-function)
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

A Serverless Workflow is a sequence of specific [tasks](#tasks) that are executed in a defined order. By default, this order follows the declaration sequence within the workflow definition. Workflows are designed to automate processes and orchestrate various serverless functions and services. 

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

Additionally, the flow of execution within a workflow can be controlled using [directives*](#flow-directives), which provide instructions to the workflow engine on how to manage and handle specific aspects of workflow execution.

**To learn more about flow directives and how they can be utilized to control the execution and behavior of workflows, please refer to [Flow Directives](dsl-reference.md#flow-directive).*

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
- [Sequentially](dsl-reference.md#sequentially), used to define a minimum of two subtasks to perform sequentially.
- [Concurrently](dsl-reference.md#concurrently), used to define a minimum of two subtasks to perform concurrently.
- [Competitively](dsl-reference.md#competitively), used to define a minimum of two subtasks to perform concurrently, and to continue with the first one that completes.
- [Emit](dsl-reference.md#emit), used to emit [events](dsl-reference.md#event).
- [For](dsl-reference.md#for), used to iterate over a collection of items, and conditionally perform a task for each of them.
- [Listen](dsl-reference.md#listen), used to listen for an [event](dsl-reference.md#event) or more.
- [Raise](dsl-reference.md#raise), used to raise an [error](dsl-reference.md#error) and potentially fault the [workflow](dsl-reference.md#workflow).
- [Run](dsl-reference.md#run), used to run a [container](dsl-reference.md#container), a [script](dsl-reference.md#script) or event a [shell](dsl-reference.md#shell) command. 
- [Switch](dsl-reference.md#switch), used to dynamically select and execute one of multiple alternative paths based on specified conditions
- [Set](dsl-reference.md#set), used to dynamically set or update the [workflow](dsl-reference.md#workflow)'s data during the its execution. 
- [Try](dsl-reference.md#try), used to attempt executing a specified [task](dsl-reference.md#task), and to handle any resulting [errors](dsl-reference.md#error) gracefully, allowing the [workflow](dsl-reference.md#workflow) to continue without interruption.
- [Wait](dsl-reference.md#wait), used to pause or wait for a specified duration before proceeding to the next task.

To ensure they conform to the DSL, runtimes **should** pass all the feature conformance test scenarii defined in the [ctk](ctk/README.md).

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

### Task Flow

A workflow begins with the first task defined.

Once the task has been executed, different things can happen:

- `continue`: the task ran to completion, and the next task, if any, should be executed. The task to run next is implictly the next in declaration order, or explicitly defined by the `then` property of the executed task. If the executed task is the last task, then the workflow's execution gracefully ends.
- `fault`: the task raised an uncaught error, which abruptly halts the workflow's execution and makes it transition to `faulted` [status phase](#status-phases).
- `end`: the task explicitly and gracefully ends the workflow's execution. 

### Data Flow

In Serverless Workflow DSL, data flow management is crucial to ensure that the right data is passed between tasks and to the workflow itself. 

Here's how data flows through a workflow based on various filtering stages:

1. **Filter Workflow Input**
Before the workflow starts, the input data provided to the workflow can be filtered to ensure only relevant data is passed into the workflow context. This step allows the workflow to start with a clean and focused dataset, reducing potential overhead and complexity in subsequent tasks.

*Example: If the workflow receives a JSON object as input, a filter can be applied to remove unnecessary fields and retain only those that are required for the workflow's execution.*

2. **Filter First Task Input**
The input data for the first task can be filtered to match the specific requirements of that task. This ensures that the first task receives only the necessary data it needs to perform its operations.

*Example: If the first task is a function call that only needs a subset of the workflow input, a filter can be applied to provide only those fields needed for the function to execute.*

3. **Filter First Task Output**
After the first task completes, its output can be filtered before passing it to the next task or storing it in the workflow context. This helps in managing the data flow and keeping the context clean by removing any unnecessary data produced by the task.

*Example: If the first task returns a large dataset, a filter can be applied to retain only the relevant results needed for subsequent tasks.*

4. **Filter Last Task Input**
Before the last task in the workflow executes, its input data can be filtered to ensure it receives only the necessary information. This step is crucial for ensuring that the final task has all the required data to complete the workflow successfully.

*Example: If the last task involves generating a report, the input filter can ensure that only the data required for the report generation is passed to the task.*

5. **Filter Last Task Output**
After the last task completes, its output can be filtered before it is considered as the workflow output. This ensures that the workflow produces a clean and relevant output, free from any extraneous data that might have been generated during the task execution.

*Example: If the last task outputs various statistics, a filter can be applied to retain only the key metrics that are relevant to the stakeholders.*

6. **Filter Workflow Output**
Finally, the overall workflow output can be filtered before it is returned to the caller or stored. This step ensures that the final output of the workflow is concise and relevant, containing only the necessary information that needs to be communicated or recorded.

*Example: If the workflow's final output is a summary report, a filter can ensure that the report contains only the most important summaries and conclusions, excluding any intermediate data.*

By applying filters at these strategic points, Serverless Workflow DSL ensures that data flows through the workflow in a controlled and efficient manner, maintaining clarity and relevance at each stage of execution. This approach helps in managing complex workflows and ensures that each task operates with the precise data it requires, leading to more predictable and reliable workflow outcomes.

### Runtime Expressions

Runtime expressions serve as dynamic elements that enable flexible and adaptable workflow behaviors. These expressions provide a means to evaluate conditions, transform data, and make decisions during the execution of workflows.

Runtime expressions allow for the incorporation of variables, functions, and operators to create logic that responds to changing conditions and input data. These expressions can range from simple comparisons, such as checking if a variable meets a certain condition, to complex computations and transformations involving multiple variables and data sources.

One key aspect of runtime expressions is their ability to adapt to runtime data and context. This means that expressions can access and manipulate data generated during the execution of a workflow, enabling dynamic decision-making and behavior based on real-time information.

Runtime expressions in Serverless Workflow can be evaluated using either the default `strict` mode or the `loose` mode. In `strict` mode, all expressions must be properly identified with `${}` syntax. Conversely, in `loose` mode, expressions are evaluated more liberally, allowing for a wider range of input formats. While `strict` mode ensures strict adherence to syntax rules, `loose` mode offers flexibility, allowing evaluation even if the syntax is not perfectly formed.

All runtimes **must** support the default runtime expression language, which is [`jq`](https://jqlang.github.io/jq/).

Runtimes **may** optionally support other runtime expression languages, which authors can specifically use by adequately configuring the workflow. See [`evaluate.language`](dsl-reference.md#evaluate) for more details.

CloudFlows defines [several arguments](#runtime-expression-arguments) that runtimes **must** provide during the evaluation of runtime expressions.

When the evaluation of an expression fails, runtimes **must** raise an error with type `https://https://serverlessworkflow.io/spec/1.0.0/errors/expression` and status `400`.

#### Runtime expression arguments

| Name | Type | Description |
|:-----|:----:|:------------|
| context | `map` | The task's context data. |
| input | `any` | The task's filtered input. |
| task | [`taskDescriptor`](#task-descriptor) | Describes the current task. |
| workflow | [`workflowDescritor`](#workflow-descriptor) | Describes the current workflow. |

### Fault Tolerance

Serverless Workflow is designed with resilience in mind, acknowledging that errors are an inevitable part of any system. The DSL provides robust mechanisms to identify, describe, and handle errors effectively, ensuring the workflow can recover gracefully from failures.

Overall, the fault tolerance features in Serverless Workflow enhance its robustness and reliability, making it capable of handling a wide range of failure scenarios gracefully and effectively.

#### Errors

Errors in Serverless Workflow are described using the [Problem Details RFC](https://datatracker.ietf.org/doc/html/rfc7807). This specification helps to standardize the way errors are communicated, using the `instance` property as a [JSON Pointer](https://datatracker.ietf.org/doc/html/rfc6901) to identify the specific component of the workflow that has raised the error. By adhering to this standard, authors and runtimes can consistently describe and handle errors.

*Example error:*
```yaml
type: https://https://serverlessworkflow.io/spec/1.0.0/errors/communication
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

A timeout error **must** have its `type` set to `https://https://serverlessworkflow.io/spec/1.0.0/errors/timeout` and **should** have its `status` set to `408`.

### Interoperability

Serverless Workflow DSL is designed to seamlessly interact with a variety of services, ensuring robust service interoperability.

#### Supported Protocols
 - [**HTTP**](dsl-reference.md#http-call): Allows the workflow to make standard HTTP requests to web services. This is useful for RESTful services and web APIs that communicate over the HTTP protocol.
- [**gRPC**](dsl-reference.md#grpc-call): Supports Remote Procedure Call (RPC) using gRPC, a high-performance, open-source universal RPC framework. This is suitable for connecting to services that require low-latency and high-throughput communication.
- [**AsyncAPI**](dsl-reference.md#asyncapi-call): Facilitates interaction with asynchronous messaging protocols. AsyncAPI is designed for event-driven architectures, allowing workflows to publish and subscribe to events.
- [**OpenAPI**](dsl-reference.md#openapi-call): Enables communication with services that provide OpenAPI specifications, which is useful for defining and consuming RESTful APIs.

Runtimes **must** raise an error with type `https://https://serverlessworkflow.io/spec/1.0.0/errors/communication` if and when a problem occurs during a call.

#### Custom and Non-Standard Interactions

 In addition to the default supported protocols, the DSL also provides mechanisms to interact with services in non-standard or unsupported ways using custom processes. This flexibility allows workflows to extend their capabilities beyond the built-in protocols and integrate with any service, regardless of the communication method.

For custom interactions, the workflow can define [tasks](dsl-reference.md#run) that [execute shell commands](dsl-reference.md#shell-process), [execute scripts](dsl-reference.md#script-process) or [run containers](dsl-reference.md#container-process) to handle unique service communication requirements. This ensures that the workflow can still maintain interoperability even with services that do not adhere to the standard supported protocols.

##### Creating a Custom Function

Serverless Workflow DSL supports the creation and publication of custom functions to extend the DSL capabilities. 

Custom functions allow you to define specific tasks and interactions that are not covered by the default supported protocols. Here’s how you can define and use custom functions within your workflows.

1. In your repository, create a new directory for your function, for example, `/.serverless-workflow/functions/my-custom-function.

2. Create a `function.yaml` file containing the [function's definition](dsl-reference.md#task).

```yaml
#function.yaml
validateEmailAddress:
  describe:
    summary: Validate email addresses from input data
  input:
    schema:
      type: object
      properties:
        emailAddress:
          type: string
  output:
    schema:
      type: object
      properties:
        isValid:
          type: boolean
  run:
    script:
      type: javascript
      source: |
        function validateEmail(email) {
          const re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
          return re.test(email);
        }
        const emailAddress = '${ .emailAddress }';
        return { isValid: validateEmail(emailAddress) };

```

3. Optionally, add all the local files your function might need into its directory. 

4. Commit and push your function to your repository.

5. Optionally, submit your function to the Serverless Worfklow Function Registry, allowing users to find your function.

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
  call: https://github.com/myorg/functions/validateEmailAddress@v1
  with:
    emailAddress: ${ .userEmail }
```

##### Publishing a Custom Function

Consider submitting your function to the Serverless Workflow Function Registry. 

This optional step allows users to discover and utilize your function, enhancing its visibility and usability within the Serverless Workflow community. By registering your function, you contribute to a shared repository of resources that can streamline workflow development for others.

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
        call: http
        with:
          method: post
          uri: https://fake.log.collector.com
          body:
            message: "${ \"Executing task '\($task.reference)'...\" }"
      after:
        call: http
        with:
          method: post
          uri: https://fake.log.collector.com
          body:
            message: "${ \"Executed task '\($task.reference)'...\" }"
do:
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