package com.learn.reactor.flux;

import reactor.core.scheduler.Schedulers;

/**
 * @author Mr.M
 */
public class FluxWithSchedulers {

    public static void main(String[] args) throws InterruptedException {
        // 一般来说，响应式框架都不支持并发，P.s. create那个是生产者并发，它本身不是并发的。
        // 所以也没有可用的并发库，需要开发者自己实现。
        // 同时，每一个操作一般都是在上一个操作所在的线程里运行，它们不会拥有自己的线程，而最顶的操作则是和subscribe()在同一个线程。
        // 比如Flux.create(...).handle(...).subscribe(...)都在主线程运行的。
        // 在响应式框架里，Scheduler决定了操作在哪个线程被怎么执行，它的作用类似于ExecutorService。不过功能稍微多点。
        // 如果你想实现一些并发操作，那么可以考虑使用Schedulers提供的静态方法，来看看有哪些可用的：
        // Schedulers.immediate(): 直接在当前线程提交Runnable任务，并立即执行。
        System.out.println("当前线程：" + Thread.currentThread().getName());
        System.out.println("zxcv");
        Schedulers.immediate().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("qwer");
        });
        System.out.println("asdf");
        // 通过上面看得出，immediate()其实就是在执行位置插入需要执行的Runnable来实现的。和直接把代码写在这里没什么区别。
        // 另一个方法，Schedulers.single()，它的作用是为当前操作开辟一个新的线程，但是记住，所有使用这个方法的操作都共用一个线程；
        // 如果你想让每次调用都是一个新的线程的话，可以使用Schedulers.newSingle()，它可以保证每次执行的操作都使用的是一个新的线程。
        Schedulers.single().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("bnmp");
        });
        Schedulers.single().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("ghjk");
        });
        Schedulers.newSingle("线程1").schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("1234");
        });
        Schedulers.newSingle("线程1").schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("5678");
        });
        Schedulers.newSingle("线程2").schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("0100");
        });
        // 还有一个，Schedulers.elastic()，它是一个弹性无界线程池，不过，随着Schedulers.bounededElastic()的引入，它不再是首选，因为它可能会导致负压问题和过多的线程被创建。
        // 另一个，Schedulers.boundedElastic()是一个更好的选择，因为它可以在需要的时候创建工作线程池，并复用空闲的池；同时，某些池如果空闲时间超过一个限定的数值就会被抛弃。
        // 同时，它还有一个容量限制，一般10倍于CPU核心数，这是它后备线程池的最大容量。最多提交10万条任务，然后会被装进任务队列，等到有可用时再调度，如果是延时调度，那么延时开始时间是在有线程可用时才开始计算。
        // 由此可见Schedulers.boundedElastic()对于阻塞的I/O操作是一个不错的选择，因为它可以让每一个操作都有自己的线程。但是记得，太多的线程会让系统备受压力。
        Schedulers.boundedElastic().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("1478");
        });
        Schedulers.boundedElastic().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("2589");
        });
        Schedulers.boundedElastic().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("0363");
        });
        // 最后，Schedulers.parallel()提供了并行的能力，它会创建数量等于CPU核心数的线程来实现这一功能。
        Schedulers.parallel().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("6541");
        });
        Schedulers.parallel().schedule(() -> {
            System.out.println("当前线程是：" + Thread.currentThread().getName());
            System.out.println("9874");
        });
        // 顺带一提，还可以通过ExecutorService创建新的Scheduler。
        // 当然，Schedulers的一堆newXXX方法也可以。
        // 有一点很重要，就是boundedElastic()方法可以适用于传统阻塞式代码，但是single()和parallel()都不行，如果你非要这么做那就会抛异常。
        // 自定义Schedulers可以通过设置ThreadFactory属性来设置接收的线程是否是被NonBlocking接口修饰的Thread实例。

        // Flux的某些方法会使用默认的Scheduler，比如Flux.interval()方法就默认使用Schedulers.parallel()方法，当然可以通过设置Scheduler来更改这种默认。

        // 在响应式链中，有两种方式可以切换执行上下文，分别是publishOn()和subscribeOn()方法，前者在流式链中的位置很重要。
        // 在Reactor中，可以以任意形式添加任意数量的订阅者来满足你的需求，但是，只有在设置了订阅方法后，才能激活这条订阅链上的全部对象。只有这样，请求才会上溯到发布者，进而产生源序列。
        // 确保异步任务可以打印出来
        Thread.sleep(1000);
    }
}
