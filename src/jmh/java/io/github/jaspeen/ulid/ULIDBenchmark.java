package io.github.jaspeen.ulid;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class ULIDBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public ULID ulidRandomThroughput() {
        return ULID.random();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public ULID ulidRandomAverage() {
        return ULID.random();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public UUID uuidRandomThroughput() {
        return UUID.randomUUID();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public UUID uuidRandomAverage() {
        return UUID.randomUUID();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public String ulidRandomToStringThroughput() {
        return ULID.random().toString();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String ulidRandomToStringAverage() {
        return ULID.random().toString();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public String uuidRandomToStringThroughput() {
        return UUID.randomUUID().toString();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String uuidRandomToStringAverage() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                              .include(ULIDBenchmark.class.getSimpleName())
                              .warmupTime(TimeValue.seconds(2))
                              .warmupIterations(5)
                              .measurementIterations(5)
                              .forks(1)
                              .build();

        new Runner(opt).run();
    }
}