/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.cncf.serverlessworkflow.spi.test.providers;

import io.cncf.serverlessworkflow.api.interfaces.WorkflowManager;
import io.cncf.serverlessworkflow.api.interfaces.WorkflowValidator;

public class TestWorkflowValidator implements WorkflowValidator {

    @Override
    public WorkflowValidator setWorkflowManager(WorkflowManager workflowManager) {
        return this;
    }

    @Override
    public void setJson(String json) {

    }

    @Override
    public void setYaml(String yaml) {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setSchemaValidationEnabled(boolean schemaValidationEnabled) {

    }

    @Override
    public void setStrictValidationEnabled(boolean strictValidationEnabled) {

    }

    @Override
    public void reset() {

    }
}
