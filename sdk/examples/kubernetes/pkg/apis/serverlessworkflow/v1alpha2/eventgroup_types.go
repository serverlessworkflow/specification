// Copyright 2020 The Serverless Workflow Specification Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
