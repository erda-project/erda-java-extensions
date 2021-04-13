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

import cloud.erda.agent.core.utils.DateTimeUtils;

/**
 * @author liuhaoyang
 * @since 2019-01-21 17:42
 **/
class AppMetricWatch {
    private Long start;

    private Long end;

    AppMetricWatch(Long start) {
        this.start = start == null ? DateTimeUtils.currentTimeNano() : start;
    }

    void stop() {
        if (end == null) {
            end = DateTimeUtils.currentTimeNano();
        }
    }

    long elapsed() {
        if (end == null) {
            return DateTimeUtils.currentTimeNano() - start;
        }
        return Math.abs(end - start);
    }
}
