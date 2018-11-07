package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommandMetrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HystrixMetricsReporter {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void doReport() {
        scheduler.scheduleAtFixedRate(() -> {
            for (HystrixCommandMetrics commandMetrics : HystrixCommandMetrics.getInstances()) {
                getCommendMetrics(commandMetrics);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private static void getCommendMetrics(HystrixCommandMetrics commandMetrics) {
        String commandName = commandMetrics.getCommandKey().name();

    }

    class CommandErrorPercentage implements Comparable<CommandErrorPercentage> {
        private String commandName;
        private Integer errorPercentage;

        public CommandErrorPercentage(String commandName, Integer errorPercentage) {
            this.commandName = commandName;
            this.errorPercentage = errorPercentage;
        }

        public String getCommandName() {
            return commandName;
        }

        public Integer getErrorPercentage() {
            return errorPercentage;
        }

        @Override
        public int compareTo(CommandErrorPercentage o) {
            return this.errorPercentage.compareTo(o.errorPercentage);
        }
    }
}
