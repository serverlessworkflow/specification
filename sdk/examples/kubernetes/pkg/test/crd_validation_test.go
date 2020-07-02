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

package test

import (
	"fmt"
	"github.com/RHsyseng/operator-utils/pkg/validation"
	"github.com/stretchr/testify/assert"
	"gopkg.in/yaml.v3"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

const (
	crdsPath = "../../deploy/crds/"
)

type crdValidation struct {
	crdFile string
	crFile  string
	schema  validation.Schema
}

func TestSampleCustomResources(t *testing.T) {
	for _, crd := range assertAndGetCRDs(t) {
		yamlString, err := ioutil.ReadFile(crd.crFile)
		assert.NoError(t, err, "Error reading %v CR yaml", crd.crFile)
		var input map[string]interface{}
		assert.NoError(t, yaml.Unmarshal(yamlString, &input))
		assert.NoError(t, crd.schema.Validate(input), "File %v does not validate against the CRD schema", crd.crFile)
	}
}

func assertAndGetCRDs(t *testing.T) (crds map[string]*crdValidation) {
	dirPath, err := filepath.Abs(crdsPath)
	assert.NoError(t, err)
	crds = map[string]*crdValidation{}
	err = filepath.Walk(dirPath, func(path string, info os.FileInfo, err error) error {
		if info.IsDir() {
			return nil
		}
		key := mustGetCRDTypeName(info.Name())
		if _, ok := crds[key]; !ok {
			crds[key] = &crdValidation{}
		}
		crd := crds[key]
		if strings.HasSuffix(info.Name(), "_crd.yaml") {
			crd.crdFile = path
			crd.schema = assertAndGetSchema(t, path)
		}
		if strings.HasSuffix(info.Name(), "_cr.yaml") {
			crd.crFile = path
		}

		return err
	})
	assert.NoError(t, err)
	assert.NotEmpty(t, crds)
	return crds
}

func assertAndGetSchema(t *testing.T, crdFile string) validation.Schema {
	yamlString, err := ioutil.ReadFile(crdFile)
	assert.NoError(t, err, "Error reading CRD yaml %v", yamlString)
	schema, err := validation.New(yamlString)
	assert.NoError(t, err)
	return schema
}

func mustGetCRDTypeName(filename string) string {
	fileSlice := strings.Split(filename, "_")
	if len(fileSlice) == 3 { // crd
		return fileSlice[1]
	} else if len(fileSlice) == 4 {
		return fileSlice[2]
	}
	panic(fmt.Sprintf("Filename not in the expected format (expecting CR or CRD yaml file), got: %s", filename))
}
