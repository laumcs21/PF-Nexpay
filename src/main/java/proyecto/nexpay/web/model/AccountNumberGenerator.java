package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;
import java.io.Serializable;
import java.util.Random;

public class AccountNumberGenerator implements Serializable {

    public static String generateUniqueNumber(int length, SimpleList<Account> existingAccounts) {
        String characters = "0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder codeBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length());
                codeBuilder.append(characters.charAt(index));
            }
            code = codeBuilder.toString();
        } while (codeExists(code, existingAccounts));

        return code;
    }

    private static boolean codeExists(String code, SimpleList<Account> existingAccounts) {
        for (Account account : existingAccounts) {
            if (account.getAccountNumber().equals(code)) {
                return true;
            }
        }
        return false;
    }
}


