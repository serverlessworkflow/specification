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

import io.cncf.serverlessworkflow.api.Workflow;
import io.cncf.serverlessworkflow.api.interfaces.ExpressionEvaluator;
import io.cncf.serverlessworkflow.api.interfaces.Extension;
import io.cncf.serverlessworkflow.api.interfaces.WorkflowManager;
import io.cncf.serverlessworkflow.api.interfaces.WorkflowValidator;

public class TestWorkflowManager implements WorkflowManager {

    @Override
    public void setWorkflow(Workflow workflow) {

    }

    @Override
    public Workflow getWorkflow() {
        return null;
    }

    @Override
    public WorkflowManager setMarkup(String markup) {
        return this;
    }

    @Override
    public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {

    }

    @Override
    public void setDefaultExpressionEvaluator(String evaluatorName) {

    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator(String evaluatorName) {
        return null;
    }

    @Override
    public void resetExpressionValidator() {

    }

    @Override
    public WorkflowValidator getWorkflowValidator() {
        return null;
    }

    @Override
    public String toJson() {
        return null;
    }

    @Override
    public String toYaml() {
        return null;
    }

    @Override
    public Workflow toWorkflow(String json) {
        return null;
    }

    @Override
    public void registerExtension(String extensionId,
                                  Class<? extends Extension> extensionHandlerClass) {

    }
}
