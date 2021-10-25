package cloud.erda.agent.core.utils;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author zhaihongwei
 * @since 2021/8/24
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 3)
@Measurement(iterations = 1, time = 10)
@Threads(8)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DateTimeUtilsBenchmark {

    public static long currentTimeNano() {
        return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }

    public static long currentTimeNano2() {
        return System.currentTimeMillis() * 1000000L;
    }

    @Benchmark
    public void testCurrentTimeNano() {
        long l = currentTimeNano();
    }

    @Benchmark
    public void testCurrentTimeNano2() {
        long l = currentTimeNano2();
    }

    public static void main(String[] args) throws RunnerException {
        Options build = new OptionsBuilder().include(DateTimeUtilsBenchmark.class.getSimpleName())
                .forks(2)
                .build();
        new Runner(build).run();
    }
}
