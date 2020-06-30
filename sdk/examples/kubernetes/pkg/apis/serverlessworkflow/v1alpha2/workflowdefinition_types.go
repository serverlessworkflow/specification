package v1alpha2

import (
	sw "github.com/cncf/wg-serverless-workflow/sdk/go/pkg/apis/serverlessworkflow"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// WorkflowDefinitionSpec defines the desired state of WorkflowDefinition
type WorkflowDefinitionSpec struct {
	sw.Workflow `json:",inline"`
}

// WorkflowDefinitionStatus defines the observed state of WorkflowDefinition
type WorkflowDefinitionStatus struct {
	// +listType=atomic
	// Actual status conditions for the WorkflowDefinition instance
	Conditions []Condition `json:"conditions,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// WorkflowDefinition represents a Serverless Workflow instance deployment on a Kubernetes cluster.
// It has every information needed for a Kubernetes native application to deploy a Serverless Workflow instance in the cluster.
// +kubebuilder:subresource:status
// +kubebuilder:resource:path=workflowdefinitions,scope=Namespaced
type WorkflowDefinition struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   WorkflowDefinitionSpec   `json:"spec,omitempty"`
	Status WorkflowDefinitionStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// WorkflowDefinitionList contains a list of WorkflowDefinition
type WorkflowDefinitionList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []WorkflowDefinition `json:"items"`
}

func init() {
	SchemeBuilder.Register(&WorkflowDefinition{}, &WorkflowDefinitionList{})
}
