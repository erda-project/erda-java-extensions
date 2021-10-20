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

package cloud.erda.agent.plugin.sdk.interceptPoint;


import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.plugin.sdk.defines.InstanceMethodInterceptInstrumentation;
import cloud.erda.agent.plugin.sdk.defines.PackageInterceptInstrumentation;
import cloud.erda.agent.plugin.sdk.defines.StaticMethodInterceptInstrumentation;
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

    private static final ILog logger = LogManager.getLogger(UserDefineInterceptPointPluginLoader.class);

    @Override
    public List<AbstractClassEnhancePluginDefine> load() throws Exception {
        List<AbstractClassEnhancePluginDefine> pluginDefines = new ArrayList<>();
        InterceptPointConfig interceptPointConfig = new ConfigAccessor(getClass().getClassLoader()).getConfig(InterceptPointConfig.class);
        for (String packageName : interceptPointConfig.getPackageInterceptPoints()) {
            pluginDefines.add(new PackageInterceptInstrumentation(packageName));
            logger.info("loading package[{}] interceptPoint.", packageName);
        }
        MethodInterceptPointResolver instanceMethodInterceptPointResolver = new MethodInterceptPointResolver(interceptPointConfig.getInstancePoints());
        for (InterceptPoint interceptPoint : instanceMethodInterceptPointResolver.resolve()) {
            pluginDefines.add(new InstanceMethodInterceptInstrumentation(interceptPoint));
            logger.info("loading instance interceptPoint. class {} methods {}", interceptPoint.getClassName(), Arrays.stream(interceptPoint.getMethodNames()).reduce((x, y) -> x + "," + y));
        }
        MethodInterceptPointResolver staticMethodInterceptPointResolver = new MethodInterceptPointResolver(interceptPointConfig.getStaticPoints());
        for (InterceptPoint interceptPoint : staticMethodInterceptPointResolver.resolve()) {
            pluginDefines.add(new StaticMethodInterceptInstrumentation(interceptPoint));
            logger.info("loading static interceptPoint. class {} methods {}", interceptPoint.getClassName(), Arrays.stream(interceptPoint.getMethodNames()).reduce((x, y) -> x + "," + y));
        }
        return pluginDefines;
    }
}
