package com.learn.reactor.flux;

import reactor.core.publisher.Flux;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午7:37
 */
public class FluxIntegerWithSubscribe {

    public static void main(String[] args) {
        Flux<Integer> integerFlux = Flux.range(0, 10);
        // 在这里说明一下subscribe()第四个参数，指出了当订阅信号到达，初次请求的个数，如果是null则全部请求(Long.MAX_VALUE)
        // 其余subscribe()详见源码或文档:https://projectreactor.io/docs/core/release/reference/#flux
        integerFlux.subscribe(i -> {
            System.out.println("run");
            System.out.println(i);
        }, error -> {
            System.out.println("error");
        }, () -> {
            System.out.println("done");
        }, p -> {
            p.request(2);
        });
    }
}
