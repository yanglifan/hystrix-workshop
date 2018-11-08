package com.github.yanglifan.workshop.hystrix;

import com.netflix.hystrix.HystrixCommandMetrics;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HystrixMetricsReporter {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void doReport() {
        scheduler.scheduleAtFixedRate(() -> {
            SortedSet<CommandErrorPercentage> topErrorPercentages = buildCommandErrorPercentages();
            log(topErrorPercentages);
        }, 0, 5, TimeUnit.SECONDS);
    }

    private static void log(SortedSet<CommandErrorPercentage> topErrorPercentages) {
        int i = 1;
        System.out.println("========== Command Error Percentage List ==========");
        for (CommandErrorPercentage errorPercentage : topErrorPercentages) {
            System.out.println(i++ + errorPercentage.getCommandName() + ": " + errorPercentage.getErrorPercentage() + "%");
        }
    }

    private static SortedSet<CommandErrorPercentage> buildCommandErrorPercentages() {
        SortedSet<CommandErrorPercentage> topErrorPercentages = new TreeSet<>();

        for (HystrixCommandMetrics commandMetrics : HystrixCommandMetrics.getInstances()) {
            CommandErrorPercentage commandErrorPercentage = new CommandErrorPercentage(commandMetrics);
            topErrorPercentages.add(commandErrorPercentage);
        }

        return topErrorPercentages;
    }

    static class CommandErrorPercentage implements Comparable<CommandErrorPercentage> {
        private String commandName;
        private Integer errorPercentage;

        public CommandErrorPercentage(HystrixCommandMetrics commandMetrics) {
            this.commandName = commandMetrics.getCommandKey().name();

            HystrixCommandMetrics.HealthCounts healthCounts = commandMetrics.getHealthCounts();
            this.errorPercentage = healthCounts.getErrorPercentage();
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
