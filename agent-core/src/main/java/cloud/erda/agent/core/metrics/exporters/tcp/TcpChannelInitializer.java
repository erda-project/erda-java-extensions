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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author liuhaoyang
 * @date 2021/11/30 13:41
 */
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private TcpClient tcpClient;

    public TcpChannelInitializer(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(StringDecoder.class.getSimpleName(), new StringDecoder());
        socketChannel.pipeline().addLast(LineProtocolEncoder.class.getSimpleName(), new LineProtocolEncoder());
        socketChannel.pipeline().addLast(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(0, tcpClient.getConfig().getIdleTimeout(), 0));
        socketChannel.pipeline().addLast(TcpInactiveHandler.class.getSimpleName(), new TcpInactiveHandler(tcpClient));
    }

}
