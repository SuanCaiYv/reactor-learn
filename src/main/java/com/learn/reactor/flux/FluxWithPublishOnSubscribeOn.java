package com.learn.reactor.flux;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * @author Mr.M
 */
public class FluxWithPublishOnSubscribeOn {

    public static void main(String[] args) throws InterruptedException {
        // 来看看publishOn()和subscribeOn()方法。
        // publishOn()就和普通操作一样，添加在操作链的中间，它会影响在它下面的所有操作的执行上下文。看个例子：
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> flux = Flux
                .range(1, 2)
                // map肯定是跑在T上的。
                .map(i -> {
                    System.out.println(Thread.currentThread().getName());
                    return 10 + i;
                })
                // 此时的执行上下文被切换到了并行线程
                .publishOn(scheduler)
                // 这个map还是跑在并行线程上的，因为publishOn()的后面的操作都被切换到了另一个执行上下文中。
                .map(i -> {
                    System.out.println(Thread.currentThread().getName());
                    return "value " + i;
                });
        // 假设这个new出来的线程名为T
        new Thread(() -> flux.subscribe(System.out::println));
        // subscribeOn()方法会把订阅之后的整个订阅链都切换到新的执行上下文中。
        // 这里的整个链包含Publisher发布者发布消息的线程也被一并切换了，也就是说整个的流水线都被切换到新的线程执行
        // 无论在subscribeOn()哪里，都可以把最前面的订阅之后的订阅序列进行切换，当然了，如果后面还有publishOn()，publishOn()会进行新的切换。
        // 依旧是创建一个并行线程
        Scheduler ss = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> fluxflux = Flux
                .range(1, 2)
                // 不过这里的map就已经在ss里跑了
                .map(i -> 10 + i)
                // 这里切换，但是切换的是整个链
                .subscribeOn(scheduler)
                // 这里的map也运行在ss上
                .map(i -> "value " + i);
        // 这是一个匿名线程TT
        new Thread(() -> fluxflux.subscribe(System.out::println));
        Supplier<String> supplier = () -> {
            return "asdf";
        };
        Publisher<String> publisher = s1 -> {
            Subscription subscription = new BaseSubscriber<>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1);
                }

                @Override
                protected void hookOnNext(Object value) {
                    // 仅请求一次就够了，所以下一次请求直接"完成"
                    onComplete();
                }

                @Override
                protected void hookOnComplete() {
                    // 此时不作任何处理
                }
            };
            s1.onSubscribe(subscription);
            s1.onNext("zxcv");
            s1.onComplete();
        };
        Mono.fromSupplier(supplier).subscribe(System.out::println);
        Mono.from(publisher).subscribe(System.out::println);

        // better example

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
