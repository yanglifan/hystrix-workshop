package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo Hystrix issue#1853 https://github.com/Netflix/Hystrix/issues/1853
 */
public class HystrixIssue1853 {
    private static final int THRESHOLD = 1000;
    private static final int TOTAL = 6000000;
    private static final int SLEEP = 100;
    private static final int N_THREADS = 100;

    @Test
    public void main() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(N_THREADS);
        CountDownLatch countDownLatch = new CountDownLatch(TOTAL);

        for (int i = 0; i < TOTAL; i++) {
            executorService.execute(() -> new TestCommand(countDownLatch).execute());
        }

        countDownLatch.await();
    }

    static class TestCommand extends HystrixCommand<Void> {
        private static AtomicInteger count = new AtomicInteger();

        private volatile boolean hasAddCount = false;
        private CountDownLatch countDownLatch;

        TestCommand(CountDownLatch countDownLatch) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                    .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(N_THREADS)));
            this.countDownLatch = countDownLatch;
        }

        @Override
        protected Void run() throws Exception {
            try {
                Thread.sleep(SLEEP);

                if (this.circuitBreaker.isOpen()) {
                    System.out.println("Do attempt execution #" + count.get());
                }

                if (count.getAndIncrement() <= THRESHOLD) {
                    hasAddCount = true;
                    System.out.println("Cause a failure #" + count.get());
                    throw new Exception("Cause a failure");
                }

                System.out.println("success #" + count.get());
                return null;
            } finally {
                countDownLatch.countDown();
            }
        }

        @Override
        protected Void getFallback() {
            countDownLatch.countDown();
            if (!hasAddCount && count.getAndIncrement() % 50000 == 0) {
                System.out.println("Do fall back #" + count.get());
            }
            return null;
        }
    }
}
