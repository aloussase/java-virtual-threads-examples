package io.github.aloussase.virtualthreadsdemo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


// Virtual threads cannot be woken up when all carrier threads are busy
// If my CPU has 4 cores, then at most 4 virtual threads can be running at the same time.
// Performance in VTs comes from cooperatively yielding while waiting for IO.
@Warmup(iterations = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SingleShotTime)
public class FibonacciBenchmarks {


    /**
     * Number of iterations.
     */
    private final static int N = 100_000_000;
    //private final static int N = 100_000;

    @Benchmark
    public void sequential(Blackhole blackhole) {
        blackhole.consume(invoke());
        blackhole.consume(invoke());
    }

    @Benchmark
    public void concurrent(Blackhole blackhole) throws InterruptedException {
        final var th1 = Thread.ofVirtual().start(() -> blackhole.consume(invoke()));
        final var th2 = Thread.ofVirtual().start(() -> blackhole.consume(invoke()));
        th1.join();
        th2.join();
    }

    @Benchmark
    public void parallel(Blackhole blackhole) throws InterruptedException {
        final var th1 = new Thread(() -> blackhole.consume(invoke()));
        final var th2 = new Thread(() -> blackhole.consume(invoke()));
        th1.start();
        th2.start();
        th1.join();
        th2.join();
    }

    private int invoke() {
        int a = 0;
        int b = 1;
        for (int i = 2; i < N; i++) {
            int c = a + b;
            a = b;
            b = c;
        }
        return b;
    }

}
