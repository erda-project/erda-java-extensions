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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/5/10 15:52
 */
public class MethodInterceptPointResolver {

    private final String[] interceptPoints;

    public MethodInterceptPointResolver(String[] interceptPoints) {
        this.interceptPoints = interceptPoints;
    }

    public List<InterceptPoint> resolve() {
        Map<String, InterceptPoint> interceptPointMap = new HashMap<>();
        for (String point : interceptPoints) {
            String[] names = point.split("#");
            if (names.length != 2) {
                continue;
            }
            InterceptPoint interceptPoint = interceptPointMap.computeIfAbsent(names[0], InterceptPoint::new);
            interceptPoint.addPoint(names[1]);
        }
        return new ArrayList<>(interceptPointMap.values());
    }
}
