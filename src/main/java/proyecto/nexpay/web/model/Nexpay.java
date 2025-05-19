package proyecto.nexpay.web.model;

import java.io.IOException;
import java.io.Serializable;

import proyecto.nexpay.web.persistence.UserPersistence;
import proyecto.nexpay.web.persistence.AccountPersistence;
import proyecto.nexpay.web.persistence.TransactionPersistence;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;
import proyecto.nexpay.web.persistence.WalletPersistence;
import proyecto.nexpay.web.service.ScheduledTransactionManager;
import proyecto.nexpay.web.service.ScheduledTransactionExecutor;
import proyecto.nexpay.web.service.TransactionManager;
import proyecto.nexpay.web.service.NotificationManager;

public class Nexpay implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Nexpay instance;

    private SimpleList<User> users;
    private SimpleList<Account> accounts;
    private DoubleLinkedList<Transaction> transactions;
    private SimpleList<Wallet> wallets;

    private UserCRUD userCRUD;
    private AccountCRUD accountCRUD;
    private TransactionCRUD transactionCRUD;
    private WalletCRUD walletCRUD;

    private TransactionManager TManager;
    private ScheduledTransactionManager SManager;
    private ScheduledTransactionExecutor executor;

    private Thread backupThread;

    private Nexpay(NotificationManager notificationManager) {
        this.users = new SimpleList<>();
        this.accounts = new SimpleList<>();
        this.wallets = new SimpleList<>();
        this.transactions = new DoubleLinkedList<>();

        this.userCRUD = new UserCRUD(this);
        this.accountCRUD = new AccountCRUD(this);
        this.transactionCRUD = new TransactionCRUD(this);
        this.walletCRUD = new WalletCRUD(this);
        this.TManager = new TransactionManager(this);

        this.SManager = new ScheduledTransactionManager(TManager);
        this.executor = new ScheduledTransactionExecutor(SManager);
    }

    public static Nexpay getInstance() {
        if (instance == null) {
            synchronized (Nexpay.class) {
                if (instance == null) {
                    NotificationManager notificationManager = new NotificationManager(instance);
                    instance = new Nexpay(notificationManager);
                    instance.loadUserData();
                    instance.loadAccountData();
                    instance.loadWalletsData();
                    instance.loadTransactionData();
                    instance.executor.start();
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

    public WalletCRUD getWalletCRUD() {
        return walletCRUD;
    }

    public SimpleList<Wallet> getWallets() {
        return wallets;
    }

    public TransactionManager getTManager() {
        return TManager;
    }

    public ScheduledTransactionManager getSManager() {
        return SManager;
    }

    private void loadUserData() {
        UserPersistence persistence = UserPersistence.getInstance();
        WalletPersistence walletPersistence = WalletPersistence.getInstance();
        try {
            for (User u : persistence.loadUsers()) {
                walletPersistence.loadWalletsForUser(u);
                this.users.addLast(u);
            }
        } catch (IOException e) {
            System.err.println("Error loading users or wallets: " + e.getMessage());
        }
    }

    private void loadAccountData() {
        AccountPersistence persistence = AccountPersistence.getInstance();
        try {
            for (Account a : persistence.loadAccounts()) {
                this.accounts.addLast(a);

                // Asociar cuenta al Wallet correspondiente
                User owner = userCRUD.safeRead(a.getUserId());
                if (owner != null) {
                    WalletNode node = owner.getWalletGraph().findWalletNode(a.getWalletId());
                    if (node != null) {
                        node.getWallet().getAccounts().addLast(a);
                        node.getWallet().updateBalance();
                    }
                }
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

                User owner = userCRUD.safeRead(t.getUserId());
                if (owner != null) {
                    WalletNode node = owner.getWalletGraph().findWalletNode(t.getWalletId());
                    if (node != null) {
                        node.getWallet().getTransactions().addLast(t);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions from file: " + e.getMessage());
        }
    }

    private void loadWalletsData() {
        WalletPersistence persistence = WalletPersistence.getInstance();
        try {
            this.wallets = persistence.loadAllWallets();
        } catch (IOException e) {
            System.err.println("Error loading all wallets: " + e.getMessage());
            this.wallets = new SimpleList<>(); // En caso de error, evita null
        }
    }


}




