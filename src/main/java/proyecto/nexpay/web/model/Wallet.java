package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.DoubleLinkedList;
import proyecto.nexpay.web.datastructures.PriorityQueue;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.service.ScheduledTransaction;

public class Wallet {
    private String walletId;
    private String userId;
    private String name;
    private double balance;

    private SimpleList<Account> accounts;
    private DoubleLinkedList<Transaction> transactions;
    private PriorityQueue<ScheduledTransaction> scheduledTransactions;

    public Wallet(String walletId, String userId, String name) {
        this.walletId = walletId;
        this.userId = userId;
        this.name = name;
        this.balance = 0;
        this.accounts = new SimpleList<>();
        this.transactions = new DoubleLinkedList<>();
        this.scheduledTransactions = new PriorityQueue<>();
    }

    public String getWalletId() {
        return walletId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void updateBalance() {
        double total = 0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        this.balance = total;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public SimpleList<Account> getAccounts() {
        return accounts;
    }

    public DoubleLinkedList<Transaction> getTransactions() {
        return transactions;
    }

    public PriorityQueue<ScheduledTransaction> getScheduledTransactions() {
        return scheduledTransactions;
    }

    public void addAccount(Account account) {
        accounts.addLast(account);
        updateBalance();
    }

    public void addTransaction(Transaction transaction) {
        transactions.addLast(transaction);
    }

    public void scheduleTransaction(ScheduledTransaction scheduledTransaction) {
        scheduledTransactions.enqueue(scheduledTransaction);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "walletId='" + walletId + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}



