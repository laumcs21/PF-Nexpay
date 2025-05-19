package proyecto.nexpay.web.persistence;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;

import proyecto.nexpay.web.model.Transaction;
import proyecto.nexpay.web.model.TransactionType;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;
import proyecto.nexpay.web.service.ScheduledTransaction;

public class TransactionPersistence {

    private static final String TEXT_FILE_PATH = "src/main/resources/persistence/files/Transactions.txt";
    private static final String XML_FILE_PATH = "src/main/resources/persistence/XML/Transactions.data";
    private static final String BINARY_FILE_PATH = "src/main/resources/persistence/binary/BinaryTransactions.data\\";
    private static final String SCHEDULED_TEXT_FILE_PATH = "src/main/resources/persistence/files/ScheduledTransactions.txt";
    private static TransactionPersistence instance;

    public static TransactionPersistence getInstance() {
        if (instance == null) {
            synchronized (TransactionPersistence.class) {
                if (instance == null) {
                    instance = new TransactionPersistence();
                }
            }
        }
        return instance;
    }

    public void saveAllTransactions(DoubleLinkedList<Transaction> transactions) {
        StringBuilder transactionText = new StringBuilder();
        for (Transaction transaction : transactions) {
            transactionText.append(transaction.getUserId()).append("@@")
                    .append(transaction.getWalletId()).append("@@")
                    .append(transaction.getId()).append("@@")
                    .append(transaction.getDate()).append("@@")
                    .append(transaction.getType()).append("@@")
                    .append(transaction.getAmount()).append("@@")
                    .append(transaction.getSourceAccountNumber()).append("@@");

            if (transaction.getDestinationAccountNumber() != null) {
                transactionText.append("destination=").append(transaction.getDestinationAccountNumber()).append("@@");
            }
            if (transaction.getDescription() != null) {
                transactionText.append("description=").append(transaction.getDescription()).append("@@");
            }
            transactionText.append("\n");
        }

        try {
            FileUtil.saveFile(TEXT_FILE_PATH, transactionText.toString(), false);
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    public DoubleLinkedList<Transaction> loadTransactions() throws IOException {
        DoubleLinkedList<Transaction> transactions = new DoubleLinkedList<>();
        File file = new File(TEXT_FILE_PATH);

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating transactions file");
        }

        for (String transactionText : FileUtil.readFile(TEXT_FILE_PATH)) {
            String[] split = transactionText.split("@@");
            if (split.length < 6) {
                System.err.println("Invalid or incomplete line: " + transactionText);
                continue;
            }

            try {
                String userId = split[0];
                String dateText = split[3];
                String[] dateParts = dateText.split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);
                LocalDate date = LocalDate.of(year, month, day);

                TransactionType type = TransactionType.valueOf(split[4]);

                Transaction.Builder builder = new Transaction.Builder(
                        userId, split [1], split[2], date, type, Double.parseDouble(split[5]), split[6]);
                for (int i = 7; i < split.length; i++) {
                    if (split[i].startsWith("destination=")) {
                        builder.withDestinationAccountNumber(split[i].substring("destination=".length()));
                    } else if (split[i].startsWith("description=")) {
                        builder.withDescription(split[i].substring("description=".length()));
                    }
                }

                transactions.addLast(builder.build());

            } catch (Exception e) {
                System.err.println("Error loading transaction from line: " + transactionText);
            }
        }

        return transactions;
    }

    public void saveScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        StringBuilder scheduledTransactionText = new StringBuilder();

        Transaction transaction = scheduledTransaction.getTransaction();
        scheduledTransactionText.append(transaction.getUserId()).append("@@")
                .append(transaction.getId()).append("@@")
                .append(transaction.getScheduledDate()).append("@@")  // Guardamos la fecha programada
                .append(transaction.getType()).append("@@")
                .append(transaction.getAmount()).append("@@")
                .append(transaction.getSourceAccountNumber()).append("@@");

        if (transaction.getDestinationAccountNumber() != null) {
            scheduledTransactionText.append("destination=").append(transaction.getDestinationAccountNumber()).append("@@");
        }
        if (transaction.getDescription() != null) {
            scheduledTransactionText.append("description=").append(transaction.getDescription()).append("@@");
        }
        scheduledTransactionText.append("\n");

