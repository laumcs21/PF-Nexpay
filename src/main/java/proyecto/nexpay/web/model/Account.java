package proyecto.nexpay.web.model;

import java.io.Serializable;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String id;
    private String bankName;
    private String accountNumber;
    private AccountType accountType;
    private double balance;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Account(String userId, String id, String bankName, String accountNumber, AccountType accountType,
                   double balance) {
        this.userId = userId;
        this.id = id;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account [userId=" + userId + ", id=" + id + ", bankName=" + bankName + ", accountNumber="
                + accountNumber + ", accountType=" + accountType + ", balance=" + balance + "]";
    }
}
