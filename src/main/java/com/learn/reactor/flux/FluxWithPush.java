package com.learn.reactor.flux;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author SuanCaiYv
 * @time 2020/10/8 下午5:16
 */
public class FluxWithPush {

    public static void main(String[] args) throws InterruptedException {
        // 说完了异步多线程，同步的生成方法，接下来就是异步单线程：push
        // 其实说到push和create的对比，我个人理解如下：
        // create允许多线程环境下调用.next()方法，只管生成元素，元素序列的顺序取决于...算了，随机的，毕竟多线程；
        // 但是push只允许一个线程生产元素，所以是有序的，至于异步指的是在新的线程中也可以，而不必非得在当前线程。
        // 顺带一提，push和create都支持onCancel()和onDispose()操作。
        // 一般来说，onCancel只响应于cancel操作，而onDispose响应于error，cancel，complete等操作。
        TestProcessor<String> testProcessor = new TestProcessor<>() {

            private TestListener<String> testListener;

            @Override
            public void register(TestListener<String> testListener) {
                this.testListener = testListener;
            }

            @Override
            public TestListener<String> get() {
                return this.testListener;
            }
        };
        Flux<String> flux = Flux.push(stringFluxSink -> testProcessor.register(new TestListener<>() {
            @Override
            public void onChunk(List<String> list) {
                for (String s : list) {
                    stringFluxSink.next(s);
                }
            }

            @Override
            public void onComplete() {
                stringFluxSink.complete();
            }
        }));
        flux.subscribe(System.out::println);
        Runnable1<String> runnable = new Runnable1<>() {

            private TestListener<String> testListener;

            @Override
            public void set(TestListener<String> testListener) {
                this.testListener = testListener;
            }

            @Override
            public void run() {
                List<String> list = new ArrayList<>(10);
                for (int i = 0; i < 10; ++i) {
                    list.add(UUID.randomUUID().toString());
                }
                testListener.onChunk(list);
            }
        };
        TestListener<String> testListener = testProcessor.get();
        runnable.set(testListener);
        new Thread(runnable).start();
        Thread.sleep(15);
        testListener.onComplete();
        // 同create一样，push也支持负压调节
    }

    public interface TestListener<T> {
        void onChunk(List<T> list);
        void onComplete();
    }

    public interface TestProcessor<T> {
        void register(TestListener<T> testListener);
        TestListener<T> get();
    }

    public interface Runnable1<T> extends Runnable {
        void set(TestListener<T> testListener);
    }
}
