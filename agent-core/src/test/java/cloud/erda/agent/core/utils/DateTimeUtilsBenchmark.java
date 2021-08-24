package cloud.erda.agent.core.utils;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaihongwei
 * @since 2021/8/24
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 1, time = 10)
@Threads(8)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DateTimeUtilsBenchmark {

    public static long currentTimeNanoOld() {
        Long currentTime = System.currentTimeMillis() * 1000000;
        Long nanoTime = System.nanoTime();
        return currentTime + (nanoTime - nanoTime / 1000000 * 1000000);
    }

    public static long currentTimeNano() {
        Instant instant = Instant.now().atZone(ZoneId.systemDefault()).toInstant();
        return TimeUnit.NANOSECONDS.convert(instant.getEpochSecond(), TimeUnit.SECONDS) + instant.getNano();
    }

    @Benchmark
    public void testCurrentTimeNano() {
        long l = currentTimeNano();
    }

    @Benchmark
    public void testCurrentTimeNanoOld() {
        long l = currentTimeNanoOld();
    }

    public static void main(String[] args) throws RunnerException {
        Options build = new OptionsBuilder().include(DateTimeUtilsBenchmark.class.getSimpleName())
                .forks(2)
                .build();
        new Runner(build).run();
    }
}
