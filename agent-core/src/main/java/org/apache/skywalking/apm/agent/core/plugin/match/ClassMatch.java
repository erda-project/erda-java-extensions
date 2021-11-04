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


package org.apache.skywalking.apm.agent.core.plugin.match;

import net.bytebuddy.description.type.TypeDescription;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wusheng
 */
public interface ClassMatch {

    default boolean defaultMatch(TypeDescription typeDescription) {
        if (JavaClass.isJavaClass(typeDescription.getActualName())) {
            return false;
        }
        if (PackageIgnore.isIgnore(typeDescription.getActualName())) {
            return false;
        }
        if (typeDescription.isInterface()) {
            return false;
        }
//        if (typeDescription.isAssignableTo(EnhancedInstance.class)) {
//            return false;
//        }
        return true;
    }

    class JavaClass {
        private static final Map<String, Class> JAVA_CLASS = new HashMap<String, Class>();

        static {
            JAVA_CLASS.put("java.lang.Object", java.lang.Object.class);
            JAVA_CLASS.put("boolean.class", boolean.class);
            JAVA_CLASS.put("char.class", char.class);
            JAVA_CLASS.put("byte.class", byte.class);
            JAVA_CLASS.put("short.class", short.class);
            JAVA_CLASS.put("int.class", int.class);
            JAVA_CLASS.put("long.class", long.class);
            JAVA_CLASS.put("float.class", float.class);
            JAVA_CLASS.put("double.class", double.class);
            JAVA_CLASS.put("java.util.List", java.util.List.class);
            JAVA_CLASS.put("java.util.Map", java.util.Map.class);
        }

        public static boolean isJavaClass(String className) {
            return JAVA_CLASS.containsKey(className);
        }
    }

    class PackageIgnore {
        private static final Set<String> Packages = new HashSet<>();

        static {
            Packages.add("org.apache.skywalking.*");
            Packages.add("cloud.erda.*");
            Packages.add("org.springframework.*");
        }

        public static boolean isIgnore(String className) {
            for (String pack : Packages) {
                if (className.matches(pack)) {
                    return true;
                }
            }
            return false;
        }
    }
}
