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

package cloud.erda.agent.plugin.microservice;

import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import cloud.erda.agent.core.utils.AddonTypeManager;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author randomnil
 */
public class MicroServiceScheduledService extends ScheduledService {

    @Override
    protected void executing() {
        for (String addonType : AddonTypeManager.INSTANCE.getAddonTypeSet()) {
            TransactionMetricBuilder transactionMetricBuilder = TransactionMetricUtils.createMiroServiceMetric(addonType);
            MetricReporter.report(transactionMetricBuilder);
        }
    }

    @Override
    protected long initialDelay() {
        return 1;
    }

    @Override
    protected long period() {
        return 5;
    }

    @Override
    protected TimeUnit timeUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    public String pluginName() {
        return "microservice";
    }

    @Override
    public boolean defaultEnable() {
        return false;
    }
}