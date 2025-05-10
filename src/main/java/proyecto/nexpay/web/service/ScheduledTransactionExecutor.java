package proyecto.nexpay.web.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTransactionExecutor {

    private final ScheduledTransactionManager scheduleTransactionManager;
    private final ScheduledExecutorService scheduler;

    public ScheduledTransactionExecutor(ScheduledTransactionManager scheduleTransactionManager) {
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

