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

import cloud.erda.agent.core.config.ExporterConfig;
import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.metrics.exporters.Exporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporterFactory;
import cloud.erda.agent.core.utils.PluginConstants;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author liuhaoyang
 * @date 2021/11/29 17:08
 */
public class MetricExporterService implements BootService {

    private static final ILog LOGGER = LogManager.getLogger(MetricExporterService.class);

    private final Map<String, MetricExporterFactory<?, ?>> exporterFactory = new HashMap<>();
    private final Map<String, MetricExporter> activeExporters = new HashMap<>();


    @Override
    public void prepare() throws Throwable {
    }

    @Override
    public void boot() throws Throwable {
        ServiceLoader<MetricExporterFactory> serviceLoader = ServiceLoader.load(MetricExporterFactory.class, AgentClassLoader.getDefault());
        for (MetricExporterFactory factory : serviceLoader) {
            try {
                Class<?> factoryClazz = factory.getClass();
                Method[] methods = factoryClazz.getDeclaredMethods();
                LOGGER.info("Finding exporter from {}", factoryClazz);
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Exporter.class)) {
                        Exporter exporter = method.getAnnotation(Exporter.class);
                        if (exporter == null) {
                            LOGGER.warn("Ignore {}, because exporter annotation cannot be obtained", factoryClazz);
                            continue;
                        }
                        LOGGER.info("Load MetricExporterFactory \"{}\" by {}", exporter.value(), factoryClazz);
                        exporterFactory.put(exporter.value(), factory);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error(ex, "Load MetricExporterFactory {} error", factory);
            }
        }
    }

    @Override
    public void complete() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        synchronized (activeExporters) {
            for (Map.Entry<String, MetricExporter> entry : activeExporters.entrySet()) {
                MetricExporterFactory factory = exporterFactory.get(entry.getKey());
                if (factory != null) {
                    try {
                        factory.shutdown(entry.getValue());
                    } catch (Exception ex) {
                        LOGGER.error(ex, "Export {} shutdown error", entry.getKey());
                    }
                }
            }
        }
    }

    @Override
    public String pluginName() {
        return PluginConstants.METRIC_PLUGIN;
    }

    public MetricExporter create(ExporterConfig exporterConfig) {
        synchronized (activeExporters) {
            String exporterName = exporterConfig.getMetricExporter();
            MetricExporter exporter = activeExporters.get(exporterName);
            if (exporter != null) {
                return exporter;
            }
            MetricExporterFactory factory = exporterFactory.get(exporterName);
            if (factory == null) {
                LOGGER.warn("Cannot find factory for MetricExport {}.", exporterConfig.getMetricExporter());
                return null;
            }
            try {
                Class<?> factoryClazz = factory.getClass();
                Method[] methods = factoryClazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Exporter.class)) {
                        Exporter annotation = method.getAnnotation(Exporter.class);
                        if (annotation != null && annotation.value().equals(exporterName)) {
                            Class<?> configClass = method.getParameterTypes()[0];
                            Config config = (Config) ConfigAccessor.Default.getConfig(configClass);
                            exporter = factory.create(config);
                            activeExporters.put(exporterName, exporter);
                            LOGGER.info("Create MetricExporter {}={} by {}", exporterName, exporter.getClass().getName(), factoryClazz.getName());
                            return exporter;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e, "Create MetricExport {} error", exporterConfig.getMetricExporter());
            }
            return null;
        }
    }
}
