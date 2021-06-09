package com.learn.reactor.flux;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

/**
 * @author CodeWithBuff(给代码来点Buff !)
 * @time 2021/5/31 4:18 下午
 *
 * transform用来接收一个Function类型，实现从Mono/Flux=>Mono/Flux的转换。
 *
 * 所以它常被用来包装操作符，我们可以把那些可以被复用的操作符包装成一个Function，然后传入到transform里去就可以了。
 *
 * 通过观察发现，transform只会在装配流水线时调用，且无论订阅多少次，transform仅会被调用一次！
 *
 * 通过观察发现，transformDeferred会在每次订阅被调用，无论它在流水线中哪个位置！
 *
 * 这是二者最明显区别，具体实现我们不表，仅仅记住这个结论，哪怕transformDeferred后面跟着transform，这个transform也仅仅会在装配时调用！
 *
 * 顺带一提，在Flux中，一个序列流称为一整个序列，无论序列流有多少个元素，都只会触发一次流水线上的任何操作符。
 *
 * 言外之意就是操作符操作的是整个序列，而不是序列的单个元素，虽然我们写的时候，操作符是针对每个元素进行操作的，但是从整体上看，一个操作符只会服务一次一个序列。
 *
 * 操作符只是对序列中的每个元素施以操作罢了，但是不代表有多少个元素这个操作方法就会被调用多少次，它只会被调用一次，然后在操作符内部依次处理每个元素。
 */
public class FluxWithReuse {

    public static void main(String[] args) {
        AtomicInteger atomicInteger1 = new AtomicInteger(0);

        Function<Flux<String>, Flux<String>> t1 = input -> {
            System.out.println("curr1: " + System.currentTimeMillis());
            System.out.println("int1: " + atomicInteger1.getAndIncrement());
            return input.map(p1 -> p1.toUpperCase());
        };

        AtomicInteger atomicInteger2 = new AtomicInteger(0);

        Function<Flux<String>, Flux<String>> t2 = input -> {
            System.out.println("curr2: " + System.currentTimeMillis());
            System.out.println("--------int2: " + atomicInteger2.getAndIncrement());
            return input.map(p1 -> (p1 + "_new").toUpperCase());
        };

        AtomicInteger atomicInteger3 = new AtomicInteger(0);

        Flux<String> flux0 = Flux.push(fluxSink -> {
            System.out.println("----------------int3: " + atomicInteger3.getAndIncrement());
            fluxSink.next(UUID.randomUUID().toString().substring(0, 5));
            fluxSink.complete();
        });

        Flux<String> flux1 = flux0.transform(t1);
        Flux<String> flux2 = flux0.transformDeferred(t1);

        flux1.subscribe(p1 -> {
            System.out.println("subscribe1: " + p1);
        });
        flux1.subscribe(p2 -> {
            System.out.println("subscribe2: " + p2);
        });
        flux2.subscribe(p3 -> {
            System.out.println("subscribe3: " + p3);
        });
        flux2.subscribe(p4 -> {
            System.out.println("subscribe4: " + p4);
        });

        System.out.println("#######################");

        System.out.println("curr: " + System.currentTimeMillis());
        Flux<String> flux3 = flux0.transform(t1).transformDeferred(t2);
        Flux<String> flux4 = flux0.transformDeferred(t1).transform(t2);
        LockSupport.parkNanos(Duration.ofMillis(300).toNanos());

        // 输出的int2: 0对应flux4的transform，而不是flux3的
        // transform的装配发生在书写时，transformDeferred的装配发生在订阅时；所以有几次订阅就有几次transformDeferred，而只有一次transform
        // transform和transformDeferred的顺序和位置不影响以上规则
        // 对于transform，只要你写了，它的装配就是立刻发生的，而transformDeferred则只有当你订阅时才会发生装配，每次订阅都会装配
        flux3.subscribe(p1 -> {
            System.out.println("subscribe5: " + p1);
        });
        flux3.subscribe(p2 -> {
            System.out.println("subscribe6: " + p2);
        });
        flux4.subscribe(p3 -> {
            System.out.println("subscribe7: " + p3);
        });
        flux4.subscribe(p4 -> {
            System.out.println("subscribe8: " + p4);
        });
    }
}
