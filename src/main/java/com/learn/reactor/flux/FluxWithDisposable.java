package com.learn.reactor.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Random;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午8:23
 */
public class FluxWithDisposable {

    public static void main(String[] args) {
        Disposable disposable = getDis();
        // 每次打印数量一般不同，因为调用了disposable的dispose()方法进行了取消，不过如果生产者产地太快了，那么可能来不及终止。
        // Disposables.composite()会得到一个Disposable的集合，调用它的dispose()方法会把集合里的所有Disposable的dispose()方法都调用。
        // 详见:https://projectreactor.io/docs/core/release/reference/#producing
        disposable.dispose();
    }

    private static Disposable getDis() {
        class Add implements Runnable {

            private final FluxSink<Integer> fluxSink;

            public Add(FluxSink<Integer> fluxSink) {
                this.fluxSink = fluxSink;
            }

            @Override
            public synchronized void run() {
                fluxSink.next(new Random().nextInt());
            }
        }
        Flux<Integer> integerFlux = Flux.create(integerFluxSink -> {
            Add add = new Add(integerFluxSink);
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
            new Thread(add).start();
        });
        return integerFlux.subscribe(System.out::println);
    }
}
