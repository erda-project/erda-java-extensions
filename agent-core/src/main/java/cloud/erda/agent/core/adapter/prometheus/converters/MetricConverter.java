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

package cloud.erda.agent.core.adapter.prometheus.converters;

import cloud.erda.agent.core.metric.Metric;
import io.prometheus.client.Collector.MetricFamilySamples;

import java.util.List;

/**
 * @author liuhaoyang 2020/3/19 15:55
 */
public interface MetricConverter {
    List<Metric> convert(MetricFamilySamples metricFamilySamples, long timestamp);
}
