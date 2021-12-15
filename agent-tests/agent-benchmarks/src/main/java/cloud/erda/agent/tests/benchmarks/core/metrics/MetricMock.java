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

package cloud.erda.agent.tests.benchmarks.core.metrics;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.DateTime;

/**
 * @author liuhaoyang
 * @date 2021/11/30 10:25
 */
public class MetricMock {

    public final static Metric[] Mocks = new Metric[1];

    static {
        Metric metric = Metric.New("mock", DateTime.currentTimeNano());
        metric.addTag("terminus_key", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("project_name", "project1");
        metric.addTag("application_name", "app1");
        metric.addTag("service_name", "svc1");
        metric.addTag("service_instance_id", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("service_instance_ip", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("runtime_name", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("runtime_id", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("workspace", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("terminus_key", "xxx-xxxxx-xxx-xxxxxx");
        metric.addTag("org_name", "org1");
        metric.addField("count", 1);
        metric.addField("rt", 100.21);
        Mocks[0] = metric;
    }
}
