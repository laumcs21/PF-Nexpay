package proyecto.nexpay.web.service;

import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.datastructures.*;

public class TransactionValidator {

    public static boolean verifyTransaction(Transaction transaction, SimpleList<Account> accounts) {
        Account sourceAccount = findAccount(transaction.getSourceAccountNumber(), accounts);
        Account destinationAccount = findAccount(transaction.getDestinationAccountNumber(), accounts);

        if (sourceAccount == null) {
            System.out.println("Cuenta de origen no encontrada para la transacción " + transaction.getId());
            return false;
        }

        if (transaction.getType() == TransactionType.TRANSFERENCIA && destinationAccount == null) {
            System.out.println("Cuenta de destino no encontrada para la transacción " + transaction.getId());
            return false;
        }

        // Verifica que los saldos sean consistentes
        double calculatedSourceBalance = sourceAccount.getBalance();
        if (transaction.getType() == TransactionType.TRANSFERENCIA) {
            double calculatedDestinationBalance = destinationAccount.getBalance();
            if (transaction.getAmount() > calculatedSourceBalance) {
                System.out.println("Fondos insuficientes para la transacción " + transaction.getId());
                return false;
            }
        }
        return true;
    }

    private static Account findAccount(String accountNumber, SimpleList<Account> accounts) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    public static void verifyAllTransactions(DoubleLinkedList<Transaction> transactions, SimpleList<Account> accounts, int index) {
        if (index >= transactions.getSize()) {
            return; // Si hemos llegado al final, terminamos
        }

        Transaction transaction = transactions.get(index);

        boolean isValid = verifyTransaction(transaction, accounts);

        if (!isValid) {
            System.out.println("Transacción inválida: " + transaction.getId());
        } else {
            System.out.println("Transacción válida: " + transaction.getId());
        }

        // Llamada recursiva para verificar la siguiente transacción
        verifyAllTransactions(transactions, accounts, index + 1);
    }
}

