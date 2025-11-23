package io.github.aloussase.virtualthreadsdemo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


// Virtual threads cannot be woken up when all carrier threads are busy
// If my CPU has 4 cores, then at most 4 virtual threads can be running at the same time.
// Performance in VTs comes from cooperatively yielding while waiting for IO.
@Threads(1)
public class FibonacciBenchmarks {


    /**
     * Number of iterations.
     * <p>
     * At a low number of iterations (e.g. 100, 1000) the overhead of thread creation overwhelms the
     * execution time.
     */
    private final static int N = 500_000;

    @Warmup(iterations = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    public void fibonacciSequential(Blackhole blackhole) {
        int a = 0;
        int b = 1;
        for (int i = 2; i < N; i++) {
            int c = a + b;
            a = b;
            b = c;
        }
        blackhole.consume(b);
    }

    @Warmup(iterations = 2)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    public void fibonacciVirtualThreads(Blackhole blackhole) throws InterruptedException {
        final var th1 = Thread.ofVirtual().start(() -> {
            int a = 0;
            int b = 1;
            for (int i = 2; i < N / 2; i++) {
                int c = a + b;
                a = b;
                b = c;
            }
            blackhole.consume(b);
        });
        final var th2 = Thread.ofVirtual().start(() -> {
            int a = 0;
            int b = 1;
            for (int i = 2; i < N / 2; i++) {
                int c = a + b;
                a = b;
                b = c;
            }
            blackhole.consume(b);
        });

        th1.join();
        th2.join();
    }

    @Warmup(iterations = 2)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    public void fibonacciPlatformThreads(Blackhole blackhole) throws InterruptedException {
        final var th1 = new Thread(() -> {
            int a = 0;
            int b = 1;
            for (int i = 2; i < N / 2; i++) {
                int c = a + b;
                a = b;
                b = c;
            }
            blackhole.consume(b);
        });
        final var th2 = new Thread(() -> {
            int a = 0;
            int b = 1;
            for (int i = 2; i < N / 2; i++) {
                int c = a + b;
                a = b;
                b = c;
            }
            blackhole.consume(b);
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
    }

}
