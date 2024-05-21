# Serverless Workflow DSL

## Table of Contents

- [Abstract](#abstract)
- [Motivation](#motivation)
- [Design](#design)
- [Concepts](#concepts)
  + [Workflow](#workflow)

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

A Serverless Workflow is a sequence of specific [tasks](#tasks) that are executed in a predetermined order. By default, this order follows the declaration sequence within the workflow definition. Workflows are designed to automate processes and orchestrate various serverless functions and services. 

Workflows can be triggered in different ways: upon request, scheduled using CRON expressions, or initiated upon correlation with specific events. 

Additionally, workflows may optionally accept inputs and produce outputs, allowing for data processing and transformation within the workflow execution.

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

**To learn more about flow directives and how they can be utilized to control the execution and behavior of workflows, please refer to [Flow Directives](#flow-directives).*

#TODO:
+ Describe how workflow flows
+ Explain how data flows
+ Explain flow directives
+ Explain runtime expressions
+ Explain referanceable components
+ Explain tasks
  - Call
  - Execute
  - Emit
  - For
  - Listen
    + Listen to one
    + Listen to all
    + Listen to any
    + Listen to any until
  - Raise
  - Run
    + container
    + script
    + shell
    + workflow
  - Set
  - Try
  - Wait
+ Explain errors
+ Explain fault tolerance, retries and timeouts
+ Explain service interoperability
+ Explain custom functionality/processes
+ Explain extensions, with before/after
+ Explain authentication
+ Explain endpoints and external resources