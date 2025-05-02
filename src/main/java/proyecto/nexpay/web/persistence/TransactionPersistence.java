package proyecto.nexpay.web.persistence;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import proyecto.nexpay.web.model.Transaction;
import proyecto.nexpay.web.model.TransactionType;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;

public class TransactionPersistence {

    private static final String TEXT_FILE_PATH = "src/main/resources/persistence/files/Transactions.txt";
    private static final String XML_FILE_PATH = "src/main/resources/persistence/XML/Transactions.data";
    private static final String BINARY_FILE_PATH = "src/main/resources/persistence/binary/BinaryTransactions.data\\";
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
            transactionText.append(transaction.getUserId()).append("@@");
            transactionText.append(transaction.getId()).append("@@");
            transactionText.append(transaction.getDate()).append("@@");
            transactionText.append(transaction.getType()).append("@@");
            transactionText.append(transaction.getAmount()).append("@@");
            transactionText.append(transaction.getSourceAccountNumber()).append("@@");

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
                String dateText = split[2];
                String[] dateParts = dateText.split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);
                LocalDate date = LocalDate.of(year, month, day);

                TransactionType type = TransactionType.valueOf(split[3]);

                Transaction.Builder builder = new Transaction.Builder(
                        userId, split[1], date, type, Double.parseDouble(split[4]), split[5]);

                for (int i = 6; i < split.length; i++) {
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

    public DoubleLinkedList<Transaction> loadTransactionsFromXML() {
        try {
            return (DoubleLinkedList<Transaction>) FileUtil.loadSerializedXMLResource(XML_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error loading transactions from XML file: " + e.getMessage());
            return new DoubleLinkedList<>();
        } catch (ClassCastException e) {
            System.err.println("Type cast error loading transactions from XML: " + e.getMessage());
            return new DoubleLinkedList<>();
        }
    }

    public void saveTransactionToXML(Transaction transaction) {
        try {
            DoubleLinkedList<Transaction> transactions = loadTransactionsFromXML();
            transactions.addLast(transaction);
            FileUtil.saveSerializedXMLResource(XML_FILE_PATH, transactions);
        } catch (IOException e) {
            System.out.println("Error saving transaction to XML: " + e.getMessage());
        }
    }

    public void saveTransactionToBinary(Transaction transaction) {
        try {
            DoubleLinkedList<Transaction> transactions = loadTransactionsFromBinary();
            transactions.addLast(transaction);
            FileUtil.saveSerializedResource(BINARY_FILE_PATH, transactions);
            System.out.println("Record saved in binary: " + transaction);
        } catch (Exception e) {
            System.out.println("Error saving transactions in binary: " + e.getMessage());
        }
    }

    private DoubleLinkedList<Transaction> loadTransactionsFromBinary() {
        try {
            return (DoubleLinkedList<Transaction>) FileUtil.loadSerializedResource(BINARY_FILE_PATH);
        } catch (Exception e) {
            System.out.println("Error loading transactions from binary: " + e.getMessage());
            return new DoubleLinkedList<>();
        }
    }
}


