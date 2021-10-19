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

package cloud.erda.agent.plugin.asf.dubbo;

import cloud.erda.agent.core.metrics.MetricProviderService;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.ReflectUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.common.threadpool.manager.DefaultExecutorRepository;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.apache.skywalking.apm.agent.core.boot.DependsOn;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author liuhaoyang
 * @date 2021/10/15 15:59
 */
@DependsOn(MetricProviderService.class)
public class DubboMetricsService extends ScheduledService {
    private static final String EXECUTOR_SERVICE_COMPONENT_KEY = ExecutorService.class.getName();
    private static boolean V2_7_5;
    private static boolean DUBBO_PROVIDER;

    static {
        try {
            Class.forName("org.apache.dubbo.common.extension.ExtensionFactory");
            DUBBO_PROVIDER = true;
        } catch (ClassNotFoundException e) {
            DUBBO_PROVIDER = false;
        }
        try {
            Class.forName("org.apache.dubbo.common.threadpool.manager.ExecutorRepository");
            V2_7_5 = true;
        } catch (ClassNotFoundException e) {
            V2_7_5 = false;
        }
    }

    private ILog logger;
    private Meter meter;
    private Attributes attributes;
    private List<Listener> listeners;

    @Override
    public void beforeBoot() throws Throwable {
        this.logger = LogManager.getLogger(DubboMetricsService.class);
        this.meter = ServiceManager.INSTANCE.findService(MetricProviderService.class).getMeter();
        this.listeners = initListeners();
        this.attributes = Attributes.of(AttributeKey.stringKey(Constants.Tags.COMPONENT), Constants.Tags.COMPONENT_DUBBO, AttributeKey.stringKey("type"), "ThreadPool", AttributeKey.stringKey("_metric_index"), "apm_component_dubbo");
    }

    @Override
    protected void executing() {
        if (!DUBBO_PROVIDER) {
            logger.info("not in apache-dubbo environment.");
            return;
        }
        if (V2_7_5) {
            // Version after 2.7.5. see issue https://github.com/apache/dubbo/issues/6625
            ExecutorRepository executorRepository = ExtensionLoader.getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
            if (executorRepository instanceof DefaultExecutorRepository) {
                DefaultExecutorRepository defaultExecutorRepository = (DefaultExecutorRepository) executorRepository;
                ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>> data = null;
                try {
                    data = (ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>>) ReflectUtils.getObjectField(defaultExecutorRepository, "data");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.info("Cannot read DefaultExecutorRepository.data field.");
                    throw new RuntimeException(e);
                }
                if (data != null) {
                    ConcurrentMap<Integer, ExecutorService> executors = data.get(CommonConstants.EXECUTOR_SERVICE_COMPONENT_KEY);
                    if (executors != null) {
                        for (Map.Entry<Integer, ExecutorService> entry : executors.entrySet()) {
                            ThreadPoolExecutor executor = (ThreadPoolExecutor) entry.getValue();
                            for (Listener listener : listeners) {
                                listener.accept(entry.getKey().toString(), executor, attributes);
                            }
                        }
                    }
                }
            }
        } else {
            // Compatible with before v2.7.5
            DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
            Map<String, Object> dataStoreExecutors = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
            for (Map.Entry<String, Object> entry : dataStoreExecutors.entrySet()) {
                if (entry.getValue() instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) entry.getValue();
                    for (Listener listener : listeners) {
                        listener.accept(entry.getKey(), executor, attributes);
                    }
                }
            }
        }
    }

    private List<Listener> initListeners() {
        List<Listener> listeners = new ArrayList<>();
        meter.gaugeBuilder("apm_dubbo_thread_pool_max_pool_size").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getMaximumPoolSize(), getThreadPoolAttributes(port, "max", attributes)));
        });
        meter.gaugeBuilder("apm_dubbo_thread_pool_core_pool_size").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getCorePoolSize(), getThreadPoolAttributes(port, "core", attributes)));
        });
        meter.gaugeBuilder("apm_dubbo_thread_pool_largest_pool_size").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getLargestPoolSize(), getThreadPoolAttributes(port, "largest", attributes)));
        });
        meter.gaugeBuilder("apm_dubbo_thread_pool_active_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getActiveCount(), getThreadPoolAttributes(port, "active_count", attributes)));
        });
        meter.gaugeBuilder("apm_dubbo_thread_pool_task_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getTaskCount(), getThreadPoolAttributes(port, "task_count", attributes)));
        });
        meter.gaugeBuilder("apm_dubbo_thread_pool_current").ofLongs().buildWithCallback(observableLongMeasurement -> {
            listeners.add((port, executor, attributes) -> observableLongMeasurement.observe(executor.getPoolSize(), getThreadPoolAttributes(port, "current", attributes)));
        });
        return listeners;
    }

    private Attributes getThreadPoolAttributes(String port, String field, Attributes parent) {
        return Attributes.builder().putAll(parent).put("thread_pool_port", port).put("field", field).put(cloud.erda.agent.core.utils.Constants.Metrics.FIELD_KEY, field).build();
    }

    public interface Listener {
        void accept(String port, ThreadPoolExecutor threadPoolExecutor, Attributes attributes);
    }
}
