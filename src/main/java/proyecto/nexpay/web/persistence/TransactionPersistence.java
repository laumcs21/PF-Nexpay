package proyecto.nexpay.web.persistence;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // Cargar todas las transacciones (regulares)
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

    public void saveScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        StringBuilder scheduledTransactionText = new StringBuilder();

        // Obtener la transacción programada y construir el texto para guardarla
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

        // Guardar solo la transacción programada en el archivo (añadir al final del archivo)
        try {
            FileUtil.saveFile(SCHEDULED_TEXT_FILE_PATH, scheduledTransactionText.toString(), true);  // 'true' para añadir al final
        } catch (IOException e) {
            System.err.println("Error saving scheduled transaction: " + e.getMessage());
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

        // Leer todas las líneas del archivo
        for (String scheduledTransactionText : FileUtil.readFile(SCHEDULED_TEXT_FILE_PATH)) {
            scheduledTransactionText = scheduledTransactionText.trim();

            // Saltar líneas vacías
            if (scheduledTransactionText.isEmpty()) {
                continue;
            }

            String[] split = scheduledTransactionText.split("@@");

            // Verificar que la línea tenga al menos 6 campos
            if (split.length < 6) {  // Al menos los campos básicos deben estar presentes
                System.err.println("Invalid or incomplete line: " + scheduledTransactionText);
                continue; // Ignorar esta línea y pasar a la siguiente
            }

            try {
                String userId = split[0];
                String scheduledDateText = split[2] + "T10:00:00";  // Agregar hora por defecto si no está presente
                LocalDateTime scheduledDate = LocalDateTime.parse(scheduledDateText);

                // Validar si el tipo de transacción es válido
                TransactionType type = TransactionType.valueOf(split[3]);

                // Crear el builder de la transacción
                Transaction.Builder builder = new Transaction.Builder(
                        userId, split[1], scheduledDate.toLocalDate(), type, Double.parseDouble(split[4]), split[5]);

                // Si hay campos opcionales, añadirlos si están presentes
                if (split.length > 6) {
                    for (int i = 6; i < split.length; i++) {
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

                // Crear la transacción y añadirla a la cola
                Transaction transaction = builder.build();
                ScheduledTransaction scheduledTransaction = new ScheduledTransaction(transaction, scheduledDate);
                scheduledTransactions.addLast(scheduledTransaction);

            } catch (Exception e) {
                System.err.println("Error processing line: " + scheduledTransactionText);
            }
        }

        return scheduledTransactions;
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



