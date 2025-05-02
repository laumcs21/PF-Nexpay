package proyecto.nexpay.web.model;

import java.io.IOException;
import java.io.Serializable;

import proyecto.nexpay.web.persistence.UserPersistence;
import proyecto.nexpay.web.persistence.AccountPersistence;
import proyecto.nexpay.web.persistence.TransactionPersistence;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;

public class Nexpay implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Nexpay instance;

    private SimpleList<User> users;
    private SimpleList<Account> accounts;
    private DoubleLinkedList<Transaction> transactions;

    private UserCRUD userCRUD;
    private AccountCRUD accountCRUD;
    private TransactionCRUD transactionCRUD;

    private Thread backupThread;

    private Nexpay() {
        this.users = new SimpleList<>();
        this.accounts = new SimpleList<>();
        this.transactions = new DoubleLinkedList<>();

        this.userCRUD = new UserCRUD(this);
        this.accountCRUD = new AccountCRUD(this);
        this.transactionCRUD = new TransactionCRUD(this);
    }

    public static Nexpay getInstance() {
        if (instance == null) {
            synchronized (Nexpay.class) {
                if (instance == null) {
                    instance = new Nexpay();
                    instance.loadUserData();
                    instance.loadAccountData();
                    instance.loadTransactionData();
                }
            }
        }
        return instance;
    }

    public SimpleList<User> getUsers() {
        return users;
    }

    public void setUsers(SimpleList<User> users) {
        this.users = users;
    }

    public SimpleList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(SimpleList<Account> accounts) {
        this.accounts = accounts;
    }

    public DoubleLinkedList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(DoubleLinkedList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public UserCRUD getUserCRUD() {
        return userCRUD;
    }

    public void setUserCRUD(UserCRUD userCRUD) {
        this.userCRUD = userCRUD;
    }

    public AccountCRUD getAccountCRUD() {
        return accountCRUD;
    }

    public void setAccountCRUD(AccountCRUD accountCRUD) {
        this.accountCRUD = accountCRUD;
    }

    public TransactionCRUD getTransactionCRUD() {
        return transactionCRUD;
    }

    public void setTransactionCRUD(TransactionCRUD transactionCRUD) {
        this.transactionCRUD = transactionCRUD;
    }

    private void loadUserData() {
        UserPersistence persistence = UserPersistence.getInstance();
        try {
            for (User u : persistence.loadUsers()) {
                this.users.addLast(u);
            }
        } catch (IOException e) {
            System.err.println("Error loading users from file: " + e.getMessage());
        }
    }

    private void loadAccountData() {
        AccountPersistence persistence = AccountPersistence.getInstance();
        try {
            for (Account a : persistence.loadAccounts()) {
                this.accounts.addLast(a);
            }
        } catch (IOException e) {
            System.err.println("Error loading accounts from file: " + e.getMessage());
        }
    }

    private void loadTransactionData() {
        TransactionPersistence persistence = TransactionPersistence.getInstance();
        try {
            for (Transaction t : persistence.loadTransactions()) {
                this.transactions.addLast(t);
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions from file: " + e.getMessage());
        }
    }
}


