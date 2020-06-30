package v1alpha2

import (
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// Condition describes a general structure for Kubernetes based resources
type Condition struct {
	// Type of this condition
	Type string `json:"type"`
	// Status ...
	Status corev1.ConditionStatus `json:"status"`
	// LastTransitionTime ...
	LastTransitionTime metav1.Time `json:"lastTransitionTime,omitempty"`
	// Reason of this condition
	Reason string `json:"reason,omitempty"`
	// Message ...
	Message string `json:"message,omitempty"`
}
