package com.example;

import javax.print.DocFlavor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConcurrencyTest {
    private static final Executor ex = Executors.newCachedThreadPool();


    public static void main(String[] args) throws InterruptedException {
        time(ex, 10, () -> {
            System.out.println("this is Thread " + Thread.currentThread().getName());
            System.out.println("now time is " + System.nanoTime());
            System.out.println("end --------");
        });

    }


    private static long time(Executor executors, int concurrency, final Runnable action) throws InterruptedException {
        final CountDownLatch ready = new CountDownLatch(concurrency);
        final CountDownLatch start = new CountDownLatch(1); //如何设定为concurrency,那么一旦await,当countDown=conCurrency,才会继续执行
        final CountDownLatch done = new CountDownLatch(concurrency);

        for (int i = 0; i < concurrency; i++) {
            executors.execute(() -> {
                ready.countDown(); //Tell timer we're ready

                try {
                    System.out.println("start waiting ....");
                    start.await();  //Wait till peers are ready
                    action.run(); //execute runnable
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown(); //Tell timer we're done
                }
            });
        }
        ready.await(); //Wait for all worker to be read
        long startNanos = System.nanoTime(); //get system time
        System.out.println("ready ......");
        start.countDown(); //And They're off !
        done.await(); //wait for all workers to finish
        long endNanos = System.nanoTime();
        return endNanos - startNanos;

    }
}
