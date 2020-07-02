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
