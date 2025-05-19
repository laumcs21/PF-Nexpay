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
    private final Stack<Transaction> revertedTransactions;

    public TransactionManager(Nexpay nexpay) {
        this.nexpay = nexpay;
        this.transactionCRUD = nexpay.getTransactionCRUD();
        this.accountCRUD = nexpay.getAccountCRUD();
        this.transactionStack = new Stack<>();
        this.notificationManager = new NotificationManager(nexpay);
        this.undoTransaction = false;
        this.revertedTransactions = new Stack<>();
    }

    public boolean isUndoTransaction() {
        return undoTransaction;
    }

    public Stack<Transaction> getRevertedTransactions() {
        return revertedTransactions;
    }

    public void executeTransaction(Transaction transaction) {
        // Verificar la integridad de la transacción antes de proceder
        verifyTransactionIntegrity(transaction);

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
        User owner = nexpay.getUserCRUD().safeRead(transaction.getUserId());
        if (owner != null) {
            WalletNode node = owner.getWalletGraph().findWalletNode(transaction.getWalletId());
            if (node != null) {
                node.getWallet().addTransaction(transaction);
                node.getWallet().updateBalance(); // ← actualiza balance del wallet
            }

            // Recalcular totalBalance sumando los balances de todos los monederos del usuario
            double userTotal = 0.0;
            for (WalletNode wn : owner.getWalletGraph().getWalletNodes()) {
                userTotal += wn.getWallet().getBalance();
            }
            owner.setTotalBalance(userTotal);
            nexpay.getUserCRUD().update(owner);
        }



        System.out.println("Transaction executed successfully.");
        System.out.println("Total balance for user " + user.getId() + ": " + totalBalanceSource);

        sendAutomaticNotifications(user);
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
        revertedTransactions.push(transaction);
        User owner = nexpay.getUserCRUD().safeRead(transaction.getUserId());
        if (owner != null) {
            WalletNode node = owner.getWalletGraph().findWalletNode(transaction.getWalletId());
            if (node != null) {
                node.getWallet().getTransactions().remove(transaction);
            }
        }
        if (owner != null) {
            WalletNode node = owner.getWalletGraph().findWalletNode(transaction.getWalletId());
            if (node != null) {
                node.getWallet().getTransactions().remove(transaction);
                node.getWallet().updateBalance(); // ← actualiza balance del wallet
            }

            double userTotal = 0.0;
            for (WalletNode wn : owner.getWalletGraph().getWalletNodes()) {
                userTotal += wn.getWallet().getBalance();
            }
            owner.setTotalBalance(userTotal);
            nexpay.getUserCRUD().update(owner);
        }

        System.out.println("Transaction reverted successfully.");
        undoTransaction=true;
        sendAutomaticNotifications(sourceUser);
        System.out.println(undoTransaction);
        return true;
    }

    private void verifyTransactionIntegrity(Transaction transaction) {
        TransactionValidator.verifyTransaction(transaction, nexpay.getAccounts());
    }

    private void sendAutomaticNotifications(User user) {
        notificationManager.checkLowBalance();
        notificationManager.checkScheduledTransactions();
        notificationManager.checkUndoTransaction();
        notificationManager.processNotifications();
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






