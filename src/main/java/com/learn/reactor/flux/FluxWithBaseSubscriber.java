package com.learn.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午8:59
 *
 * 现在是2021/05/29，我写了一堆丑炸了的SpringGateway代码，意识到了一件事，就是我的ReactorProject似乎学的跟拉垮。
 * <br/>
 * 我现在在这里重新逼逼两句关于如何新建一个Publisher的问题，Mono和Flux本质都是Publisher。
 * <br/>
 * 首先我们知道，Publisher总是和Subscriber搭配使用，所以很容易理解Publisher的唯一的需要被实现的方法：subscribe为什么需要一个Subscriber作为参数了。
 * <br/>
 * 因为不被订阅的发布者压根没有存在的意义，当一个发布者被某一个订阅者订阅了后，发布者就可以通过调用订阅者的方法实现向订阅者发布消息；
 * <br/>
 * 比如调用订阅者的onNext(T t)方法就可以告诉订阅者：我现在给你一个消息啦！你自己处理吧！，调用onComplete()就是告诉订阅者：我结束啦！你自己处理尾事吧！
 * <br/>
 * 再比如调用onError()就是告诉订阅者：我出错啦！你收不到我的消息了，但是可以收到我的一个异常，你要处理哦！
 * <br/>
 * 所以如果想要实现原生发布订阅功能，需要新建一个订阅者，设置它接收消息、接收完成信号、接收错误信号的行为；然后把它添加到一个发布者上去，就完成了。
 * <br/>
 * 发布者在subscribe()方法中调用订阅者的方法并传参实现消息发布。
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
