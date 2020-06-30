# Serverless Specification Go SDK

In this directory you will find all the [specification types](https://github.com/cncf/wg-serverless-workflow/tree/master/specification/schema) defined by our schemas in Go.

Some types defined by the specification can be generic objects (such as [`Extensions`](https://github.com/cncf/wg-serverless-workflow/tree/master/specification#Extending)) 
or share a minimum interface, like [`States`](https://github.com/cncf/wg-serverless-workflow/tree/master/specification#State-Definition). 

In cases like these, we've decided to represent their types as the Kubernetes type [`RawExtension`](https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/#rawextension).
This way the Serverless Workflow types can be Kubernetes friendly, to make it easy for one to
use this package when developing Kubernetes applications.

## How to use

Run the following command in the root of your Go's project:

```shell script
$ go get -u github.com/cncf/wg-serverless-workflow/sdk/go
```

Your `go.mod` file should be updated to add a dependency from the Serverless Workflow specification.

To use the generated types, import the package in your go file like this:

```go
package mypackage

import "github.com/cncf/wg-serverless-workflow/sdk/go/pkg/apis/serverlessworkflow"
```

Then just reference the package within your file like `myfunction := serverlessworkflow.Function{}`.

