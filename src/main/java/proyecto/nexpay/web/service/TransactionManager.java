package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.Stack;
import proyecto.nexpay.web.model.*;
public class TransactionManager {

    private final Nexpay nexpay;
    private final TransactionCRUD transactionCRUD;
    private final AccountCRUD accountCRUD;
    private final Stack<Transaction> transactionStack;

    public TransactionManager(Nexpay nexpay) {
        this.nexpay = nexpay;
        this.transactionCRUD = nexpay.getTransactionCRUD();
        this.accountCRUD = nexpay.getAccountCRUD();
        this.transactionStack = new Stack<>();
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
                break;
            case RETIRO:
                if (source.getBalance() < transaction.getAmount()) {
                    throw new IllegalArgumentException("Fondos insuficientes para realizar el retiro.");
                }
                source.setBalance(source.getBalance() - transaction.getAmount());
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
                    throw new IllegalArgumentException("No se puede tranferir a la misma cuenta de origen.");
                }
                source.setBalance(source.getBalance() - transaction.getAmount());
                destination.setBalance(destination.getBalance() + transaction.getAmount());
                accountCRUD.update(destination);
                break;
            default:
                throw new IllegalArgumentException("Tipo de transacción no válido.");
        }

        accountCRUD.update(source);
        transactionCRUD.create(transaction);
        transactionStack.push(transaction);

        System.out.println("Transaction executed successfully.");
    }

    public void undoLastTransaction() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transactions to undo.");
            return;
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
                }
                break;
            default:
                System.out.println("Invalid transaction type for undo.");
                return;
        }

        transactionCRUD.delete(transaction.getId());
        System.out.println("Transaction reverted successfully.");
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

