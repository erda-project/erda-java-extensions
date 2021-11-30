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

package cloud.erda.agent.core.metrics.exporters.udp;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.metrics.MetricBuckets;
import cloud.erda.agent.core.metrics.exporters.Exporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporterFactory;
import cloud.erda.agent.core.metrics.serializers.JsonSerializer;
import cloud.erda.agent.core.metrics.serializers.MetricSerializer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Properties;

/**
 * @author liuhaoyang
 * @date 2021/10/20 17:28
 */
public class UdpExporterFactory extends MetricExporterFactory<UdpExporterFactory.UdpExporter, UdpExportConfig> {

    @Exporter("UDP")
    @Override
    public UdpExporter create(UdpExportConfig config) {
        return new UdpExporter(config);
    }

    @Override
    public void shutdown(UdpExporter metricExporter) {
    }

    public static class UdpExporter implements MetricExporter {

        private static final int DEFAULT_BUCKET = 1;
        private static final int UDP_DATA_LIMIT = 1024 * 64 - 1;

        private static final ILog log = LogManager.getLogger(UdpExporter.class);

        private final MetricSerializer serializer = new JsonSerializer();
        private InetSocketAddress socketAddress;
        private DatagramSocket socket;
        private boolean init;
        private UdpExportConfig config;

        public UdpExporter(UdpExportConfig config) {
            this.config = config;
        }

        @Override
        public void init(Properties properties) {
            this.socketAddress = new InetSocketAddress(config.getHost(), config.getHostPort());
            log.info("Telegraf proxy addr " + socketAddress.toString());
            try {
                this.socket = new DatagramSocket();
                init = true;
            } catch (SocketException e) {
                init = false;
                log.error("Bind udp client address fail.", e);
            }
        }

        @Override
        public void consume(List<Metric> data) {
            this.send(data.toArray(new Metric[0]), DEFAULT_BUCKET);
        }

        @Override
        public void onError(List<Metric> data, Throwable t) {
            log.error(t, "Try to send {} metrics to proxy, with unexpected exception.", data.size());
        }

        @Override
        public void onExit() {
            socket.close();
        }

        public void send(Metric[] metrics, int bucket) {
            if (!init) {
                return;
            }
            if (metrics.length <= 0) {
                return;
            }

            if (metrics.length <= bucket) {
                doSend(metrics);
            } else {
                MetricBuckets metricBuckets = new MetricBuckets(metrics, bucket);
                for (Metric[] buckets : metricBuckets) {
                    doSend(buckets);
                }
            }
        }

        private void doSend(Metric[] buckets) {
            byte[] data = serializer.serializeBytes(buckets);
            try {
                socket.send(new DatagramPacket(data, 0, data.length, socketAddress));
                if (log.isDebugEnable()) {
                    log.debug("Send {}({}KB) data to udp backend success. \n {}", buckets.length, data.length / (float) 1024, new String(data));
                }
            } catch (IOException e) {
                if (log.isErrorEnable()) {
                    log.error(e, "Send {}({}KB) data fail.", buckets.length, data.length / (float) 1024);
                }
            }
        }
    }
}
