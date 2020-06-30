package v1alpha2

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// WorkflowResourceSpec defines the desired state of WorkflowResource
type WorkflowResourceSpec struct {
	// Resource (URI) for a serverless workflow instance to be deployed
	Resource string `json:"resource,omitempty"`
}

// WorkflowResourceStatus defines the observed state of WorkflowResource
type WorkflowResourceStatus struct {
	// +listType=atomic
	// Actual status conditions for the WorkflowDefinition instance
	Conditions []Condition `json:"conditions,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// WorkflowResource represents a Serverless Workflow instance deployment on a Kubernetes cluster.
// It has a reference for a workflow resource file needed for a Kubernetes native application to deploy a Serverless Workflow instance in the cluster.
// +kubebuilder:subresource:status
// +kubebuilder:resource:path=workflowresources,scope=Namespaced
type WorkflowResource struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   WorkflowResourceSpec   `json:"spec,omitempty"`
	Status WorkflowResourceStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// WorkflowDefinitionList contains a list of WorkflowDefinition
type WorkflowResourceList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []WorkflowDefinition `json:"items"`
}

func init() {
	SchemeBuilder.Register(&WorkflowResource{}, &WorkflowResourceList{})
}
