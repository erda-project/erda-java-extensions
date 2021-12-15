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

package cloud.erda.agent.tests.benchmarks;

import cloud.erda.agent.tests.benchmarks.core.metrics.SerializeBenchmarks;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author liuhaoyang
 * @date 2021/11/30 10:21
 */
public class Application {

    public static void main(final String[] args) throws RunnerException {
        System.out.println("Start benchmark ..");
        Options options = new OptionsBuilder().include(SerializeBenchmarks.class.getSimpleName())
                .output("output/benchmark.log").forks(1).build();
        new Runner(options).run();
    }
}