        try {
            FileUtil.saveFile(SCHEDULED_TEXT_FILE_PATH, scheduledTransactionText.toString(), true);  // 'true' para añadir al final
        } catch (IOException e) {
            System.err.println("Error saving scheduled transaction: " + e.getMessage());
        }
    }

    public void saveScheduledTransactions(DoubleLinkedList<ScheduledTransaction> scheduledTransactions) {
        StringBuilder scheduledTransactionText = new StringBuilder();

        for (ScheduledTransaction scheduledTransaction : scheduledTransactions) {
            Transaction current = scheduledTransaction.getTransaction();

            scheduledTransactionText.append(current.getUserId()).append("@@")
                    .append(current.getId()).append("@@")
                    .append(current.getScheduledDate()).append("@@")
                    .append(current.getType()).append("@@")
                    .append(current.getAmount()).append("@@")
                    .append(current.getSourceAccountNumber()).append("@@");

            if (current.getDestinationAccountNumber() != null) {
                scheduledTransactionText.append("destination=").append(current.getDestinationAccountNumber()).append("@@");
            }

            if (current.getDescription() != null) {
                scheduledTransactionText.append("description=").append(current.getDescription()).append("@@");
            }

            scheduledTransactionText.append("\n");
        }

        try {
            FileUtil.saveFile(SCHEDULED_TEXT_FILE_PATH, scheduledTransactionText.toString(), false);  // 'false' para sobrescribir
            System.out.println("Scheduled transactions saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving scheduled transactions: " + e.getMessage());
        }
    }


    public DoubleLinkedList<ScheduledTransaction> loadScheduledTransactions() throws IOException {
        DoubleLinkedList<ScheduledTransaction> scheduledTransactions = new DoubleLinkedList<>();
        File file = new File(SCHEDULED_TEXT_FILE_PATH);

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating scheduled transactions file");
        }

        for (String scheduledTransactionText : FileUtil.readFile(SCHEDULED_TEXT_FILE_PATH)) {
            scheduledTransactionText = scheduledTransactionText.trim();

            if (scheduledTransactionText.isEmpty()) {
                continue;
            }

            String[] split = scheduledTransactionText.split("@@");

            if (split.length < 7) {
                System.err.println("Invalid or incomplete line: " + scheduledTransactionText);
                continue;
            }

            try {
                String userId = split[0];
                String scheduledDateText = split[3] + "T10:00:00";
                LocalDateTime scheduledDate = LocalDateTime.parse(scheduledDateText);

                TransactionType type = TransactionType.valueOf(split[4]);

                Transaction.Builder builder = new Transaction.Builder(
                        userId, split[1],split[2], scheduledDate.toLocalDate(), type, Double.parseDouble(split[5]), split[6]);

                if (split.length > 7) {
                    for (int i = 7; i < split.length; i++) {
                        if (split[i].startsWith("destination=")) {
                            builder.withDestinationAccountNumber(split[i].substring("destination=".length()));
                        } else if (split[i].startsWith("description=")) {
                            // Si description está vacío, no lo agregues
                            String description = split[i].substring("description=".length());
                            if (!description.isEmpty()) {
                                builder.withDescription(description);
                            }
                        }
                    }
                }

                Transaction transaction = builder.build();
                ScheduledTransaction scheduledTransaction = new ScheduledTransaction(transaction, scheduledDate);
                scheduledTransactions.addLast(scheduledTransaction);

            } catch (Exception e) {
                //System.err.println("Error processing line: " + scheduledTransactionText);
            }
        }

        return scheduledTransactions;
    }

    public synchronized void removeScheduledTransaction(String transactionId) {
        DoubleLinkedList<ScheduledTransaction> scheduledTransactions = null;
        try {
            scheduledTransactions = loadScheduledTransactions();
        } catch (IOException e) {
            System.err.println("Error loading scheduled transactions: " + e.getMessage());
            return;
        }

        boolean removed = false;
        Iterator<ScheduledTransaction> iterator = scheduledTransactions.iterator();
        while (iterator.hasNext()) {
            ScheduledTransaction scheduledTransaction = iterator.next();

            if (scheduledTransaction.getTransaction().getId().equals(transactionId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        if (!removed) {
            System.out.println("Transaction with ID " + transactionId + " not found.");
            return;
        }
        saveScheduledTransactions(scheduledTransactions);
    }

}



