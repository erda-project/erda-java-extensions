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

package cloud.erda.agent.core.utils;

import java.util.concurrent.Callable;

/**
 * @author liuhaoyang
 * @date 2021/10/12 15:55
 */
public class Numbers {

    public static String doubleToGoString(double d) {
        if (d == Double.POSITIVE_INFINITY) {
            return "+Inf";
        }
        if (d == Double.NEGATIVE_INFINITY) {
            return "-Inf";
        }
        return Double.toString(d);
    }

    public static double safeDouble(Callable<Object> callable) {
        try {
            return Double.parseDouble(callable.call().toString());
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static long safeLong(Callable<Object> callable) {
        try {
            return Long.parseLong(callable.call().toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
