package com.learn.reactor.test;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

/**
 * @author 十三月之夜
 * @time 2021/5/30 1:26 下午
 * 模拟ReactiveRedis实现get()方法，了解并学习Redis标准库是怎么返回Mono<T>类型的值
 *
 * 注意⚠️，我可能理解错了，但是看源码理解是，ReactiveRedis使用一个Scheduler去执行操作命令。
 * 所以这是开辟了新的线程去跑get()方法，然后就可以做到不阻塞主线程。
 *
 * 其实想想也只能这么做，如果发布者产生item需要很久的话，如果不异步执行，那无论怎么做都没法不阻塞当前线程，所以如果发布者
 * 产出一个item需要很久的话，那么就把它扔到线程池去异步执行，并且绑上一系列操作，完成回调的功能，其实就是push操作，主动告知订阅者消息可用并发布消息。
 * 一直都是我想复杂了，其实实现起来没有那么的复杂。
 */
public class MyReactiveRedis {

    static ExecutorService executorService = Executors.newCachedThreadPool();

    private static class OnNext implements Runnable {

        private Subscriber<? super String> subscriber;

        @Override
        public void run() {
            LockSupport.parkNanos(Duration.ofMillis(5000).toNanos());
            subscriber.onNext(UUID.randomUUID().toString());
            OnComplete onComplete = new OnComplete();
            onComplete.subscriber = subscriber;
            executorService.submit(onComplete);
        }
    }

    private static class OnComplete implements Runnable {

        private Subscriber<?> subscriber;

        @Override
        public void run() {
            subscriber.onComplete();
        }
    }

    private static class MyPublisher implements Publisher<String> {

        @Override
        public void subscribe(Subscriber<? super String> s) {
            Subscription subscription = new Subscription() {
                @Override
                public void request(long n) {
                    for (int i = 0; i < n; ++ i) {
                        s.onNext(UUID.randomUUID().toString());
                    }
                }

                @Override
                public void cancel() {
                    ;
                }
            };
            OnNext onNext = new OnNext();
            onNext.subscriber = s;
            executorService.submit(onNext);
        }
    }

    private static class MySubscriber implements Subscriber<String> {

        @Override
        public void onSubscribe(Subscription s) {
            s.request(1);
        }

        @Override
        public void onNext(String s) {
            System.out.println("get: " + s);
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
            System.out.println("done");
        }
    }

    public static void main(String[] args) {
        MyPublisher myPublisher = new MyPublisher();
        MySubscriber mySubscriber = new MySubscriber();
        myPublisher.subscribe(mySubscriber);
        System.out.println("run");
    }
}
