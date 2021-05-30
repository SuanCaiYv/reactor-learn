package com.learn.reactor.test;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午5:16
 */
public class TestMain {

    public static void main(String[] args) {
        // 通过这个例子可以看出，Mono和Flux一样都是通过onNext()方法push消息的
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("subscribe");
                s.request(1);
            }

            @Override
            public void onNext(String s) {
                System.out.println("called");
                System.out.println(s);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t);
            }

            @Override
            public void onComplete() {
                System.out.println("completed");
            }
        };
        Mono.just("aaa")
                .subscribe(subscriber);
    }
}
