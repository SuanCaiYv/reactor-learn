package com.learn.reactor.flux;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author SuanCaiYv
 * @time 2020/10/10 下午10:03
 */
public class FluxWithGenerate {

    public static void main(String[] args) {
        // 现在到了程序化生成Flux/Mono的时候。首先介绍generate()方法，这是一个同步的方法。
        // 言外之意就是，它是线程不安全的，且它的接收器只能一次一个的接受输入来生成Flux/Mono。也就是说，它在任意时刻只能被调用一次且只接受一个输入。
        // 或者这么说，它生成的元素序列的顺序，取决于代码编写的方式。
        // 下面这个是它的变种方法之一：第一个参数是提供初始状态的，第二个参数是一个向接收器写入数据的生成器，入参为state(一般为整数，用来记录状态)，和接收器。
        // 其他变种请看源码
        Flux.generate(() -> 0, (state, sink) -> {
            sink.next(state+"asdf");
            // 加上对于sink.complete()的调用即可终止生成；否则就是无限序列。
            return state+1;
        }).subscribe(System.out::println);
        // 通过上述代码不难看到，每次的接收器接受的值来自于上一次生成方法的返回值，也就是state=上一个迭代的返回值(其实称为上一个流才准确，这么说只是为了方便理解)。
        // 不过这个state每次都是一个全新的(每次都+1当然是新的)，那么有没有什么方法可以做到前后两次迭代的state是同一个引用且还可以更新值呢？答案就是原子类型。-
        // generate方法的第三个参数用于结束生成时被调用，消耗state。
        Flux.generate(AtomicInteger::new, (state, sink) -> {
            sink.next(state.getAndIncrement()+"qwer");
            return state;
        }).subscribe(System.out::println);
        // generate()的工作流看起来就像:next()->next()->next()->...
    }
}
