package io.github.aloussase.virtualthreadsdemo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class HttpCallBenchmarks {


    @Benchmark
    public void sequential(Blackhole blackhole) {
        blackhole.consume(invoke());
        blackhole.consume(invoke());
    }

    @Benchmark
    public void concurrent(Blackhole blackhole) throws InterruptedException {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            exec.submit(() -> blackhole.consume(invoke()));
            exec.submit(() -> blackhole.consume(invoke()));
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void parallel(Blackhole blackhole) throws InterruptedException {
        try (var exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            exec.submit(() -> blackhole.consume(invoke()));
            exec.submit(() -> blackhole.consume(invoke()));
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
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
