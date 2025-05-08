package proyecto.nexpay.web.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTransactionExecutor {

    private final ScheduleTransactionManager scheduleTransactionManager;
    private final ScheduledExecutorService scheduler;

    public ScheduledTransactionExecutor(ScheduleTransactionManager scheduleTransactionManager) {
        this.scheduleTransactionManager = scheduleTransactionManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkAndExecuteScheduledTransactions, 0, 1, TimeUnit.MINUTES);
    }

    private void checkAndExecuteScheduledTransactions() {
        System.out.println("Checking scheduled transactions...");
        scheduleTransactionManager.processScheduledTransactions();
    }

    public void stop() {
        scheduler.shutdown();
    }
}

