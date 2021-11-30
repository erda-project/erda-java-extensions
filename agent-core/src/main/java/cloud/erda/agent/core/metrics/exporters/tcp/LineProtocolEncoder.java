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
import cloud.erda.agent.core.metrics.serializers.LineProtocolSerializer;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author liuhaoyang
 * @date 2021/11/30 17:11
 */
public class LineProtocolEncoder extends MessageToMessageEncoder<Metric[]> {

    private final static ILog LOGGER = LogManager.getLogger(LineProtocolEncoder.class);
    private final LineProtocolSerializer serializer = new LineProtocolSerializer();

    @Override
    protected void encode(ChannelHandlerContext ctx, Metric[] metrics, List<Object> out) throws Exception {
        if (metrics.length != 0) {
            String data = serializer.serialize(metrics);
            CharBuffer buffer = CharBuffer.wrap(data);
            if (LOGGER.isDebugEnable()) {
                LOGGER.debug("Send {}({}KB) data to tcp backend. \n {}", metrics.length, buffer.length() / (float) 1024, data);
            }
            out.add(ByteBufUtil.encodeString(ctx.alloc(), buffer, StandardCharsets.UTF_8));
        }
    }
}
