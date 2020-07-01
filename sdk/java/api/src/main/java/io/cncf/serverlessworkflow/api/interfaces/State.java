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
package io.cncf.serverlessworkflow.api.interfaces;

import io.cncf.serverlessworkflow.api.end.End;
import io.cncf.serverlessworkflow.api.error.Error;
import io.cncf.serverlessworkflow.api.filters.StateDataFilter;
import io.cncf.serverlessworkflow.api.retry.Retry;
import io.cncf.serverlessworkflow.api.start.Start;
import io.cncf.serverlessworkflow.api.states.DefaultState.Type;
import io.cncf.serverlessworkflow.api.transitions.Transition;

import java.util.List;
import java.util.Map;

public interface State {

    String getId();

    String getName();

    Type getType();

    Start getStart();

    End getEnd();

    StateDataFilter getStateDataFilter();

    String getDataInputSchema();

    String getDataOutputSchema();

    Transition getTransition();

    List<Error> getOnError();

    List<Retry> getRetry();

    Map<String, String> getMetadata();
}