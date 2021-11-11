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

package cloud.erda.agent.core.metrics.reporter;

import cloud.erda.agent.core.config.TelegrafProxyConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.metrics.MetricDispatcher;
import cloud.erda.agent.core.utils.GsonUtils;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.commons.datacarrier.consumer.IConsumer;

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
public class TelegrafSender implements IConsumer<Metric> {

    private static final int DEFAULT_BUCKET = 10;
    private static final int UDP_DATA_LIMIT = 1024 * 64 - 1;

    private static final ILog log = LogManager.getLogger(MetricDispatcher.class);

    private final InetSocketAddress socketAddress;
    private DatagramSocket socket;
    private boolean init;

    public TelegrafSender() {
        TelegrafProxyConfig config = ConfigAccessor.Default.getConfig(TelegrafProxyConfig.class);
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
    public void init(Properties properties) {
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
        byte[] data = GsonUtils.toBytes(buckets);
        if (data.length >= UDP_DATA_LIMIT) {
            send(buckets, Math.max(buckets.length / 2, 1));
            return;
        }
        try {
            socket.send(new DatagramPacket(data, 0, data.length, socketAddress));
            if (log.isDebugEnable()) {
                log.debug("Send {}({}KB) data to collector proxy success. \n {}", buckets.length, data.length / (float) 1024, new String(data));
            }
        } catch (IOException e) {
            if (log.isErrorEnable()) {
                log.error(e, "Send {}({}KB) data fail.", buckets.length, data.length / (float) 1024);
            }
        }
    }
}
