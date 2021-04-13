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

import java.util.Collection;

/**
 * @author liuhaoyang 2020/3/22 14:25
 */
public class Collections {

    public static <E> boolean IsNullOrEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <E> boolean IsNullOrEmpty(E[] array) {
        return array == null || array.length == 0;
    }

    public static boolean IsNullOrEmpty(double[] array) {
        return array == null || array.length == 0;
    }
}
