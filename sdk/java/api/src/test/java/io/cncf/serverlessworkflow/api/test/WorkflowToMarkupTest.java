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

import io.cncf.serverlessworkflow.api.Workflow;
import io.cncf.serverlessworkflow.api.end.End;
import io.cncf.serverlessworkflow.api.events.EventDefinition;
import io.cncf.serverlessworkflow.api.functions.Function;
import io.cncf.serverlessworkflow.api.interfaces.State;
import io.cncf.serverlessworkflow.api.start.Start;
import io.cncf.serverlessworkflow.api.states.DelayState;
import io.cncf.serverlessworkflow.api.test.utils.WorkflowTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static io.cncf.serverlessworkflow.api.states.DefaultState.Type.DELAY;

public class WorkflowToMarkupTest {
    @Test
    public void testSingleState() {

        Workflow workflow = new Workflow().withId("test-workflow").withName("test-workflow-name").withVersion("1.0")
                .withStates(Arrays.asList(
                        new DelayState().withName("delayState").withType(DELAY)
                                .withStart(
                                        new Start().withKind(Start.Kind.DEFAULT)
                                )
                                .withEnd(
                                        new End().withKind(End.Kind.DEFAULT)
                                )
                                .withTimeDelay("PT1M")
                        )
                );

        assertNotNull(workflow);
        assertEquals(1, workflow.getStates().size());
        State state = workflow.getStates().get(0);
        assertTrue(state instanceof DelayState);

        assertNotNull(WorkflowTestUtils.toJson(workflow));
        assertNotNull(WorkflowTestUtils.toYaml(workflow));
    }

    @Test
    public void testSingleFunction() {

        Workflow workflow = new Workflow().withId("test-workflow").withName("test-workflow-name").withVersion("1.0")
                .withFunctions(Arrays.asList(
                        new Function().withName("testFunction").withResource("testResource").withType("testType"))
                )
                .withStates(Arrays.asList(
                        new DelayState().withName("delayState").withType(DELAY)
                                .withStart(
                                        new Start().withKind(Start.Kind.DEFAULT)
                                )
                                .withEnd(
                                        new End().withKind(End.Kind.DEFAULT)
                                )
                                .withTimeDelay("PT1M")
                        )
                );

        assertNotNull(workflow);
        assertEquals(1, workflow.getStates().size());
        State state = workflow.getStates().get(0);
        assertTrue(state instanceof DelayState);
        assertNotNull(workflow.getFunctions());
        assertEquals(1, workflow.getFunctions().size());
        assertEquals("testFunction", workflow.getFunctions().get(0).getName());

        assertNotNull(WorkflowTestUtils.toJson(workflow));
        assertNotNull(WorkflowTestUtils.toYaml(workflow));
    }

    @Test
    public void testSingleEvent() {

        Workflow workflow = new Workflow().withId("test-workflow").withName("test-workflow-name").withVersion("1.0")
                .withEvents(Arrays.asList(
                        new EventDefinition().withName("testEvent").withSource("testSource").withType("testType"))
                )
                .withFunctions(Arrays.asList(
                        new Function().withName("testFunction").withResource("testResource").withType("testType"))
                )
                .withStates(Arrays.asList(
                        new DelayState().withName("delayState").withType(DELAY)
                                .withStart(
                                        new Start().withKind(Start.Kind.DEFAULT)
                                )
                                .withEnd(
                                        new End().withKind(End.Kind.DEFAULT)
                                )
                                .withTimeDelay("PT1M")
                        )
                );

        assertNotNull(workflow);
        assertEquals(1, workflow.getStates().size());
        State state = workflow.getStates().get(0);
        assertTrue(state instanceof DelayState);
        assertNotNull(workflow.getFunctions());
        assertEquals(1, workflow.getFunctions().size());
        assertEquals("testFunction", workflow.getFunctions().get(0).getName());
        assertNotNull(workflow.getEvents());
        assertEquals(1, workflow.getEvents().size());
        assertEquals("testEvent", workflow.getEvents().get(0).getName());

        assertNotNull(WorkflowTestUtils.toJson(workflow));
        assertNotNull(WorkflowTestUtils.toYaml(workflow));
    }
}
