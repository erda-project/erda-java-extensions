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

package cloud.erda.agent.plugin.sdk.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.IndirectMatch;

import java.util.HashMap;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author liuhaoyang
 * @date 2021/10/20 13:31
 */
public class PackageMatch implements IndirectMatch {

    private final String[] matchPackageNames;

    private PackageMatch(String[] packages) {
        if (packages == null || packages.length == 0) {
            throw new IllegalArgumentException("match package names is null");
        }
        this.matchPackageNames = packages;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String name : matchPackageNames) {
            if (junction == null) {
                junction = nameStartsWith(name);
            } else {
                junction = junction.or(nameStartsWith(name));
            }
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        for (String packageName : matchPackageNames) {
            if (typeDescription.getActualName().startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static ClassMatch byPackages(String... packages) {
        return new PackageMatch(packages);
    }
}
