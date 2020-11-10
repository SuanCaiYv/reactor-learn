package com.learn.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author Mr.M
 */
public class FluxWithPublishOnSubscribeOn {

    public static void main(String[] args) throws InterruptedException {
        // 来看看publishOn()和subscribeOn()方法。
        // publishOn()就和普通操作一样，添加在操作链的中间，它会影响在它下面的所有操作的执行上下文。看个例子：
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> flux = Flux
                .range(1, 2)
                // map肯定是跑在T上的。
                .map(i -> 10 + i)
                // 此时的执行上下文被切换到了并行线程
                .publishOn(s)
                // 这个map还是跑在并行线程上的，因为publishOn()的后面的操作都被切换到了另一个执行上下文中。
                .map(i -> "value " + i);
        // 假设这个new出来的线程名为T
        new Thread(() -> flux.subscribe(System.out::println));
        // subscribeOn()方法会把订阅之后的整个订阅链都切换到新的执行上下文中。
        // 无论在subscribeOn()哪里，都可以把最前面的订阅之后的订阅序列进行切换，当然了，如果后面还有publishOn()，publishOn()会进行新的切换。
        // 依旧是创建一个并行线程
        Scheduler ss = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> fluxflux = Flux
                .range(1, 2)
                // 不过这里的map就已经在ss里跑了
                .map(i -> 10 + i)
                // 这里切换，但是切换的是整个链
                .subscribeOn(s)
                // 这里的map也运行在ss上
                .map(i -> "value " + i);
        // 这是一个匿名线程TT
        new Thread(() -> fluxflux.subscribe(System.out::println));
        Thread.sleep(1000);
    }
}
