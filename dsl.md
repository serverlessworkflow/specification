# Serverless Workflow DSL

## Table of Contents

- [Abstract](#abstract)
- [Motivation](#motivation)
- [Design](#design)
- [Versioning](#versioning)

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

## Versioning

## Version

The versioning strategy for the Serverless Workflow DSL is structured to accommodate different types of changes introduced through pull requests (PRs). 

If a PR is labeled with `change: documentation`, indicating modifications related to improving or clarifying documentation, it does not trigger a version change. 

Conversely, if the PR addresses a fix, labeled as `change: fix`, it results in an increase in the patch version (0.0.x). 
A fix typically refers to corrections made to resolve bugs or errors in the DSL specification or its implementations, ensuring smoother functionality and reliability. 

Similarly, when a PR introduces a new feature, labeled as `change: feature`, it prompts an increase in the minor version (0.x.0). 
A feature denotes the addition of significant capabilities, enhancements, or functionalities that extend the DSL's capabilities or improve its usability. 

Lastly, if the PR is marked as `change: breaking`, indicating alterations that are incompatible with previous versions, it leads to an increase in the major version (x.0.0). A breaking change signifies modifications that necessitate adjustments in existing workflows or implementations, potentially impacting backward compatibility. 

This versioning strategy ensures clarity and transparency in tracking changes and communicating their impact on users and implementations.

| Label | Version Change |  Description  |
|:-- |:---:|:---|
| `change: documentation` | - | Modifications related to documentation improvements or clarifications. |
| `change: fix` | `0.0.x` | Corrections made to resolve bugs or errors in the DSL specification or its implementations. |
| `change: feature` | `0.x.0` | Addition of significant capabilities, enhancements, or functionalities that extend the DSL's capabilities or improve its usability. |
| `change: breaking` | `x.0.0` | Alterations that are incompatible with previous versions, necessitating adjustments in existing workflows or implementations. |

In addition to versioning changes denoted by labels in pull requests, pre-release versions will be suffixed with either `alphaX`, `betaX`, or `rcX` where `X` represents the pre-release version number (ex: `1.0.0-alpha1`). These pre-release versions are designated to indicate different stages of development and testing before the final release.

- **Alpha versions** are the earliest stages of testing and development. They typically contain incomplete features and may have known issues. They are intended for a limited audience, such as internal testers or early adopters, for initial feedback and testing.

- **Beta versions** represent a more stable state compared to alpha versions. They are released to a broader audience, allowing for wider testing and feedback collection. Beta versions may still contain bugs or issues, but they are generally considered to be closer to the final release state.

- **Release Candidate (RC)** versions are the versions that are considered to be potentially ready for final release. They undergo rigorous testing to identify and resolve any remaining critical issues. RC versions are released to a wider audience for final validation before the official release.

These pre-release versions with appropriate suffixes provide transparency about the development stage and help users and testers understand the level of stability and readiness of each release candidate.

## Concepts

### Workflow

A Serverless Workflow is a sequence of specific [tasks](#tasks) that are executed in a predetermined order. By default, this order follows the declaration sequence within the workflow definition. Workflows are designed to automate processes and orchestrate various serverless functions and services. 

Workflows can be triggered in different ways: upon request, scheduled using CRON expressions, or initiated upon correlation with specific events. 

Additionally, workflows may optionally accept inputs and produce outputs, allowing for data processing and transformation within the workflow execution.

Workflows in the Serverless Workflow DSL can exist in several phases, each indicating the current state of the workflow execution. These phases include:

| Phase |	Description |
| --- | --- |
| `Pending` |	The workflow has been initiated and is pending execution. |
| `Running` |	The workflow is currently in progress. |
| `Suspended` |	The workflow execution has been paused or halted temporarily. |
| `Cancelled` |	The workflow execution has been terminated before completion. |
| `Faulted` |	The workflow execution has encountered an error. |
| `Completed` |	The workflow execution has successfully finished all tasks. |

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