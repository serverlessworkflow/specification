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
package io.serverlessworkflow.spi;

import io.serverlessworkflow.api.interfaces.WorkflowValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public class WorkflowValidatorProvider {
    private WorkflowValidator workflowValidator;

    private static Logger logger = LoggerFactory.getLogger(WorkflowValidatorProvider.class);

    public WorkflowValidatorProvider() {
        ServiceLoader<WorkflowValidator> foundWorkflowValidators = ServiceLoader.load(WorkflowValidator.class);
        Iterator<WorkflowValidator> it = foundWorkflowValidators.iterator();
        if (it.hasNext()) {
            workflowValidator = it.next();
            logger.info("Found workflow validator: " + workflowValidator.toString());
        }
    }

    private static class LazyHolder {

        static final WorkflowValidatorProvider INSTANCE = new WorkflowValidatorProvider();
    }

    public static WorkflowValidatorProvider getInstance() {
        return LazyHolder.INSTANCE;
    }

    public WorkflowValidator get() {
        return workflowValidator;
    }
}
