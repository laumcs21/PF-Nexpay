package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.DoubleLinkedList;
import proyecto.nexpay.web.datastructures.PriorityQueue;
import proyecto.nexpay.web.model.Transaction;
import proyecto.nexpay.web.persistence.TransactionPersistence;

import java.io.IOException;
import java.time.LocalDateTime;

public class ScheduledTransactionManager {
    private TransactionManager transactionManager;
    private PriorityQueue<ScheduledTransaction> scheduledTransactions;
    TransactionPersistence Pt = TransactionPersistence.getInstance();

    public PriorityQueue<ScheduledTransaction> getScheduledTransactions() {
        return scheduledTransactions;
    }

    public ScheduledTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.scheduledTransactions = new PriorityQueue<>();
        loadScheduledTransactions();
    }

    public void scheduleTransaction(Transaction transaction, LocalDateTime date) {
        ScheduledTransaction scheduledTransaction = new ScheduledTransaction(transaction, date);
        scheduledTransactions.enqueue(scheduledTransaction);
        Pt.saveScheduledTransaction(scheduledTransaction);
        System.out.println("Transaction scheduled for: " + date);
    }

    public void processScheduledTransactions() {
        LocalDateTime today = LocalDateTime.now();
        System.out.println("Checking scheduled transactions at: " + today);

        while (!scheduledTransactions.isEmpty()) {
            ScheduledTransaction scheduled = scheduledTransactions.getHead().getData();
            System.out.println("Scheduled for: " + scheduled.getScheduledDate());

            if (!scheduled.getScheduledDate().isAfter(today)) {
                try {
                    transactionManager.executeTransaction(scheduled.getTransaction());
                    scheduledTransactions.dequeue();
                    //Pt.removeScheduledTransaction(scheduled.getTransaction().getId());

                    System.out.println("Executed transaction: " + scheduled.getTransaction());
                } catch (Exception e) {
                    System.err.println("Error executing transaction: " + scheduled.getTransaction() + " - " + e.getMessage());
                }
            } else {
                System.out.println("Transaction not yet scheduled: " + scheduled.getTransaction());
                break;
            }
        }
    }



    public void printScheduledTransactions() {
        for (ScheduledTransaction st : scheduledTransactions) {
            System.out.println(st);
        }
    }

    private void loadScheduledTransactions() {
        try {
            DoubleLinkedList<ScheduledTransaction> loadedTransactions = Pt.loadScheduledTransactions();
            for (ScheduledTransaction st : loadedTransactions) {
                scheduledTransactions.enqueue(st);
            }
        } catch (IOException e) {
            System.err.println("Error loading scheduled transactions: " + e.getMessage());
        }
    }
}





