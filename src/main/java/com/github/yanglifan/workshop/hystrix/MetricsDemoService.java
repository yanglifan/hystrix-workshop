package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MetricsDemoService {
    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private void doService() throws InterruptedException {
        Random random = new Random();
        int sleep = random.nextInt(1000);
        Thread.sleep(sleep);
    }

    @PostConstruct
    public void init() throws InterruptedException {
        HystrixMetricsReporter.doReport();

        for (int i = 0; i < 1000000; i++) {
            Thread.sleep(10);
            executorService.execute(() -> new DemoServiceCommand().execute());
        }

    }

    class DemoServiceCommand extends HystrixCommand<Void> {
        DemoServiceCommand() {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("MetricsDemoService"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withExecutionTimeoutInMilliseconds(500)
                    )
            );
        }

        @Override
        protected Void run() throws Exception {
            MetricsDemoService.this.doService();
            return null;
        }

        @Override
        protected Void getFallback() {
            // DO NOTHING
            return null;
        }
    }
}
