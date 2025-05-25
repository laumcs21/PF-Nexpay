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
        verifyTransactionIntegrity(transaction);

        Account source = findAccount(transaction.getSourceAccountNumber());
        if (source == null) {
            throw new IllegalArgumentException("Cuenta no encontrada.");
        }

        User user = nexpay.getUserCRUD().safeRead(transaction.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        if (!source.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("La cuenta no pertenece al usuario.");
        }

        double commission = calculateCommission(transaction, user);
        double totalAmount = transaction.getAmount() + commission;

        switch (transaction.getType()) {
            case DEPOSITO:
                source.setBalance(source.getBalance() + transaction.getAmount());
                accountCRUD.update(source);
                break;

            case RETIRO:
                if (source.getBalance() < totalAmount) {
                    throw new IllegalArgumentException("Saldo insuficiente..");
                }
                source.setBalance(source.getBalance() - totalAmount);
                accountCRUD.update(source);
                break;

            case TRANSFERENCIA:
                if (source.getBalance() < totalAmount) {
                    throw new IllegalArgumentException("Saldo insuficiente.");
                }
                Account destination = findAccount(transaction.getDestinationAccountNumber());
                if (destination == null) {
                    throw new IllegalArgumentException("Cuenta destino no encontrada.");
                }
                if (destination.getAccountNumber().equals(source.getAccountNumber())) {
                    throw new IllegalArgumentException("No se puede transferir a la misma cuenta.");
                }

                source.setBalance(source.getBalance() - totalAmount);
                destination.setBalance(destination.getBalance() + transaction.getAmount());

                accountCRUD.update(source);
                accountCRUD.update(destination);
                break;

            default:
                throw new IllegalArgumentException("Tipo de transaccion invalido..");
        }

        if (transaction.getType() == TransactionType.TRANSFERENCIA) {
            Account destination = findAccount(transaction.getDestinationAccountNumber());

            if (source != null && destination != null &&
                    !source.getCategory().equals(destination.getCategory())) {
                nexpay.getCategoryGraphManager().registerTransition(source.getCategory(), destination.getCategory());
                nexpay.getCategoryGraphManager().saveGraph();
            }
        }

        if (transaction.getType() == TransactionType.TRANSFERENCIA) {
            Account destination = findAccount(transaction.getDestinationAccountNumber());

            if (source != null && destination != null) {
                String senderId = source.getUserId();
                String receiverId = destination.getUserId();

                if (!senderId.equals(receiverId)) {
                    nexpay.getUserTransferGraphManager().registerTransfer(senderId, receiverId);
                    nexpay.getUserTransferGraphManager().saveGraph();
                }
            }
        }


        // Update user total balance from all accounts
        double accountBalance = 0.0;
        for (Account acc : nexpay.getAccounts()) {
            if (acc.getUserId().equals(user.getId())) {
                accountBalance += acc.getBalance();
            }
        }
        user.setTotalBalance(accountBalance);

        // Earn and assign points
        int earnedPoints = calculatePoints(transaction);
        user.setPoints(user.getPoints() + earnedPoints);
        nexpay.getPointManager().addPoints(user.getId(), earnedPoints);
        nexpay.getRankManager().insertOrUpdate(user.getId(), user.getPoints());
        nexpay.getPointHistoryManager().register(user.getId(), "Transaction " + transaction.getType(), earnedPoints);
        nexpay.getPointHistoryManager().saveHistory();


        // Register transaction in wallet
        WalletNode node = user.getWalletGraph().findWalletNode(transaction.getWalletId());
        if (node != null) {
            node.getWallet().addTransaction(transaction);
            node.getWallet().updateBalance();
        }

        // Update total balance from all wallets
        double totalFromWallets = 0.0;
        for (WalletNode wn : user.getWalletGraph().getWalletNodes()) {
            totalFromWallets += wn.getWallet().getBalance();
        }
        user.setTotalBalance(totalFromWallets);

        // Save updated user (including benefit state)
        nexpay.getUserCRUD().update(user);

        // If it's a transfer, update destination user balance
        Account destinationAccount = findAccount(transaction.getDestinationAccountNumber());
        if (destinationAccount != null && !destinationAccount.getUserId().equals(user.getId())) {
            double destBalance = 0.0;
            for (Account acc : nexpay.getAccounts()) {
                if (acc.getUserId().equals(destinationAccount.getUserId())) {
                    destBalance += acc.getBalance();
                }
            }

            User destUser = nexpay.getUserCRUD().safeRead(destinationAccount.getUserId());
            if (destUser != null) {
                destUser.setTotalBalance(destBalance);
                nexpay.getUserCRUD().update(destUser);
            }
        }

        transactionCRUD.create(transaction);
        transactionStack.push(transaction);

        sendAutomaticNotifications(user);
        System.out.println("Transaccion ejecutada correctamente. Puntos: " + user.getPoints());
    }


    public boolean undoLastTransaction() {
        if (transactionStack.isEmpty()) {
            System.out.println("No hay transacciones recientes.");
            return false;
        }

        Transaction transaction = transactionStack.pop();
        Account source = findAccount(transaction.getSourceAccountNumber());
        User user = nexpay.getUserCRUD().safeRead(transaction.getUserId());

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
                    System.out.println("Cuenta destino no encontrada. No es posible revertir.");
                    return false;
                }
                break;

            default:
                System.out.println("Tipo de transaccion invalido.");
                return false;
        }

        // Revert user points
        int lostPoints = calculatePoints(transaction);
        user.setPoints(Math.max(0, user.getPoints() - lostPoints));
        nexpay.getPointManager().removePoints(user.getId(), lostPoints);
        nexpay.getRankManager().insertOrUpdate(user.getId(), user.getPoints());
        nexpay.getPointHistoryManager().register(user.getId(), "Reverted transaction", -lostPoints);
        nexpay.getPointHistoryManager().saveHistory();

        // Remove transaction from wallet
        WalletNode node = user.getWalletGraph().findWalletNode(transaction.getWalletId());
        if (node != null) {
            node.getWallet().getTransactions().remove(transaction);
            node.getWallet().updateBalance();
        }

        // Update user total balance from all accounts
        double totalBalance = 0.0;
        for (Account acc : nexpay.getAccounts()) {
            if (acc.getUserId().equals(user.getId())) {
                totalBalance += acc.getBalance();
            }
        }
        user.setTotalBalance(totalBalance);

        // Update total balance from all wallets
        double walletTotal = 0.0;
        for (WalletNode wn : user.getWalletGraph().getWalletNodes()) {
            walletTotal += wn.getWallet().getBalance();
        }
        user.setTotalBalance(walletTotal);

        // Save updated user (including points and benefit state)
        nexpay.getUserCRUD().update(user);

        // Update destination user balance if applicable
        Account destinationAccount = findAccount(transaction.getDestinationAccountNumber());
        if (destinationAccount != null && !destinationAccount.getUserId().equals(user.getId())) {
            double destTotal = 0.0;
            for (Account acc : nexpay.getAccounts()) {
                if (acc.getUserId().equals(destinationAccount.getUserId())) {
                    destTotal += acc.getBalance();
                }
            }
            User destUser = nexpay.getUserCRUD().safeRead(destinationAccount.getUserId());
            if (destUser != null) {
                destUser.setTotalBalance(destTotal);
                nexpay.getUserCRUD().update(destUser);
            }
        }

        transactionCRUD.delete(transaction.getId());
        revertedTransactions.push(transaction);

        sendAutomaticNotifications(user);

        System.out.println("Transaccion Revertida Exitosamente.");
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

    private int calculatePoints(Transaction tx) {
        int base = (int) (tx.getAmount() / 100);
        return switch (tx.getType()) {
            case DEPOSITO -> base;
            case RETIRO -> base * 2;
            case TRANSFERENCIA -> base * 3;
        };
    }

    private double calculateCommission(Transaction tx, User user) {
        double rate = switch (tx.getType()) {
            case TRANSFERENCIA -> 0.02;
            case RETIRO -> 0.015;
            default -> 0.0;
        };

        if (user.getBenefits().isDiscountActive()) {
            rate *= 0.9;
            user.getBenefits().deactivateDiscount(); // one-time benefit
        }

        if (tx.getType() == TransactionType.RETIRO &&
                user.getBenefits().hasFreeWithdrawalsActive()) {
            return 0.0;
        }

        return tx.getAmount() * rate;
    }

}






