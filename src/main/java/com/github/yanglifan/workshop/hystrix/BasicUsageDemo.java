package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Yang Lifan
 */
public class BasicUsageDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicUsageDemo.class);

    private static final HystrixCommandGroupKey BASIC_USAGE_GROUP =
            HystrixCommandGroupKey.Factory.asKey("BasicUsage");

    @Test
    public void demo_basic_usage() {
        String result = new BasicCommand("Stark").execute();
        assertThat(result, is("Hello, Stark"));
    }

    @Test
    public void demo_enum_group_key() {
        new EnumGroup1Command().execute();
        new EnumGroup2Command().execute();
    }

    @Test
    public void show_fallback_thread() {
        new BasicFallbackCommand().execute();
    }

    public enum Groups implements HystrixCommandGroupKey {
        GROUP_1, GROUP_2
    }

    static class EnumGroup1Command extends HystrixCommand<String> {
        EnumGroup1Command() {
            super(Groups.GROUP_1);
        }

        @Override
        protected String run() throws Exception {
            LOGGER.info("Thread of Command 1: {}", Thread.currentThread().getName());
            return null;
        }
    }

    static class EnumGroup2Command extends HystrixCommand<String> {
        EnumGroup2Command() {
            super(Groups.GROUP_2);
        }

        @Override
        protected String run() throws Exception {
            LOGGER.info("Thread of Command 2: {}", Thread.currentThread().getName());
            return null;
        }
    }

    class BasicCommand extends HystrixCommand<String> {
        private String name;

        BasicCommand(String name) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("MyService"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("MyThreadPool"))
                    .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                            .withCoreSize(10)
                            .withKeepAliveTimeMinutes(1)
                            .withMaxQueueSize(-1)
                    )
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withExecutionTimeoutInMilliseconds(100)
                    ));
            this.name = name;
        }

        @Override
        protected String run() throws Exception {
            return "Hello, " + name;
        }
    }

    class BasicFallbackCommand extends HystrixCommand<Void> {
        BasicFallbackCommand() {
            super(Setter.withGroupKey(BASIC_USAGE_GROUP)
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withFallbackIsolationSemaphoreMaxConcurrentRequests(1)
                    ));
        }

        @Override
        protected Void run() throws Exception {
            LOGGER.info("Thread in run() is {}", Thread.currentThread().getName());
            throw new RuntimeException("Cause failure");
        }

        @Override
        protected Void getFallback() {
            LOGGER.info("Thread in getFallback() is {}", Thread.currentThread().getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //
            }
            return null;
        }
    }
}
