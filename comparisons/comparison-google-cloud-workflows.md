# Comparisons - Google Cloud Workflows

[Google Cloud Workflows](https://cloud.google.com/workflows) is a proprietary Google serverless workflow language
and runtime service. It's main features, as mentioned on their website, include orchestration of Google Cloud 
and HTTP-based API services, automation of complex processes, no infra/capacity planning, scalability support, and 
a pay-per-use pricing model.

We are focusing here only on the Google Cloud Workflow dsl (the language definition). The purpose of this document
is to show a side-by-side comparisons between the equivalent markup of the Serverless Workflow Specifiation workflow language 
and that of Google Cloud Workflows.
This can hopefully help compare and contrast the two workflow languages and give a better understanding of both.

The Google Cloud Workflow examples used in this document are all available on the 
[GoogleCloudPlatform Workflow examples github page](https://github.com/GoogleCloudPlatform/workflows-samples/tree/main/src).


## Preface

Both Serverless Workflow and Google Cloud Workflow can describe their workflow language
in both JSON and YAML formats. For the sake of this document, our comparison
examples will include examples in JSON format, but same can be established with 
YAML formats as well.

Overall, as of the time of the writing of this document, the Serverless Workflow language is a 
super-set of the Google Cloud Workflow language
in terms of functionality. It also focuses more on the domain-specific aspects, where as 
the Google Workflow language seems to be more code-like and focused on easy integration
with their runtime implementation it seems. 

We hope that these examples will give you a good start for comparing and contrasting the two serverless workflow
languages.

## Table of Contents
 
- [Greeting with Arguments](#Greeting-With-Arguments)
- [Concatenating array values](#Concatenating-Array-Values)
- [Connect Compute engine](#Connect-Compute-Engine)
- [Error Handling for REST service invocation](#Error-Handling-For-REST-Service-Invocation)
- [Retrying on errors](#Retrying-On-Errors)
- [Sub Workflows](#Sub-Workflows)
- [Data based condition](#Data-Based-Conditions)


### Greeting With Arguments

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/args.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
{
  "main": {
    "params": [
      "args"
    ],
    "steps": [
      {
        "step1": {
          "assign": [
            {
              "outputVar": "${\"Hello \" + args.firstName + \" \" + args.lastName}"
            }
          ]
        }
      },
      {
        "step2": {
          "return": "${outputVar}"
        }
      }
    ]
  }
}
```

</td>
<td valign="top">

```json
{
    "id": "greetingwithargs",
    "name": "Greeting With Args",
    "start": "Set Output",
    "states": [
        {
            "name": "Set Output",
            "type": "inject",
            "data": {
                "outputVar": "Hello ${ .firstname + \" \" +  .lastname  }"
            },
            "stateDataFilter": {
                "output": "${ .outputVar }"
             },
            "end": true
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Both languages allow for JSON initializing data to be defined within the markup.
Google Workflow uses the "assign" keyword to set specific data property where as
Serverless Workflow has a dedicated state for this. Google Workflow uses 
a second step with a "return" keyword to set the workflow output where as 
in Serverless Workflow each state can define data filters to select the state 
data which should be passed to the next state or become workflow data output.
It's important to mention that the inject state is not needed in Serverless Workflow
as this data can also be dynamically passed to the workflow when instance a workflow 
instance is created. See the Serverless Workflow ["Workflow Data"](../specification.md#Workflow-Data) section for more info on this.

### Concatenating Array Values

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/array.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
[
  {
    "define": {
      "assign": [
        {
          "array": [
            "foo",
            "ba",
            "r"
          ]
        },
        {
          "result": ""
        },
        {
          "i": 0
        }
      ]
    }
  },
  {
    "check_condition": {
      "switch": [
        {
          "condition": "${len(array) > i}",
          "next": "iterate"
        }
      ],
      "next": "exit_loop"
    }
  },
  {
    "iterate": {
      "assign": [
        {
          "result": "${result + array[i]}"
        },
        {
          "i": "${i+1}"
        }
      ],
      "next": "check_condition"
    }
  },
  {
    "exit_loop": {
      "return": {
        "concat_result": "${result}"
      }
    }
  }
]
```

</td>
<td valign="top">

```json
{
    "id": "concatarray",
    "name": "Concatenating array values",
    "start": "DoConcat",
    "states": [
        {
            "name": "DoConcat",
            "type": "inject",
            "data": {
                "array": [
                    "foo",
                    "ba",
                    "r"
                ]
            },
            "stateDataFilter": {
                "output": "${ .array | join(\"\") }"
             },
            "end": true
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Google Workflow lang takes a programmatic-like approach here by iterating the array values
with the "switch" directive.
It uses the "+" symbol to we assume is how the underlying programming language
used in their runtime impl can concatenate strings.
The second step, "exit-loop" is then used alongside the "return" keyword to specify the 
workflow results.
With Serverless Workflow we can inject the array data via the "inject" state again, or 
it can simply be passed as workflow data input. There is no need for looping here as
we can just utilize the [jq "join" function](https://stedolan.github.io/jq/manual/#join(str)) as shown in the states data filter.
We could use the [ForEach state](../specification.md#ForEach-State) for iteration of 
array values, however it would just unnecessarily complicate things.

### Connect Compute Engine

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/connect_compute_engine.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
[
  {
    "initialize": {
      "assign": [
        {
          "project": "${sys.get_env(\"GOOGLE_CLOUD_PROJECT_NUMBER\")}"
        },
        {
          "zone": "us-central1-a"
        },
        {
          "vmToStop": "examplevm"
        }
      ]
    }
  },
  {
    "stopInstance": {
      "call": "http.post",
      "args": {
        "url": "${\"https://compute.googleapis.com/compute/v1/projects/\"+project+\"/zones/\"+zone+\"/instances/\"+vmToStop+\"/stop\"}",
        "auth": {
          "type": "OAuth2"
        }
      },
      "result": "stopResult"
    }
  }
]
```

</td>
<td valign="top">

```json
{
    "id": "stopcomputeengine",
    "name": "Stop Compute Engine",
    "start": "DoStop",
    "states": [
        {
            "name": "DoStop",
            "type": "operation",
            "actions": [
                {
                    "functionRef": {
                        "refName": "StopComputeEngine",
                        "arguments": {
                            "project": "${ .project }",
                            "zone": "${ .zone }",
                            "vmToStop": "${ .vmToStop }"
                        }
                    }
                }
            ],
            "end": true
        }
    ],
    "functions": [
        {
            "name": "StopComputeEngine",
            "operation": "computeengineopenapi.json#stopengine"
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Google workflow defines its own REST service invocations inside the workflow language
where as Serverless Workflow utilizes the OpenAPI specification for REST service invocations.
The "operation" parameter in Serverless Workflow is an URI to an OpenAPI definition file which
contains all the information needed to invoke this service.
We assume that the values are passed to the Serverless Workflow as workflow data inputs.
Serverless Workflow has a designated "operation" state to perform operations such 
as service invocations, where as Google Workflow uses the "call" keyword.


### Error Handling For REST Service Invocation

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/connector_publish_pubsub.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
[
  {
    "initVariables": {
      "assign": [
        {
          "project": "${sys.get_env(\"GOOGLE_CLOUD_PROJECT_ID\")}"
        },
        {
          "topic": "mytopic1"
        },
        {
          "message": "Hello world!"
        }
      ]
    }
  },
  {
    "publish": {
      "try": {
        "call": "googleapis.pubsub.v1.projects.topics.publish",
        "args": {
          "topic": "${\"projects/\" + project + \"/topics/\" + topic}",
          "body": {
            "messages": [
              {
                "data": "${base64.encode(text.encode(message))}"
              }
            ]
          }
        },
        "result": "publishResult"
      },
      "except": {
        "as": "e",
        "steps": [
          {
            "handlePubSubError": {
              "switch": [
                {
                  "condition": "${e.code == 404}",
                  "raise": "PubSub Topic not found"
                },
                {
                  "condition": "${e.code == 403}",
                  "raise": "Error authenticating to PubSub"
                }
              ]
            }
          },
          {
            "unhandledException": {
              "raise": "${e}"
            }
          }
        ]
      }
    }
  },
  {
    "last": {
      "return": "${publishResult}"
    }
  }
]
```

</td>
<td valign="top">

```json
{
    "id": "publishtotopicwitherrorhandling",
    "name": "Publish To Topic With Error Handling",
    "start": "DoPublish",
    "states": [
        {
            "name": "DoPublish",
            "type": "operation",
            "actions": [
                {
                    "functionRef": {
                        "refName": "PublishToTopic",
                        "arguments": {
                            "project": "${ .project }",
                            "topic": "${ .topic }",
                            "message": "${ .message }"
                        }
                    }
                }
            ],
            "onErrors": [
                {
                    "error": "PubSub Topic not found",
                    "code": "404",
                    "end": {
                        "produceEvents": [
                            {
                                "eventRef": "TopicError",
                                "data": { "message": "PubSub Topic not found"}
                            }
                        ]
                    }
                },
                {
                    "error": "Error authenticating to PubSub",
                    "code": "403",
                    "end": {
                        "produceEvents": [
                            {
                                "eventRef": "TopicError",
                                "data": { "message": "Error authenticating to PubSub"}
                            }
                        ]
                    }
                },
                {
                    "error": "*",
                    "end": {
                        "produceEvents": [
                            {
                                "eventRef": "TopicError",
                                "data": { "message": "Error Performing PubSub"}
                            }
                        ]
                    }
                }
            ],
            "end": true
        }
    ],
    "functions": [
        {
            "name": "PublishToTopic",
            "operation": "pubsubapi.json#publish"
        }
    ],
    "events": [
        {
            "name": "TopicError",
            "source": "pubsub.topic.events",
            "type": "pubsub/events"
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

This example shows the differences of error handling approaches between the two languages.
We assumed here that the "raise" keyword used in the Google Workflow language completes workflow execution.
The biggest difference here is that with Serverless Workflow there is no specific way 
of "raising" or "throwing" a caught exception. [Error handling in Serverless Workflow](../specification.md#Workflow-Error-Handling) is explicit
meaning handling the error has to be defined within the workflow execution logic. 
Another difference is that with Serverless Workflow you can notify occurence of an error
to interested parties via events (CloudEvents specification format), which we are showing in this example.

### Retrying On Errors

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/error_retry_500.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
{
  "main": {
    "steps": [
      {
        "read_item": {
          "try": {
            "call": "http.get",
            "args": {
              "url": "https://host.com/api"
            },
            "result": "api_response"
          },
          "retry": {
            "predicate": "${custom_predicate}",
            "max_retries": 5,
            "backoff": {
              "initial_delay": 2,
              "max_delay": 60,
              "multiplier": 2
            }
          }
        }
      },
      {
        "last_step": {
          "return": "OK"
        }
      }
    ]
  },
  "custom_predicate": {
    "params": [
      "e"
    ],
    "steps": [
      {
        "what_to_repeat": {
          "switch": [
            {
              "condition": "${e.code == 500}",
              "return": true
            }
          ]
        }
      },
      {
        "otherwise": {
          "return": false
        }
      }
    ]
  }
}
```

</td>
<td valign="top">

```json
{
    "id": "errorhandlingwithretries",
    "name": "Error Handling with Retries",
    "start": "ReadItem",
    "states": [
        {
            "name": "ReadItem",
            "type": "operation",
            "actions": [
                {
                    "functionRef": "ReadItemFromApi"
                }
            ],
            "onErrors": [
                {
                    "error": "Service Not Available",
                    "code": "500",
                    "retryRef": "ServiceNotAvailableRetry",
                    "end": true
                }
            ],
            "end": true
        }
    ],
    "functions": [
        {
            "name": "ReadItemFromApi",
            "operation": "someapi.json#read"
        }
    ],
    "retries": [
        {
            "name": "ServiceNotAvailableRetry",
            "maxAttempts": 5,
            "delay": "PT2S",
            "maxDelay": "PT60S",
            "multiplier": 2
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Serverless Workflow defines [reusable retry definitions](../specification.md#Defining-Retries) which
can be referenced by one or many error definitions in states. Google Workflow seems to reference the 
error handlers in the "retry" statement as an expression/variable.

### Sub Workflows

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/subworkflow.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
{
  "main": {
    "steps": [
      {
        "first": {
          "call": "hello",
          "args": {
            "input": "Kristof"
          },
          "result": "someOutput"
        }
      },
      {
        "second": {
          "return": "${someOutput}"
        }
      }
    ]
  },
  "hello": {
    "params": [
      "input"
    ],
    "steps": [
      {
        "first": {
          "return": "${\"Hello \"+input}"
        }
      }
    ]
  }
}
```

</td>
<td valign="top">

```json
{
    "id": "callsubflow",
    "name": "Call SubFlow",
    "start": "CallSub",
    "states": [
        {
            "name": "CallSub",
            "type":"operation",
            "actions": [
              {
                "subFlowRef": "calledsubflow"
              }
            ],
            "end": true
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Serverless Workflow has a specific [SubFlow action](../specification.md#SubFlow-Action). By default the current workflow data
is passed to it, so there is no need to define specific arguments.
We have omitted the definition of "calledsubflow" as it is pretty straight forward. It would be 
a separate workflow definition with the "id" parameter set to "calledsubflow" in this example.

### Data Based Conditions

[Google Cloud Workflows Example](https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/step_conditional_jump.workflows.json)

<table>
<tr>
    <th>Google</th>
    <th>Serverless Workflow</th>
</tr>
<tr>
<td valign="top">

```json
[
  {
    "firstStep": {
      "call": "http.get",
      "args": {
        "url": "https://www.example.com/callA"
      },
      "result": "firstResult"
    }
  },
  {
    "whereToJump": {
      "switch": [
        {
          "condition": "${firstResult.body.SomeField < 10}",
          "next": "small"
        },
        {
          "condition": "${firstResult.body.SomeField < 100}",
          "next": "medium"
        }
      ],
      "next": "large"
    }
  },
  {
    "small": {
      "call": "http.get",
      "args": {
        "url": "https://www.example.com/SmallFunc"
      },
      "next": "end"
    }
  },
  {
    "medium": {
      "call": "http.get",
      "args": {
        "url": "https://www.example.com/MediumFunc"
      },
      "next": "end"
    }
  },
  {
    "large": {
      "call": "http.get",
      "args": {
        "url": "https://www.example.com/LargeFunc"
      },
      "next": "end"
    }
  }
]
```

</td>
<td valign="top">

```json
{
    "id": "databasedconditions",
    "name": "Data Based Conditions",
    "start": "CallA",
    "states": [
        {
            "name": "CallA",
            "type":"operation",
            "actions": [
                {
                    "functionRef": "callFunctionA"
                }
            ],
            "transition": "EvaluateAResults"
        },
        {
            "name": "EvaluateAResults",
            "type": "switch",
            "dataConditions": [
                {
                    "name": "Less than 10",
                    "condition": "${ .body |  .SomeField < 10 }",
                    "transition": "CallSmall"
                },
                {
                    "name": "Less than 100",
                    "condition": "${ .body |  .SomeField < 100 }",
                    "transition": "CallMedium"
                }
            ],
            "defaultCondition": {
                "transition": "CallLarge"
            }
        },
        {
            "name": "CallSmall",
            "type":"operation",
            "actions": [
                {
                    "functionRef": "callFunctionSmall"
                }
            ],
            "end": true
        },
        {
            "name": "CallMedium",
            "type":"operation",
            "actions": [
                {
                    "functionRef": "callFunctionMedium"
                }
            ],
            "end": true
        },
        {
            "name": "CallLarge",
            "type":"operation",
            "actions": [
                {
                    "functionRef": "callFunctionMedium"
                }
            ],
            "end": true
        }
    ],
    "functions": [
        {
            "name": "callFunctionA",
            "operation": "myapi.json#calla"
        },
        {
            "name": "callFunctionSmall",
            "operation": "myapi.json#callsmall"
        },
        {
            "name": "callFunctionMedium",
            "operation": "myapi.json#callmedium"
        },
        {
            "name": "callFunctionLarge",
            "operation": "myapi.json#calllarge"
        }
    ]
}
```

</td>
</tr>
</table>

#### Notes

Serverless Workflow has a specific [Switch state](../specification.md#Switch-State) which can handle both data-based as well as event-based 
conditions. Instead of hard-coding the REST invocation info in states, it has reusable
function definitions which can be referenced by one or many states.
