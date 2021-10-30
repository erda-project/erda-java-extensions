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

package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.config.AgentConfig;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import cloud.erda.agent.core.config.loader.ConfigAccessor;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liuhaoyang
 * @since 2019-01-07 16:29
 **/
public class SamplerService extends ScheduledService implements Sampler {

    private final Random random = new Random();
    private final int rate = ConfigAccessor.Default.getConfig(AgentConfig.class).samplingRate();
    private final int limit = ConfigAccessor.Default.getConfig(AgentConfig.class).samplingLimit();
    private final AtomicInteger index = new AtomicInteger(0);

    public boolean shouldSample() {
        if (index.get() > limit) {
            return false;
        }
        if (rate < 0 || rate > 100) {
            return false;
        }
        int v = random.nextInt(100);
        if (v > rate) {
            return false;
        }
        index.getAndIncrement();
        return true;
    }

    @Override
    protected void executing() {
        while (true) {
            int current = index.get();
            if (index.compareAndSet(current, 0)) {
                break;
            }
        }
    }

    @Override
    protected long period() {
        return 60;
    }
}
