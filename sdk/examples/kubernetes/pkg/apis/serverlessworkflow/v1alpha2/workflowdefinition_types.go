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
