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

package cloud.erda.agent.core.metrics.exporters.tcp;

import cloud.erda.agent.core.metrics.Metric;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.util.concurrent.TimeUnit;

/**
 * @author liuhaoyang
 * @date 2021/11/30 14:06
 */
public class TcpClient {

    private final static ILog LOGGER = LogManager.getLogger(TcpClient.class);
    private final Object lock = new Object();
    private final TcpExportConfig config;
    private Channel channel;
    private boolean connected;

    public TcpClient(TcpExportConfig config) {
        this.config = config;
    }

    public TcpExportConfig getConfig() {
        return config;
    }

    public boolean send(Metric[] metrics) {
        if (isConnected()) {
            channel.writeAndFlush(metrics);
            return true;
        }
        return false;
    }

    public void connect(EventLoopGroup loopGroup) {
        if (isConnected()) {
            return;
        }
        synchronized (lock) {
            if (isConnected()) {
                return;
            }
        }
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .handler(new TcpChannelInitializer(this));
        bootstrap.connect(config.getExportHost(), config.getExportPort()).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                LOGGER.warn("Connected to TCP backend {}:{} aborted. Exception: {}", config.getExportHost(), config.getExportPort(), channelFuture.cause());
                reconnect(loopGroup);
            } else {
                synchronized (lock) {
                    channel = channelFuture.channel();
                    connected = true;
                }
                LOGGER.info("Connected to TCP backend {}:{}", config.getExportHost(), config.getExportPort());
            }
        });
        LOGGER.info("Connecting to TCP backend {}:{}", config.getExportHost(), config.getExportPort());
    }

    public void reconnect(EventLoopGroup loopGroup) {
        LOGGER.warn("Delay {} ms reconnecting to TCP backend {}:{}", config.getReconnectDelay(), config.getExportHost(), config.getExportPort());
        loopGroup.schedule(() -> connect(loopGroup), config.getReconnectDelay(), TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        synchronized (lock) {
            disconnectAndCloseChannel();
        }
    }

    private void disconnectAndCloseChannel() {
        connected = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.pipeline().remove(TcpInactiveHandler.class.getSimpleName());
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
                channel.pipeline().remove(LineProtocolEncoder.class.getSimpleName());
                channel.pipeline().remove(StringDecoder.class.getSimpleName());
                channel.close();
            }
        } catch (Exception ex) {
            LOGGER.error(ex, "Close TCP channel error.");
        } finally {
            channel = null;
        }
    }

    private boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }
}
