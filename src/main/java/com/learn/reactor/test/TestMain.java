package com.learn.reactor.test;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午5:16
 */
public class TestMain {

    public static void main(String[] args) {
        // 说完了同步生成，接下来就是异步生成，还是多线程的！
        // 让我们有请:create()闪亮登场！！！
        // create()方法对外暴露出一个FluxSink对象，通过它我们可以访问并生成需要的序列。
        // 除此之外，它还可以触发回调中的多线程事件。
        // create另一特性就是很容易把其他的接口与响应式桥接起来。
        // 注意，它是异步多线程并不意味着create可以并行化你写的代码或者异步执行；怎么理解呢？就是，create方法里面的Lambda表达式代码还是单线程阻塞的。
        // 如果你在创建序列的地方阻塞了代码，那么可能造成订阅者即使请求了数据，也得不到，因为序列被阻塞了，没法生成新的。
        // 其实通过上面的现象可以猜测，默认情况下订阅者使用的线程和create使用的是一个线程，当然阻塞create就会导致订阅者没法运行咯！
        // 上述问题可以通过Scheduler解决，后面会提到。
        TestProcessor<String> testProcessor = new TestProcessor<>() {

            private TestListener<String> testListener;

            @Override
            public void register(TestListener<String> stringTestListener) {
                this.testListener = stringTestListener;
            }

            @Override
            public TestListener<String> get() {
                return testListener;
            }
        };
        Flux<String> flux = Flux.create(stringFluxSink -> testProcessor.register(new TestListener<String>() {
            @Override
            public void onChunk(List<String> chunk) {
                for (String s : chunk) {
                    stringFluxSink.next(s);
                }
            }

            @Override
            public void onComplete() {
                stringFluxSink.complete();
            }
        }));
        flux.subscribe(System.out::println);
        System.out.println("现在是2020/10/22 22:58；我好困");
        TestListener<String> testListener = testProcessor.get();
        Runnable1<String> runnable1 = new Runnable1<>() {

            private TestListener<String> testListener;

            @Override
            public void set(TestListener<String> testListener) {
                this.testListener = testListener;
            }

            @Override
            public void run() {
                List<String> list = new ArrayList<>(10);
                for (int i = 0; i < 10; ++ i) {
                    list.add(UUID.randomUUID().toString());
                }
                testListener.onChunk(list);
                testListener.onComplete();
            }
        };
        Runnable1<String> runnable2 = new Runnable1<>() {

            private TestListener<String> testListener;

            @Override
            public void set(TestListener<String> testListener) {
                this.testListener = testListener;
            }

            @Override
            public void run() {
                List<String> list = new ArrayList<>(10);
                for (int i = 0; i < 10; ++ i) {
                    list.add(UUID.randomUUID().toString());
                }
                testListener.onChunk(list);
                testListener.onComplete();
            }
        };
        Runnable1<String> runnable3 = new Runnable1<>() {

            private TestListener<String> testListener;

            @Override
            public void set(TestListener<String> testListener) {
                this.testListener = testListener;
            }

            @Override
            public void run() {
                List<String> list = new ArrayList<>(10);
                for (int i = 0; i < 10; ++ i) {
                    list.add(UUID.randomUUID().toString());
                }
                testListener.onChunk(list);
                testListener.onComplete();
            }
        };
        runnable1.set(testListener);
        runnable2.set(testListener);
        runnable3.set(testListener);
        new Thread(runnable1).start();
        new Thread(runnable2).start();
        new Thread(runnable3).start();
        // 另一方面，create的另一个变体可以设置参数来实现负压控制，具体看源码。
    }
    public interface TestListener<T> {

        void onChunk(List<T> chunk);

        void onComplete();
    }

    public interface TestProcessor<T> {

        void register(TestListener<T> tTestListener);

        TestListener<T> get();
    }

    public interface Runnable1<T> extends Runnable {
         void set(TestListener<T> testListener);
    }
}
