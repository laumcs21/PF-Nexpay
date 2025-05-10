package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.Stack;
import proyecto.nexpay.web.model.*;

public class TransactionManager {

    private final Nexpay nexpay;
    private final TransactionCRUD transactionCRUD;
    private final AccountCRUD accountCRUD;
    private final Stack<Transaction> transactionStack;
    private final NotificationManager notificationManager;
    private boolean undoTransaction;

    public TransactionManager(Nexpay nexpay) {
        this.nexpay = nexpay;
        this.transactionCRUD = nexpay.getTransactionCRUD();
        this.accountCRUD = nexpay.getAccountCRUD();
        this.transactionStack = new Stack<>();
        this.notificationManager = new NotificationManager(nexpay);
        this.undoTransaction = false;
    }

    public boolean isUndoTransaction() {
        return undoTransaction;
    }

    public Transaction getLastRevertedTransaction() {
        if (transactionStack.isEmpty()) {
            return null;
        }
        return transactionStack.peek();
    }

    public void executeTransaction(Transaction transaction) {
        Account source = findAccount(transaction.getSourceAccountNumber());
        if (source == null) {
            throw new IllegalArgumentException("La cuenta de origen no fue encontrada.");
        }

        User user = nexpay.getUserCRUD().safeRead(transaction.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        if (!source.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("La cuenta de origen no pertenece al usuario");
        }

        switch (transaction.getType()) {
            case DEPOSITO:
                source.setBalance(source.getBalance() + transaction.getAmount());
                accountCRUD.update(source);
                break;
            case RETIRO:
                if (source.getBalance() < transaction.getAmount()) {
                    throw new IllegalArgumentException("Fondos insuficientes para realizar el retiro.");
                }
                source.setBalance(source.getBalance() - transaction.getAmount());
                accountCRUD.update(source);
                break;
            case TRANSFERENCIA:
                if (source.getBalance() < transaction.getAmount()) {
                    throw new IllegalArgumentException("Fondos insuficientes para realizar la transferencia.");
                }
                Account destination = findAccount(transaction.getDestinationAccountNumber());
                if (destination == null) {
                    throw new IllegalArgumentException("Cuenta de destino no encontrada.");
                }
                if (destination.getAccountNumber().equals(source.getAccountNumber())) {
                    throw new IllegalArgumentException("No se puede transferir a la misma cuenta de origen.");
                }

                source.setBalance(source.getBalance() - transaction.getAmount());
                destination.setBalance(destination.getBalance() + transaction.getAmount());

                accountCRUD.update(source);
                accountCRUD.update(destination);
                break;
            default:
                throw new IllegalArgumentException("Tipo de transacción no válido.");
        }

        double totalBalanceSource = 0.0;
        for (Account account : nexpay.getAccounts()) {
            if (account.getUserId().equals(user.getId())) {
                totalBalanceSource += account.getBalance();
            }
        }

        user.setTotalBalance(totalBalanceSource);

        nexpay.getUserCRUD().update(user);

        Account destinationAccount = findAccount(transaction.getDestinationAccountNumber());
        if (destinationAccount != null && !destinationAccount.getUserId().equals(user.getId())) {
            double totalBalanceDestination = 0.0;
            for (Account account : nexpay.getAccounts()) {
                if (account.getUserId().equals(destinationAccount.getUserId())) {
                    totalBalanceDestination += account.getBalance();
                }
            }

            User destinationUser = nexpay.getUserCRUD().safeRead(destinationAccount.getUserId());
            if (destinationUser != null) {
                destinationUser.setTotalBalance(totalBalanceDestination);
                nexpay.getUserCRUD().update(destinationUser);
            }
        }

        transactionCRUD.create(transaction);
        transactionStack.push(transaction);

        System.out.println("Transaction executed successfully.");
        System.out.println("Total balance for user " + user.getId() + ": " + totalBalanceSource);

        sendAutomaticNotifications(user);
    }

    private void sendAutomaticNotifications(User user) {
        notificationManager.checkLowBalance();
        notificationManager.checkScheduledTransactions();
        notificationManager.checkUndoTransaction();
        notificationManager.processNotifications();
    }

    public boolean undoLastTransaction() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transactions to undo.");
            return false;
        }

        Transaction transaction = transactionStack.pop();
        Account source = findAccount(transaction.getSourceAccountNumber());

        switch (transaction.getType()) {
            case DEPOSITO:
                source.setBalance(source.getBalance() - transaction.getAmount());
                accountCRUD.update(source);
                break;
            case RETIRO:
                source.setBalance(source.getBalance() + transaction.getAmount());
                accountCRUD.update(source);
                break;
            case TRANSFERENCIA:
                Account destination = findAccount(transaction.getDestinationAccountNumber());
                if (destination != null) {
                    source.setBalance(source.getBalance() + transaction.getAmount());
                    destination.setBalance(destination.getBalance() - transaction.getAmount());
                    accountCRUD.update(source);
                    accountCRUD.update(destination);
                } else {
                    System.out.println("Cuenta de destino no encontrada. No se puede revertir.");
                    return false;
                }
                break;
            default:
                System.out.println("Tipo de transacción inválido.");
                return false;
        }

        double totalBalanceSource = 0.0;
        for (Account account : nexpay.getAccounts()) {
            if (account.getUserId().equals(source.getUserId())) {
                totalBalanceSource += account.getBalance();
            }
        }
        User sourceUser = nexpay.getUserCRUD().safeRead(source.getUserId());
        sourceUser.setTotalBalance(totalBalanceSource);
        nexpay.getUserCRUD().update(sourceUser);
        Account destinationAccount = findAccount(transaction.getDestinationAccountNumber());
        if (destinationAccount != null && !destinationAccount.getUserId().equals(source.getUserId())) {
            double totalBalanceDestination = 0.0;
            for (Account account : nexpay.getAccounts()) {
                if (account.getUserId().equals(destinationAccount.getUserId())) {
                    totalBalanceDestination += account.getBalance();
                }
            }
            User destinationUser = nexpay.getUserCRUD().safeRead(destinationAccount.getUserId());
            if (destinationUser != null) {
                destinationUser.setTotalBalance(totalBalanceDestination);
                nexpay.getUserCRUD().update(destinationUser);
            }
        }
        transactionCRUD.delete(transaction.getId());
        System.out.println("Transaction reverted successfully.");
        sendAutomaticNotifications(sourceUser);
        undoTransaction=true;
        System.out.println(undoTransaction);
        return true;
    }

    private Account findAccount(String accountNumber) {
        for (Account account : nexpay.getAccounts()) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }
}





