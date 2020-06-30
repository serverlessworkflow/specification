module github.com/cncf/wg-serverless-workflow/sdk/kubernetes/example

go 1.13

require (
	github.com/RHsyseng/operator-utils v0.0.0-20200417214513-7aac0c82a293
	github.com/cncf/wg-serverless-workflow/sdk/go v0.0.0-20200630210910-fed20b3e7be1
	github.com/go-openapi/runtime v0.19.19 // indirect
	github.com/stretchr/testify v1.6.1
	golang.org/x/net v0.0.0-20200625001655-4c5254603344 // indirect
	golang.org/x/sys v0.0.0-20200625212154-ddb9806d33ae // indirect
	golang.org/x/text v0.3.3 // indirect
	gopkg.in/yaml.v3 v3.0.0-20200615113413-eeeca48fe776
	k8s.io/api v0.18.2
	k8s.io/apimachinery v0.18.5
	k8s.io/kube-openapi v0.0.0-20190816220812-743ec37842bf
	sigs.k8s.io/controller-runtime v0.4.0
)

// Pinned to kubernetes-1.16.2
replace (
	k8s.io/api => k8s.io/api v0.0.0-20191016110408-35e52d86657a
	k8s.io/apimachinery => k8s.io/apimachinery v0.0.0-20191004115801-a2eda9f80ab8
)
