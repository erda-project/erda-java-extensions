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
import cloud.erda.agent.core.metrics.exporters.Exporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporter;
import cloud.erda.agent.core.metrics.exporters.MetricExporterFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;
import java.util.Properties;

/**
 * @author liuhaoyang
 * @date 2021/11/30 11:30
 */
public class TcpExporterFactory extends MetricExporterFactory<TcpExporterFactory.TcpExporter, TcpExportConfig> {

    @Exporter("TCP")
    @Override
    public TcpExporter create(TcpExportConfig config) {
        return new TcpExporter(config);
    }

    @Override
    public void shutdown(TcpExporter metricExporter) {
    }

    public static class TcpExporter implements MetricExporter {

        private final EventLoopGroup eventLoopGroup;
        private final TcpClient tcpClient;

        public TcpExporter(TcpExportConfig config) {
            this.tcpClient = new TcpClient(config);
            this.eventLoopGroup = new NioEventLoopGroup();
        }

        @Override
        public void init(Properties properties) {
            tcpClient.connect(eventLoopGroup);
        }

        @Override
        public void consume(List<Metric> data) {
            tcpClient.send(data.toArray(new Metric[0]));
        }

        @Override
        public void onError(List<Metric> data, Throwable t) {
        }

        @Override
        public void onExit() {
            tcpClient.disconnect();
            eventLoopGroup.shutdownGracefully();
        }
    }
}
