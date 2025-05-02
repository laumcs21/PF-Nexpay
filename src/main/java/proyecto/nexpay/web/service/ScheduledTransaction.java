package proyecto.nexpay.web.service;

import proyecto.nexpay.web.model.Transaction;

import java.time.LocalDateTime;

public class ScheduledTransaction implements Comparable<ScheduledTransaction>{
    private Transaction transaction;
    private LocalDateTime scheduledDate;

    public ScheduledTransaction(Transaction transaction, LocalDateTime scheduledDate) {
        this.transaction = transaction;
        this.scheduledDate = scheduledDate;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    @Override
    public int compareTo(ScheduledTransaction other) {
        return this.scheduledDate.compareTo(other.getScheduledDate()); // Prioridad: menor fecha
    }

    @Override
    public String toString() {
        return "Scheduled for: " + scheduledDate + " -> " + transaction.toString();
    }

}

