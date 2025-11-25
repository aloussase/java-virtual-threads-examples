package io.github.aloussase.virtualthreadsdemo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SleepBenchmarks {

    private static final int N = 100;

    @Benchmark
    public void concurrent(Blackhole blackhole) throws InterruptedException {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < N; i++) {
                exec.submit(() -> blackhole.consume(invoke()));
            }
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void parallel(Blackhole blackhole) throws InterruptedException {
        try (var exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (int i = 0; i < N; i++) {
                exec.submit(() -> blackhole.consume(invoke()));
            }
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }


    private int invoke() {
        try {
            Thread.sleep(500);
            return 42;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
