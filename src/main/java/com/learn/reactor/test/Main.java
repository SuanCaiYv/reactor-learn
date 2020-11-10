package com.learn.reactor.test;

import reactor.core.publisher.Flux;

public class Main {

    public static void main(String[] args) {
        Flux<Integer> flux = Flux.range(0, 10);
        flux.subscribe(i -> {
            System.out.println("run1: " + i);
        });
        flux.subscribe(i -> {
            System.out.println("run2: " + i);
        });
    }
}
