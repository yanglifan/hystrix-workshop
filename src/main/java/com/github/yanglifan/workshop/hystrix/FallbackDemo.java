package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Yang Lifan
 */
public class FallbackDemo {
    private static final HystrixCommandGroupKey GROUP =
            HystrixCommandGroupKey.Factory.asKey("FallbackDemo");
    private static final HystrixCommandGroupKey SINGLE_THREAD =
            HystrixCommandGroupKey.Factory.asKey("FallbackDemo.SingleThread");

    /**
     * getFallback cannot be controlled by timeout configure.
     */
    @Test
    public void fallback_will_not_timeout() {
        FallbackTimeoutCommand command = new FallbackTimeoutCommand();
        String result = command.execute();
        assertThat(result, is("fallback"));
    }

    @Test
    public void fallback_by_thread_pool_reject() {
        SingleThreadPoolCommand command1 = new SingleThreadPoolCommand();
        command1.queue();

        SingleThreadPoolCommand command2 = new SingleThreadPoolCommand();
        String result = command2.execute();
        assertThat(result, is("fallback"));
    }

    class SingleThreadPoolCommand extends HystrixCommand<String> {
        SingleThreadPoolCommand() {
            super(Setter.withGroupKey(SINGLE_THREAD)
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter().withCoreSize(1)
                    )
            );
        }

        @Override
        protected String run() throws Exception {
            Thread.sleep(500);
            return "hello";
        }

        @Override
        protected String getFallback() {
            return "fallback";
        }
    }

    class FallbackTimeoutCommand extends HystrixCommand<String> {
        public FallbackTimeoutCommand() {
            super(GROUP, 1000);
        }

        @Override
        protected String run() throws Exception {
            System.out.println("Print the thread name in run() " + Thread.currentThread().getName());
            throw new RuntimeException("test");
        }

        @Override
        protected String getFallback() {
            System.out.println("Print the thread name in getFallback() " + Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "fallback";
        }
    }
}
