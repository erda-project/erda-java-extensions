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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liuhaoyang
 * @date 2021/5/10 16:48
 */
public class InterceptPoint {

    private String className;

    private Set<String> methodNames;

    public InterceptPoint(String className, String... methodNames) {
        this.className = className;
        this.methodNames = new HashSet<>(Arrays.asList(methodNames));
    }

    public void addPoint(String methodName) {
        if (methodName != null) {
            methodNames.add(methodName);
        }
    }

    public String getClassName() {
        return className;
    }

    public String[] getMethodNames() {
        return methodNames.toArray(new String[0]);
    }
}
