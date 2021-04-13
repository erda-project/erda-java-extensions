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

package cloud.erda.agent.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Counter;
import cloud.erda.agent.plugin.monitor.sdk.metrics.AutoResetCounter;

/**
 * @author liuhaoyang 2020/3/18 20:09
 */
public class CounterWrapper extends BaseWrapper<AutoResetCounter> implements Counter {

    public CounterWrapper(AutoResetCounter metric) {
        super(metric);
    }

    @Override
    public void add(double value) {
        this.metric.inc(value);
    }

    @Override
    public void inc() {
        this.metric.inc();
    }

    @Override
    public void add(double value, String[] args) {
        this.metric.labels(args).inc(value);
    }

    @Override
    public void inc(String[] args) {
        this.metric.labels(args).inc();
    }
}
