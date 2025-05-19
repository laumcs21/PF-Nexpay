package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;
import java.io.Serializable;
import java.util.Random;

public class WalletCodeGenerator implements Serializable {

    public static String generateUniqueCode(int length, SimpleList<Wallet> existingWallets) {
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
        } while (codeExists(code, existingWallets));

        return code;
    }

    private static boolean codeExists(String code, SimpleList<Wallet> existingWallets) {
        for (Wallet wallet : existingWallets) {
            if (wallet.getWalletId().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
