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

package cloud.erda.agent.core.metrics;

import cloud.erda.agent.core.metrics.reporter.TelegrafSender;
import cloud.erda.agent.core.utils.PluginConstants;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.commons.datacarrier.DataCarrier;
import org.apache.skywalking.apm.commons.datacarrier.buffer.BufferStrategy;

public class MetricDispatcher implements BootService {

    private DataCarrier<Metric> dataCarrier;

    @Override
    public void prepare() throws Throwable {
        dataCarrier = new DataCarrier<Metric>("MSP_METRIC", "MSP_METRIC", 5, 10000, BufferStrategy.IF_POSSIBLE);
    }

    @Override
    public void boot() throws Throwable {
        dataCarrier.consume(new TelegrafSender(),1);
    }

    @Override
    public void complete() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        dataCarrier.shutdownConsumers();
    }

    @Override
    public String pluginName() {
        return PluginConstants.METRIC_PLUGIN;
    }

    public void dispatch(Metric... metrics) {
        for (Metric metric : metrics) {
            dataCarrier.produce(metric);
        }
    }
}
