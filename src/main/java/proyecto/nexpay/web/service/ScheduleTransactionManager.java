package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.PriorityQueue;
import proyecto.nexpay.web.model.Transaction;

import java.time.LocalDateTime;

public class ScheduleTransactionManager {

    private TransactionManager transactionManager;
    private PriorityQueue<ScheduledTransaction> scheduledTransactions;

    public ScheduleTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.scheduledTransactions = new PriorityQueue<>();
    }

    public void scheduleTransaction(Transaction transaction, LocalDateTime date) {
        scheduledTransactions.enqueue(new ScheduledTransaction(transaction, date));
        System.out.println("Transaction scheduled for: " + date);
    }

    public void processScheduledTransactions() {
        LocalDateTime today = LocalDateTime.now();
        while (!scheduledTransactions.isEmpty() && !scheduledTransactions.getHead().getData().getScheduledDate().isAfter(today)) {
            ScheduledTransaction scheduled = scheduledTransactions.dequeue();
            transactionManager.executeTransaction(scheduled.getTransaction());
        }
    }

    public void printScheduledTransactions() {
        for (ScheduledTransaction st : scheduledTransactions) {
            System.out.println(st);
        }
    }
}
