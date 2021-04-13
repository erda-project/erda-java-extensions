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

package cloud.erda.agent.plugin.log.error;

public class StackElement {
    private String className;

    private String methodName;

    private String fileName;

    private int line;

    private int index;

    public int getLine() {
        return line;
    }

    public String getClassName() {
        return className;
    }

    public int getIndex() {
        return index;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setMethodName(String methodName) {
        if (methodName.contains("$")) {
            methodName = methodName.split("\\$")[0];
        }
        this.methodName = methodName;
    }

    public void setIndex(int order) {
        this.index = order;
    }
}
