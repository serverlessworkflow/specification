package v1alpha2

import (
	sw "github.com/cncf/wg-serverless-workflow/sdk/go/pkg/apis/serverlessworkflow"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// FunctionGroupSpec defines the desired state of FunctionGroup
type FunctionGroupSpec struct {
	// List of functions deployed for this instance. A function name must be unique in the given namespace.
	// +kubebuilder:validation:MinItems=1
	// +listType=atomic
	Functions []sw.Function `json:"functions"`
}

// FunctionGroupStatus defines the observed state of FunctionGroup
type FunctionGroupStatus struct {
	// Actual status conditions for the FunctionGroup instance
	Conditions []Condition `json:"conditions,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

//  FunctionGroup defines the structure for a function infrastructure that can be called by a WorkflowDefinition instance
// +kubebuilder:subresource:status
// +kubebuilder:resource:path=functiongroups,scope=Namespaced
type FunctionGroup struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   FunctionGroupSpec   `json:"spec,omitempty"`
	Status FunctionGroupStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// FunctionGroupList contains a list of FunctionGroup
type FunctionGroupList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []EventGroup `json:"items"`
}

func init() {
	SchemeBuilder.Register(&FunctionGroup{}, &FunctionGroupList{})
}
