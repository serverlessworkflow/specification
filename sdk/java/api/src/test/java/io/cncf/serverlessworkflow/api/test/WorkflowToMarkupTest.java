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
package io.cncf.serverlessworkflow.api.test;

import io.cncf.serverlessworkflow.api.end.End;
import io.cncf.serverlessworkflow.api.start.Start;
import io.cncf.serverlessworkflow.api.states.DefaultState;
import io.cncf.serverlessworkflow.api.test.utils.WorkflowTestUtils;
import org.junit.jupiter.api.Test;

import io.cncf.serverlessworkflow.api.Workflow;
import io.cncf.serverlessworkflow.api.mapper.JsonObjectMapper;
import io.cncf.serverlessworkflow.api.mapper.YamlObjectMapper;
import io.cncf.serverlessworkflow.api.states.DelayState;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkflowToMarkupTest {

    private static String testDelayStateYaml = "id: \"test-workflow\"\n" +
            "name: \"test-workflow-name\"\n" +
            "version: \"1.0\"\n" +
            "events: []\n" +
            "functions: []\n" +
            "states:\n" +
            "- timeDelay: \"PT1M\"\n" +
            "  name: \"delayState\"\n" +
            "  type: \"delay\"\n" +
            "  start:\n" +
            "    kind: \"default\"\n" +
            "  end:\n" +
            "    kind: \"default\"\n" +
            "  onError: []\n" +
            "  retry: []\n";

    private static String testDelayStateJson = "{\n" +
            "  \"id\" : \"test-workflow\",\n" +
            "  \"name\" : \"test-workflow-name\",\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"events\" : [ ],\n" +
            "  \"functions\" : [ ],\n" +
            "  \"states\" : [ {\n" +
            "    \"timeDelay\" : \"PT1M\",\n" +
            "    \"name\" : \"delayState\",\n" +
            "    \"type\" : \"delay\",\n" +
            "    \"start\" : {\n" +
            "      \"kind\" : \"default\"\n" +
            "    },\n" +
            "    \"end\" : {\n" +
            "      \"kind\" : \"default\"\n" +
            "    },\n" +
            "    \"onError\" : [ ],\n" +
            "    \"retry\" : [ ]\n" +
            "  } ]\n" +
            "}";

    @Test
    public void testSingleDelayState() {

        Workflow workflow = new Workflow().withId("test-workflow").withName("test-workflow-name").withVersion("1.0")
                .withStates(Arrays.asList(
                        new DelayState().withName("delayState").withType(DefaultState.Type.DELAY).withStart(
                                new Start().withKind(Start.Kind.DEFAULT)
                        )
                                .withEnd(
                                        new End().withKind(End.Kind.DEFAULT)
                                )
                                .withTimeDelay("PT1M")
                        )
                );

        assertNotNull(workflow);
        assertEquals(testDelayStateJson, WorkflowTestUtils.toJson(workflow));
        assertEquals(testDelayStateYaml, WorkflowTestUtils.toYaml(workflow));
    }
}
