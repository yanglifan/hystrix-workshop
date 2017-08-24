package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Yang Lifan
 */
public class AsyncUsageDemo {
    private static final HystrixCommandGroupKey ASYNC_USAGE_GROUP =
            HystrixCommandGroupKey.Factory.asKey("AsyncUsageDemo");

    @Test
    public void basic_demo() throws Exception {
        Future<String> future = new SimpleCommand().queue();
        assertThat(future.get(), is("success"));
    }

    class SimpleCommand extends HystrixCommand<String> {
        public SimpleCommand() {
            super(ASYNC_USAGE_GROUP);
        }

        @Override
        protected String run() throws Exception {
            Thread.sleep(500);
            return "success";
        }
    }
}
