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

package cloud.erda.msp.integrations.micrometer;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author liuhaoyang
 * @date 2021/10/10 14:10
 */
public class NodeProxyReporter implements MetricReporter {

    private static final Gson gson = new Gson();
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final Logger logger = LoggerFactory.getLogger(NodeProxyReporter.class);

    private final InetSocketAddress socketAddress;
    private final DatagramSocket socket;
    private boolean init;

    public NodeProxyReporter(String host, int port) throws SocketException {
        this.socketAddress = new InetSocketAddress(host, port);
        logger.info("Node proxy addr {}", this.socketAddress.toString());
        try {
            this.socket = new DatagramSocket();
            init = true;
        } catch (SocketException e) {
            init = false;
            logger.error("Bind udp client address fail.", e);
            throw e;
        }
    }

    @Override
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
        byte[] data = gson.toJson(buckets).getBytes(charset);
        try {
            socket.send(new DatagramPacket(data, 0, data.length, socketAddress));
            if (logger.isDebugEnabled()) {
                logger.debug("Send {}({}KB) data to collector proxy success. \n {}", buckets.length, data.length / (float) 1024, new String(data));
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Send data fail.", e);
            }
        }
    }
}
