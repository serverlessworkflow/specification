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

import io.serverlessworkflow.api.interfaces.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public class WorkflowManagerProvider {
    private WorkflowManager workflowManager;

    private static Logger logger = LoggerFactory.getLogger(WorkflowManagerProvider.class);

    public WorkflowManagerProvider() {
        ServiceLoader<WorkflowManager> foundWorkflowManagers = ServiceLoader.load(WorkflowManager.class);
        Iterator<WorkflowManager> it = foundWorkflowManagers.iterator();
        if (it.hasNext()) {
            workflowManager = it.next();
            logger.info("Found workflow manager: " + workflowManager.toString());
        }
    }

    private static class LazyHolder {

        static final WorkflowManagerProvider INSTANCE = new WorkflowManagerProvider();
    }

    public static WorkflowManagerProvider getInstance() {
        return WorkflowManagerProvider.LazyHolder.INSTANCE;
    }

    public WorkflowManager get() {
        // always reset the manager validator and expression validator
        if (workflowManager.getWorkflowValidator() != null) {
            workflowManager.getWorkflowValidator().reset();
            workflowManager.resetExpressionValidator();
        }
        return workflowManager;
    }
}
