package proyecto.nexpay.web.model;

import java.io.Serializable;
import java.util.Random;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;

public class TransactionCodeGenerator implements Serializable {

    public static String generateUniqueCode(int length, DoubleLinkedList<Transaction> existingTransactions) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder codeBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length());
                codeBuilder.append(characters.charAt(index));
            }
            code = codeBuilder.toString();
        } while (codeExists(code, existingTransactions));

        return code;
    }

    private static boolean codeExists(String code,DoubleLinkedList<Transaction> existingTransactions) {
        for (Transaction transaction : existingTransactions) {
            if (transaction.getId().equals(code)) {
                return true;
            }
        }
        return false;
    }
}



