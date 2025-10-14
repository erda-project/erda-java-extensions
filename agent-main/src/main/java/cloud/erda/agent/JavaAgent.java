/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.erda.agent;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.AgentConfig;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.skywalking.apm.agent.InstrumentDebuggingClass;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.*;
import org.apache.skywalking.apm.agent.core.plugin.jdk9module.JDK9ModuleExporter;
import org.apache.skywalking.apm.agent.core.util.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.core.util.AgentPackagePath;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class JavaAgent {
    private static final ILog logger = LogManager.getLogger(JavaAgent.class);

    public static void premain(String agentArgs, Instrumentation instrumentation) throws PluginException, AgentPackageNotFoundException {

        AgentPackagePath.getPath();

        AgentConfig config = ConfigAccessor.Default.getConfig(AgentConfig.class);
        if (!config.agentEnable()) {
            logger.info("Java agent disable. Set TERMINUS_AGENT_ENABLE=TRUE to enable agent.");
            return;
        }

        final PluginFinder pluginFinder;
        try {
            pluginFinder = new PluginFinder(new PluginBootstrap().loadPlugins(config));
            ServiceManager.INSTANCE.boot(config);
        } catch (Exception e) {
            logger.error(e, "Java agent initialized failure. Shutting down.");
            return;
        }

        // startup plugin & instrument

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceManager.INSTANCE.shutdown();
            }
        }, "Java-Agent service shutdown thread"));

        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy.")
                        .or(nameStartsWith("org.slf4j."))
                        .or(nameStartsWith("org.groovy."))
                        .or(nameContains("javassist"))
                        .or(nameContains(".asm."))
                        .or(nameContains(".reflectasm."))
                        .or(nameStartsWith("sun.reflect"))
                        .or(ElementMatchers.isSynthetic()));

        JDK9ModuleExporter.EdgeClasses edgeClasses = new JDK9ModuleExporter.EdgeClasses();

        try {
            agentBuilder = JDK9ModuleExporter.openReadEdge(instrumentation, agentBuilder, edgeClasses);
        } catch (Exception e) {
            logger.error(e, "Java agent open read edge in JDK 9+ failure. Shutting down.");
            return;
        }

        agentBuilder.type(pluginFinder.buildMatch())
                .transform(new Transformer(pluginFinder))
                .with(new Listener())
                .with(instrumentation.isRetransformClassesSupported() ? AgentBuilder.RedefinitionStrategy.RETRANSFORMATION : AgentBuilder.RedefinitionStrategy.DISABLED)
                .installOn(instrumentation);
    }

    private static class Transformer implements AgentBuilder.Transformer {
        private PluginFinder pluginFinder;

        Transformer(PluginFinder pluginFinder) {
            this.pluginFinder = pluginFinder;
        }

        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, 
                ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
            List<AbstractClassEnhancePluginDefine> pluginDefines = pluginFinder.find(typeDescription, classLoader);
            if (pluginDefines.size() > 0) {
                DynamicType.Builder<?> newBuilder = builder;
                EnhanceContext context = new EnhanceContext();
                context.setTypeDescription(typeDescription);
                for (AbstractClassEnhancePluginDefine define : pluginDefines) {
                    DynamicType.Builder<?> possibleNewBuilder = define.define(typeDescription.getTypeName(), newBuilder, classLoader, context);
                    if (possibleNewBuilder != null) {
                        newBuilder = possibleNewBuilder;
                    }
                }
                if (context.isEnhanced()) {
//                    logger.debug("Finish the prepare stage for {}.", typeDescription.getName());
                }

                return newBuilder;
            }

            logger.debug("Matched class {}, but ignore by finding mechanism.", typeDescription.getTypeName());
            return builder;
        }
    }

    private static class Listener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                                     boolean loaded, DynamicType dynamicType) {
            if (logger.isDebugEnable()) {
                logger.debug("Enhance class {} completely.", typeDescription.getName());
            }

            InstrumentDebuggingClass.INSTANCE.log(typeDescription, dynamicType);
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                              boolean loaded) {
//            logger.debug("On Ignored class {}.", typeDescription.getName());

        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                            Throwable throwable) {
            logger.error("Enhance class " + typeName + "[ " + classLoader.getClass().getName() + " ]" + "error.", throwable);
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
//            logger.debug("On Enhance class {} completed.", typeName);
        }
    }
}
