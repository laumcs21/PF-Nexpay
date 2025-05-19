package proyecto.nexpay.web.persistence;

import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.model.User;
import proyecto.nexpay.web.model.Wallet;
import proyecto.nexpay.web.model.WalletNode;

import java.io.File;
import java.io.IOException;

public class WalletPersistence {

    private static final String FILE_PATH = "src/main/resources/persistence/files/Wallets.txt";
    private static WalletPersistence instance;

    public static WalletPersistence getInstance() {
        if (instance == null) {
            synchronized (WalletPersistence.class) {
                if (instance == null) {
                    instance = new WalletPersistence();
                }
            }
        }
        return instance;
    }

    public void saveAllWallets(SimpleList<Wallet> wallets) {
        StringBuilder sb = new StringBuilder();

        for (Wallet wallet : wallets) {
            sb.append(wallet.getUserId()).append("@@")
                    .append(wallet.getWalletId()).append("@@")
                    .append(wallet.getName()).append("@@")
                    .append(wallet.getBalance()).append("\n");
        }

        try {
            FileUtil.saveFile(FILE_PATH, sb.toString(), false);
        } catch (IOException e) {
            System.err.println("Error saving wallets: " + e.getMessage());
        }
    }

    public void saveWallet(Wallet wallet) {
        String line = wallet.getUserId() + "@@" +
                wallet.getWalletId() + "@@" +
                wallet.getName() + "@@" +
                wallet.getBalance() + "\n";

        try {
            FileUtil.saveFile(FILE_PATH, line, true); // true = añadir
        } catch (IOException e) {
            System.err.println("Error saving wallet: " + e.getMessage());
        }
    }

    public void loadWalletsForUser(User user) throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        for (String line : FileUtil.readFile(FILE_PATH)) {
            String[] split = line.split("@@");
            if (split.length != 4) continue; // ahora deben ser 4 campos

            String userId = split[0];
            if (!user.getId().equals(userId)) continue;

            String walletId = split[1];
            String name = split[2];
            double balance = Double.parseDouble(split[3]);

            Wallet wallet = new Wallet(walletId, userId, name);
            wallet.setBalance(balance);

            WalletNode node = new WalletNode(wallet);
            user.getWalletGraph().getWalletNodes().addLast(node);
        }
    }

    public SimpleList<Wallet> loadAllWallets() throws IOException {
        SimpleList<Wallet> allWallets = new SimpleList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        for (String line : FileUtil.readFile(FILE_PATH)) {
            String[] split = line.split("@@");
            if (split.length != 4) continue;

            String userId = split[0];
            String walletId = split[1];
            String name = split[2];
            double balance = Double.parseDouble(split[3]);

            Wallet wallet = new Wallet(walletId, userId, name);
            wallet.setBalance(balance);

            allWallets.addLast(wallet);
        }

        return allWallets;
    }
}

