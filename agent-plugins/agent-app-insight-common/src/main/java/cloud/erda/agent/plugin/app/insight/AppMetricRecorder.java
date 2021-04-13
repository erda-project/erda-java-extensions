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

package cloud.erda.agent.plugin.app.insight;

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.reporter.TelegrafReporter;

/**
 * @author liuhaoyang
 * @since 2019-01-21 18:12
 **/
public class AppMetricRecorder {

    private static final TelegrafReporter transporter = ServiceManager.INSTANCE.findService(TelegrafReporter.class);

    public static void record(AppMetricBuilder builder) {
        transporter.send(builder.build());
    }
}
