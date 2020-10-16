package com.learn.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午8:59
 */
public class FluxWithBaseSubscriber {

    public static void main(String[] args) {
        Flux<Integer> integerFlux = Flux.range(0, 10);
        // 这是对于lambda表达式的一种替换
        integerFlux.subscribe(new MySubscriber());
    }

    /**
     * 一般来说，通过继承BaseSubscriber<T>来实现，而且一般自定义hookOnSubscribe()和hookOnNext()方法
     */
    private static class MySubscriber extends BaseSubscriber<Integer> {

        /**
         * 初次订阅时被调用
         */
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            System.out.println("开始啦！");
            // 记得至少请求一次，否则不会执行hookOnNext()方法
            request(1);
        }

        /**
         * 每次读取新值调用
         */
        @Override
        protected void hookOnNext(Integer value) {
            System.out.println("开始读取...");
            System.out.println(value);
            // 指出下一次读取多少个
            request(2);
        }

        @Override
        protected void hookOnComplete() {
            System.out.println("结束啦");
        }
    }
}
