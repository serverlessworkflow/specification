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
package io.serverlessworkflow.spi.test.providers;

import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.interfaces.ExpressionEvaluator;

public class TestExpressionEvaluator implements ExpressionEvaluator {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public boolean evaluate(String expression,
                            EventDefinition eventDefinition) {
        return false;
    }
}
