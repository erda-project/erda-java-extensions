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


import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.plugin.method.defines.InstanceMethodInterceptInstrumentation;
import cloud.erda.agent.plugin.method.defines.StaticMethodInterceptInstrumentation;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.AbstractClassEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.PluginLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liuhaoyang
 * @date 2021/5/10 16:41
 */
public class UserDefineInterceptPointPluginLoader implements PluginLoader {

    private static final ILog log = LogManager.getLogger(UserDefineInterceptPointPluginLoader.class);

    @Override
    public List<AbstractClassEnhancePluginDefine> load() throws Exception {
        List<AbstractClassEnhancePluginDefine> pluginDefines = new ArrayList<>();
        InterceptPointConfig interceptPointConfig = new ConfigAccessor(getClass().getClassLoader()).getConfig(InterceptPointConfig.class);
        InterceptPointResolver instanceInterceptPointResolver = new InterceptPointResolver(interceptPointConfig.getInstancePoints());
        for (InterceptPoint interceptPoint : instanceInterceptPointResolver.resolve()) {
            pluginDefines.add(new InstanceMethodInterceptInstrumentation(interceptPoint));
            log.debug("loading instance interceptPoint. class {} methods {}", interceptPoint.getClassName(), Arrays.stream(interceptPoint.getMethodNames()).reduce((x, y) -> x + "," + y));
        }
        InterceptPointResolver staticInterceptPointResolver = new InterceptPointResolver(interceptPointConfig.getStaticPoints());
        for (InterceptPoint interceptPoint : staticInterceptPointResolver.resolve()) {
            pluginDefines.add(new StaticMethodInterceptInstrumentation(interceptPoint));
            log.debug("loading static interceptPoint. class {} methods {}", interceptPoint.getClassName(), Arrays.stream(interceptPoint.getMethodNames()).reduce((x, y) -> x + "," + y));
        }
        return pluginDefines;
    }
}
