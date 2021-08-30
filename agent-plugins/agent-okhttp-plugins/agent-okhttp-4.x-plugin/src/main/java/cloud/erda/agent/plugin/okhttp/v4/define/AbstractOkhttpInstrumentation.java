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

package cloud.erda.agent.plugin.okhttp.v4.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;

/**
 * @author liuhaoyang
 * @date 2021/8/30 17:22
 */
public abstract class AbstractOkhttpInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    public static final String WITHNESS_CLASSES = "okhttp3.internal.connection.RealCall";

    @Override
    protected final String[] witnessClasses() {
        return new String[] {WITHNESS_CLASSES};
    }
}
