package proyecto.nexpay.web.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private String userId;
    private String id;
    private LocalDate date;
    private TransactionType type;
    private double amount;
    private String description;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private LocalDateTime scheduledDate;

    public Transaction(Builder builder) {
        this.userId = builder.userId;
        this.id = builder.id;
        this.date = builder.date;
        this.type = builder.type;
        this.amount = builder.amount;
        this.description = builder.description;
        this.sourceAccountNumber = builder.sourceAccountNumber;
        this.destinationAccountNumber = builder.destinationAccountNumber;
        this.scheduledDate = builder.scheduledDate;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public static class Builder {

        private String userId;
        private String id;
        private LocalDate date;
        private TransactionType type;
        private double amount;
        private String description;
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private LocalDateTime scheduledDate;

        public Builder(String userId, String id, LocalDate date, TransactionType type, double amount,
                       String sourceAccountNumber) {
            this.type = type;
            this.userId = userId;
            this.id = id;
            this.date = date;
            this.amount = amount;
            this.sourceAccountNumber = sourceAccountNumber;
        }

        public Builder withDestinationAccountNumber(String destinationAccountNumber) {
            this.destinationAccountNumber = destinationAccountNumber;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withScheduledDate(LocalDateTime scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "userId='" + userId + '\'' +
                ", transactionCode='" + id + '\'' +
                ", date=" + date +
                ", transactionType=" + type +
                ", amount=" + amount +
                ", sourceAccountNumber='" + sourceAccountNumber + '\'' +
                (destinationAccountNumber != null ? ", destinationAccountNumber='" + destinationAccountNumber + '\'' : "") +
                (description != null ? ", description='" + description + '\'' : "") +
                '}';
    }
}

