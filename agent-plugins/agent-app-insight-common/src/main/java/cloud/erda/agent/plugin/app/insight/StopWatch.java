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

/**
 * @author liuhaoyang
 * @since 2019-01-21 17:42
 **/
public class StopWatch {
    private Long start;

    private Long end;

    public StopWatch() {
        this.start = System.nanoTime();
    }

    public void stop() {
        if (end == null) {
            end = System.nanoTime();
        }
    }

    public float elapsed() {
        if (end == null) {
            return System.nanoTime() - start;
        }
        return Math.abs(end - start);
    }
}
