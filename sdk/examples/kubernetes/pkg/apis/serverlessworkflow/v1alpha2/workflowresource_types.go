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
