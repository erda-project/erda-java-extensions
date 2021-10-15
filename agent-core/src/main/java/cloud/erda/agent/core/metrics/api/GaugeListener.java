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

package cloud.erda.agent.core.metrics.api;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.Meter;

import java.util.function.Consumer;

/**
 * @author liuhaoyang
 * @date 2021/10/14 12:09
 */
public class GaugeListener {

    private final DoubleGaugeBuilder doubleGaugeBuilder;

    public GaugeListener(Meter meter, String name) {
        this.doubleGaugeBuilder = meter.gaugeBuilder(name);
    }

    public void addCallback(Consumer<ObservableMeasurement> consumer) {
        this.doubleGaugeBuilder.buildWithCallback(x -> consumer.accept(x::observe));
    }
}
