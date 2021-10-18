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

package cloud.erda.agent.plugin.tomcat.metric;

import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.Numbers;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author liuhaoyang
 * @date 2021/10/13 15:40
 */
public class TomcatStatsCollector {

    private static final String JMX_DOMAIN_EMBEDDED = "Tomcat";
    private static final String OBJECT_NAME_THREAD_POOL_SUFFIX = ":type=ThreadPool,name=*";
    private static final String OBJECT_NAME_THREAD_POOL_EMBEDDED = JMX_DOMAIN_EMBEDDED + OBJECT_NAME_THREAD_POOL_SUFFIX;

    public static MBeanServer getMBeanServer() {
        List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if (!mBeanServers.isEmpty()) {
            return mBeanServers.get(0);
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    private final MBeanServer mBeanServer;
    private final Meter meter;
    private final Map<ObjectName, List<BiConsumer<ObjectName, Attributes>>> metricListeners;
    private final Set<NotificationListener> notificationListeners;

    public TomcatStatsCollector(Meter meter) {
        this.meter = meter;
        this.mBeanServer = getMBeanServer();
        this.metricListeners = new LinkedHashMap<>();
        this.notificationListeners = ConcurrentHashMap.newKeySet();

        this.registerGlobalRequestMetrics();
        this.registerThreadPoolMetrics();
    }

    private void registerGlobalRequestMetrics() {
        registerMetricsEventually(JMX_DOMAIN_EMBEDDED + ":type=GlobalRequestProcessor,name=*", objectName -> {
            List<BiConsumer<ObjectName, Attributes>> listeners = metricListeners.computeIfAbsent(objectName, k -> new ArrayList<>());
            this.meter.counterBuilder("apm_tomcat_global_request_bytes_sent").buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "bytesSent"), getGlobalRequestAttributes(name, "bytes_sent", attributes)));
            });
            this.meter.counterBuilder("apm_tomcat_global_request_bytes_received").buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "bytesReceived"), getGlobalRequestAttributes(name, "bytes_received", attributes)));
            });
            this.meter.counterBuilder("apm_tomcat_global_request_error_count").buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "errorCount"), getGlobalRequestAttributes(name, "error_count", attributes)));
            });
            this.meter.counterBuilder("apm_tomcat_global_request_request_count").buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "requestCount"), getGlobalRequestAttributes(name, "request_count", attributes)));
            });
        });
    }

    private void registerThreadPoolMetrics() {
        registerMetricsEventually(JMX_DOMAIN_EMBEDDED + ":type=ThreadPool,name=*", objectName -> {
            List<BiConsumer<ObjectName, Attributes>> listeners = metricListeners.computeIfAbsent(objectName, k -> new ArrayList<>());
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_max_threads").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "maxThreads"), getThreadPoolAttributes(name, "max_threads", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_threads_busy").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "currentThreadsBusy"), getThreadPoolAttributes(name, "threads_busy", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_connection_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "connectionCount"), getThreadPoolAttributes(name, "connection_count", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_accept_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "acceptCount"), getThreadPoolAttributes(name, "accept_count", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_max_connections").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "maxConnections"), getThreadPoolAttributes(name, "max_connections", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_keep_alive_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "keepAliveCount"), getThreadPoolAttributes(name, "keep_alive_count", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_acceptor_thread_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "acceptorThreadCount"), getThreadPoolAttributes(name, "acceptor_thread_count", attributes)));
            });
            this.meter.gaugeBuilder("apm_tomcat_thread_pool_poller_thread_count").ofLongs().buildWithCallback(observableLongMeasurement -> {
                listeners.add((name, attributes) ->
                        observableLongMeasurement.observe(this.getMBeanAttribute(name, "pollerThreadCount"), getThreadPoolAttributes(name, "poller_thread_count", attributes)));
            });
        });
    }

    public void collect() {
        for (Map.Entry<ObjectName, List<BiConsumer<ObjectName, Attributes>>> item : metricListeners.entrySet()) {
            ObjectName objectName = item.getKey();
            String type = objectName.getKeyProperty("type");
            Attributes attributes = Attributes.of(AttributeKey.stringKey(Constants.Tags.COMPONENT), Constants.Tags.COMPONENT_TOMCAT, AttributeKey.stringKey("type"), type, AttributeKey.stringKey("_metric_index"), "apm_component_tomcat");
            for (BiConsumer<ObjectName, Attributes> listener : item.getValue()) {
                listener.accept(objectName, attributes);
            }
        }
    }

    private void registerMetricsEventually(String patternSuffix, Consumer<ObjectName> perObject) {
        ObjectName objectName = null;
        try {
            objectName = new ObjectName(patternSuffix);
        } catch (MalformedObjectNameException exception) {
            throw new RuntimeException(exception);
        }
        Set<ObjectName> objectNames = this.mBeanServer.queryNames(objectName, null);
        if (!objectNames.isEmpty()) {
            // MBeans are present, so we can register metrics now.
            objectNames.forEach(perObject);
        } else {
            ObjectName readonlyObjName = objectName;
            NotificationListener notificationListener = new NotificationListener() {
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    MBeanServerNotification mBeanServerNotification = (MBeanServerNotification) notification;
                    ObjectName notificationObjName = mBeanServerNotification.getMBeanName();
                    perObject.accept(notificationObjName);
                    if (readonlyObjName.isPattern()) {
                        // patterns can match multiple MBeans so don't remove listener
                        return;
                    }
                    try {
                        mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
                        notificationListeners.remove(this);
                    } catch (InstanceNotFoundException | ListenerNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            notificationListeners.add(notificationListener);

            NotificationFilter notificationFilter = notification -> {
                if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
                    return false;
                }

                // we can safely downcast now
                ObjectName notificationObjName = ((MBeanServerNotification) notification).getMBeanName();
                try {
                    return new ObjectName(OBJECT_NAME_THREAD_POOL_EMBEDDED).apply(notificationObjName);
                } catch (MalformedObjectNameException ignored) {
                }
                return false;
            };

            try {
                mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener, notificationFilter, null);
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException("Error registering MBean listener", e);
            }
        }
    }

    private long getMBeanAttribute(ObjectName name, String attribute) {
        return Numbers.safeLong(() -> this.mBeanServer.getAttribute(name, attribute));
    }

    private Attributes getThreadPoolAttributes(ObjectName name, String attribute, Attributes parent) {
        return Attributes.builder().putAll(parent).put("thread_pool", name.getKeyProperty("name").replace("\"", "")).put("field", attribute).put(Constants.Metrics.FIELD_KEY, attribute).build();
    }

    private Attributes getGlobalRequestAttributes(ObjectName name, String attribute, Attributes parent) {
        return Attributes.builder().putAll(parent).put("global_request_processor", name.getKeyProperty("name").replace("\"", "")).put("field", attribute).put(Constants.Metrics.FIELD_KEY, attribute).build();
    }
}
