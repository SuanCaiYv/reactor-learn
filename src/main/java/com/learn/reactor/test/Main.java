package com.learn.reactor.test;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

public class Main {

    public static void main(String[] args) {
        Mono.fromCallable(() -> {
            System.out.println("publish   run on the thread: " + Thread.currentThread().getName());
            LockSupport.parkNanos(Duration.ofMillis(3000).toNanos());
            return "aaa";
        })
                .map(p1 -> {
                    System.out.println("map1      run on the thread: " + Thread.currentThread().getName());
                    return "bbb";
                })
                .map(p2 -> {
                    System.out.println("map2      run on the thread: " + Thread.currentThread().getName());
                    return "ccc";
                })
                .publishOn(Schedulers.boundedElastic())
                .map(p3 -> {
                    System.out.println("map3      run on the thread: " + Thread.currentThread().getName());
                    return "ddd";
                })
                .map(p4 -> {
                    System.out.println("map4      run on the thread: " + Thread.currentThread().getName());
                    return "eee";
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(item -> {
                    System.out.println("subscribe run on the thread: " + Thread.currentThread().getName());
                    System.out.println(item);
                });
        System.out.println("run");
        LockSupport.parkNanos(Duration.ofMillis(5000).toNanos());
    }
}
