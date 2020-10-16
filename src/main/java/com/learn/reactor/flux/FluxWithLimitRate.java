package com.learn.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author SuanCaiYv
 * @time 2020/10/10 下午8:26
 */
public class FluxWithLimitRate {

    public static void main(String[] args) {
        Flux<Integer> integerFlux = Flux.range(0, 100);
        // 为了缓解订阅者压力，订阅者可以通过负压流回溯进行重塑发布者发布的速率。
        // 最典型的用法就是下面这个——通过继承BaseSubscriber来设置自己的请求速率。
        // 但是有一点必须明确，就是hookOnSubscribe()方法必须至少请求一次，不然你的发布者可能会“卡住”。
        integerFlux.subscribe(new MySubscriber());

        // 另一点，某些上流的操作符(或翻译成操作方法)可以更改下流订阅者的请求速率，有一些操作符(操作方法)有一个prefetch整型作为输入，可以获取大于下流订阅者请求的数量的序列元素，这样做是为了处理它们自己的内部序列。
        // 这些预获取的操作方法一般默认预获取32个，不过为了优化；每次已经获取了预获取数量的75%的时候，会再获取75%。这叫“补充优化”。
        // 最后，来看一些Flux提供的预获取方法：
        // 指出预取数量
        integerFlux.limitRate(10);
        // lowTide指出预获取操作的补充优化的值，即修改75%的默认值；highTide指出预获取数量。
        integerFlux.limitRate(10, 15);
        // 哎～最典型的就是，请求无数:request(Long.MAX_VALUE)但是我给你limitRate(2)；那你也只能乖乖每次得到两个哈哈哈哈！
        // 还有一个就是limitRequest(N)，它会把下流总请求限制为N。如果下流请求超过了N，那么只返回N个，否则返回实际数量。然后认为请求完成，向下流发送onComplete信号。
        integerFlux.limitRequest(5).subscribe(new MySubscriber());
        // 上面这个只会输出5个。
    }

    private static class MySubscriber extends BaseSubscriber<Integer> {

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            System.out.println("开始啦！");
            // 记得至少请求一次，否则不会执行hookOnNext()方法
            request(1);
        }

        @Override
        protected void hookOnNext(Integer value) {
            System.out.println("开始读取...");
            System.out.println(value);
            // 指出下一次读取多少个
            request(2);
        }

        @Override
        protected void hookOnComplete() {
            System.out.println("结束啦！");
        }
    }
}
