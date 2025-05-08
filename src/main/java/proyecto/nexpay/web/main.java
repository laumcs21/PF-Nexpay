package proyecto.nexpay.web;

import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.service.*;
import proyecto.nexpay.web.persistence.*;
import proyecto.nexpay.web.datastructures.*;
import java.time.LocalDateTime;

import java.time.LocalDate;

public class main {

    public static void main(String[] args) {
        Nexpay nexpay = Nexpay.getInstance();

        LocalDateTime scheduledTime = LocalDateTime.of(2025, 5, 8, 16, 0, 0, 0);

        Transaction transaction = new Transaction.Builder("001", "trans002", LocalDate.now(), TransactionType.DEPOSITO, 100000, "4355760746")
                .withScheduledDate(scheduledTime)
                .build();
        nexpay.getSManager().scheduleTransaction(transaction, scheduledTime);

        System.out.println("Scheduled Transactions: ");
        nexpay.getSManager().printScheduledTransactions();

        nexpay.getSManager().processScheduledTransactions();
    }
}

