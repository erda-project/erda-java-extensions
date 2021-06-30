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

package cloud.erda.agent.plugin.method;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @date 2021/5/9 18:21
 */
public class InterceptPointConfig implements Config {

    /**
     * {class1}#{method1};{class2}#{method2}
     *
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getInstanceMethods;io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_INSTANCE_POINTS", defaultValue = "")
    private String instancePoints;

    /**
     * {class1}#{method1};{class2}#{method2}
     *
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_STATIC_POINTS", defaultValue = "")
    private String staticPoints;

    @Configuration(name = "TERMINUS_INTERCEPT_ATTACH_TRACE", defaultValue = "false")
    private boolean attachTrace;

    public String[] getInstancePoints() {
        if (instancePoints == null) {
            return new String[0];
        }
        return instancePoints.replace(" ", "").split(";");
    }

    public String[] getStaticPoints() {
        if (staticPoints == null) {
            return new String[0];
        }
        return staticPoints.replace(" ", "").split(";");
    }

    public boolean isAttachTrace() {
        return attachTrace;
    }
}