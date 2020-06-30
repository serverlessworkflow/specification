package v1alpha2

import (
	sw "github.com/cncf/wg-serverless-workflow/sdk/go/pkg/apis/serverlessworkflow"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EventGroupSpec defines the desired state of EventGroup
type EventGroupSpec struct {
	// List of events deployed for this instance. An event name must be unique in the given namespace.
	// +kubebuilder:validation:MinItems=1
	// +listType=atomic
	Events []sw.Eventdef `json:"events"`
}

// EventGroupStatus defines the observed state of EventGroup
type EventGroupStatus struct {
	// +listType=atomic
	// Actual status conditions for the WorkflowDefinition instance
	Conditions []Condition `json:"conditions,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// EventGroup defines the structure for an event infrastructure that can be consumed or produced by a WorkflowDefinition instance
// +kubebuilder:subresource:status
// +kubebuilder:resource:path=eventgroups,scope=Namespaced
type EventGroup struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   EventGroupSpec   `json:"spec,omitempty"`
	Status EventGroupStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// EventGroupList contains a list of EventGroup
type EventGroupList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []EventGroup `json:"items"`
}

func init() {
	SchemeBuilder.Register(&EventGroup{}, &EventGroupList{})
}
