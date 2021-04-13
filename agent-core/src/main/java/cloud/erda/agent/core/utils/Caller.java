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

package cloud.erda.agent.core.utils;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

/**
 * @author liuhaoyang
 * @since 2019-11-28 19:17
 **/
public class Caller {
    private static final ILog log = LogManager.getLogger(Caller.class);

    public static void invoke(Action action) {
        try {
            action.invoke();
        } catch (Throwable t) {
            log.error(t, "Caller invoke exception");
        }
    }

    public static interface Action {
        void invoke() throws Exception;
    }
}
