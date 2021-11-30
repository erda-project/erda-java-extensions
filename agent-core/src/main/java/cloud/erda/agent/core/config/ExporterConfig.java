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

package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @date 2021/11/29 17:45
 */
public class ExporterConfig implements Config {

    @Configuration(name = "MSP_EXPORTER", defaultValue = "UDP")
    private String exporter;

    @Configuration(name = "MSP_METRIC_EXPORTER_PARALLELISM", defaultValue = "2")
    private Integer exporterParallelism;

    public String getMetricExporter() {
        return exporter;
    }

    public Integer getExporterParallelism() {
        return exporterParallelism;
    }
}
