package com.learn.reactor.flux;

import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午5:16
 */
public class FluxWithHandle {

    public static void main(String[] args) {
        // 在Flux的实例方法里，还有一种类似filter和map的操作：handle
        Flux<String> stringFlux = Flux.push(stringFluxSink -> {
            for (int i = 0; i < 10; ++ i) {
                stringFluxSink.next(UUID.randomUUID().toString().substring(0, 5));
            }
        });
        // 获取所有包含'a'的串
        Flux<String> flux = stringFlux.handle((str, sink) -> {
            String s = f(str);
            if (s != null) {
                sink.next(s);
            }
        });
        flux.subscribe(System.out::println);
    }

    private static String f(String str) {
        return str.contains("a") ? str : null;
    }
}
