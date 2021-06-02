package com.learn.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/5/31 10:45 下午
 */
public class FluxColdAndHot {
    public static void main(String[] args) {
        Flux<Object> flux0 = Flux.push(p1 -> {
            for (int i = 0; i < 10; ++i) {
                // 模拟持续的生产
                LockSupport.parkNanos(Duration.ofMillis(1000).toNanos());
                p1.next(i + "th");
            }
            p1.complete();
        });
        // 这里把默认为Cold的Flux转换成Hot的Flux，同时为了展示，把元素发布转移到新的线程，避免被主线程阻塞
        Flux<Object> share0 = flux0.subscribeOn(Schedulers.boundedElastic()).share();
        // 此时立刻开启第一个订阅者，因为我们手动设置了发布元素的延迟时间，所以不出意外第一个订阅者可以订阅全部元素
        share0.subscribe(p1 -> {
            System.out.println("s1: " + p1);
        });
        // 注意这里，我们让第二个订阅者"迟"一会订阅，因为此时已经开始了元素的发布，所以可以观察到第二个订阅者订阅到的元素少了前几个
        LockSupport.parkNanos(Duration.ofMillis(2100).toNanos());
        share0.subscribe(p2 -> {
            System.out.println("s2: " + p2);
        });
        // 顺带一提，Mono.just()方法是一个Hot方法，你可以使用Mono.defer()来实现Cold版"just"！
        LockSupport.park();
    }
}
