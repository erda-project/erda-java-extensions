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

package cloud.erda.agent.core.reporter;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.GsonUtils;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import cloud.erda.agent.core.config.TelegrafProxyConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class TelegrafReporter implements BootService {

    private static final ILog log = LogManager.getLogger(TelegrafReporter.class);

    private TelegrafProxyConfig config;
    private InetSocketAddress socketAddress;
    private DatagramSocket socket;
    private boolean init;

    @Override
    public void beforeBoot() throws Throwable {
    }

    @Override
    public void boot() throws Throwable {
        this.config = ConfigAccessor.Default.getConfig(TelegrafProxyConfig.class);
        this.socketAddress = new InetSocketAddress(config.getProxyHost(), config.getProxyPort());
        log.info("Telegraf proxy addr " + socketAddress.toString());
        try {
            this.socket = new DatagramSocket();
            init = true;
        } catch (SocketException e) {
            init = false;
            log.error("Bind udp client address fail.", e);
            throw e;
        }
    }

    @Override
    public void afterBoot() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        if (init) {
            this.socket.close();
        }
    }

    public void send(Metric... metrics) {
        if (!init) {
            return;
        }
        if (metrics.length <= 0) {
            return;
        }

        if (metrics.length <= 10) {
            doSend(metrics);
        } else {
            MetricBuckets metricBuckets = new MetricBuckets(metrics);
            for (Metric[] buckets : metricBuckets) {
                doSend(buckets);
            }
        }
    }

    private void doSend(Metric[] buckets) {
        byte[] data = GsonUtils.toBytes(buckets);
        try {
            socket.send(new DatagramPacket(data, 0, data.length, socketAddress));
            if (log.isDebugEnable()) {
                log.debug("Send {}({}KB) data to collector proxy success. \n {}", buckets.length, data.length / (float) 1024, new String(data));
            }
        } catch (IOException e) {
            if (log.isErrorEnable()) {
                log.error("Send data fail.", e);
            }
        }
    }
}
