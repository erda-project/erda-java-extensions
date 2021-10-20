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

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liuhaoyang
 * @date 2021/5/9 18:21
 */
public class InterceptPointConfig implements Config {

    /**
     * {class1}#{method1},{class2}#{method2}
     * <p>
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getInstanceMethods;io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_INSTANCE_POINTS", defaultValue = "")
    private String instancePointsCompatible;

    /**
     * {class1}#{method1},{class2}#{method2}
     * <p>
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_STATIC_POINTS", defaultValue = "")
    private String staticPointsCompatible;

    /**
     * {class1}#{method1},{class2}#{method2}
     * <p>
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getInstanceMethods,io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "MSP_METHOD_INTERCEPT_POINTS", defaultValue = "")
    private String methodInterceptPoints;

    /**
     * {packageName1},{packageName2}
     * <p>
     * example:
     * io.terminus.spot.plugin.method,io.terminus.spot.plugin.method.class
     */
    @Configuration(name = "MSP_PACKAGE_INTERCEPT_POINTS", defaultValue = "")
    private String packageInterceptPoints;

    public String[] getInstancePoints() {
        List<String> points = new ArrayList<>();
        points.addAll(Arrays.asList(getPoints(instancePointsCompatible)));
        points.addAll(Arrays.asList(getPoints(methodInterceptPoints)));
        return points.toArray(new String[0]);
    }

    public String[] getStaticPoints() {
        List<String> points = new ArrayList<>();
        points.addAll(Arrays.asList(getPoints(staticPointsCompatible)));
        points.addAll(Arrays.asList(getPoints(methodInterceptPoints)));
        return points.toArray(new String[0]);
    }

    public String[] getPackageInterceptPoints() {
        return getPoints(packageInterceptPoints);
    }

    private String[] getPoints(String config) {
        if (Strings.isEmpty(config)) {
            return new String[0];
        }
        return config.replace(" ", "").replace(";", ",").split(",");
    }
}