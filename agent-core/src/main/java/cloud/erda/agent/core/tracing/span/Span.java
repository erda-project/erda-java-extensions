/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.TracerContext;

import java.util.List;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-04 16:53
 **/
public interface Span {

    SpanContext getContext();

    String getOperationName();

    void updateName(String operationName);

    long getStartTime();

    long getEndTime();

    Map<String, String> getTags();

    void tag(String key, String value);

    SpanLog log(Long timestamp);

    List<SpanLog> getLogs();

    void finish();
}
