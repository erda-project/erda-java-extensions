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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

/**
 * @author liuhaoyang
 * @date 2021/11/30 13:38
 */
public class TcpInactiveHandler extends SimpleChannelInboundHandler<Object> {

    private final static ILog LOGGER = LogManager.getLogger(TcpInactiveHandler.class);
    private final TcpClient tcpClient;

    public TcpInactiveHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        // Discard received data
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn("TCP connection status changed to NOT_ACTIVE");
        tcpClient.reconnect(ctx.channel().eventLoop());
        super.channelInactive(ctx);
    }
}
