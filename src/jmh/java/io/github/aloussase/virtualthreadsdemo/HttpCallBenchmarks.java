package io.github.aloussase.virtualthreadsdemo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

@Threads(1)
@Warmup(iterations = 1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class HttpCallBenchmarks {


    @Benchmark
    public void sequentialCalls(Blackhole blackhole) {
        blackhole.consume(invoke());
        blackhole.consume(invoke());
    }

    @Benchmark
    public void concurrentCalls(Blackhole blackhole) throws InterruptedException {
        final var th1 = Thread.ofVirtual().start(() -> blackhole.consume(invoke()));
        final var th2 = Thread.ofVirtual().start(() -> blackhole.consume(invoke()));
        th1.join();
        th2.join();
    }

    @Benchmark
    public void parallelCalls(Blackhole blackhole) throws InterruptedException {
        final var th1 = new Thread(() -> blackhole.consume(invoke()));
        final var th2 = new Thread(() -> blackhole.consume(invoke()));
        th1.start();
        th2.start();
        th1.join();
        th2.join();
    }


    private String invoke() {
        try (var client = HttpClient.newHttpClient()) {
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://jsonplaceholder.typicode.com/posts/1"))
                    .GET()
                    .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
