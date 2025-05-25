package proyecto.nexpay.web.persistence;

import proyecto.nexpay.web.model.Account;
import proyecto.nexpay.web.model.AccountType;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.datastructures.SimpleList;

import java.io.File;
import java.io.IOException;

public class AccountPersistence {

    private static final String FILE_PATH = "src/main/resources/persistence/files/Accounts.txt";
    private static final String XML_FILE_PATH = "src/main/resources/persistence/XML/Accounts.xml";
    private static final String BINARY_FILE_PATH = "src/main/resources/persistence/binary/BinaryAccounts.data";
    private static AccountPersistence instance;

    public static AccountPersistence getInstance() {
        if (instance == null) {
            synchronized (AccountPersistence.class) {
                if (instance == null) {
                    instance = new AccountPersistence();
                }
            }
        }
        return instance;
    }

    public void saveAllAccounts(SimpleList<Account> accounts) {
        StringBuilder accountText = new StringBuilder();

        for (Account account : accounts) {
            accountText.append(account.getUserId()).append("@@");
            accountText.append(account.getWalletId()).append("@@");
            accountText.append(account.getId()).append("@@");
            accountText.append(account.getBankName()).append("@@");
            accountText.append(account.getAccountNumber()).append("@@");
            accountText.append(account.getAccountType()).append("@@");
            accountText.append(account.getBalance()).append("@@");
            accountText.append(account.getCategory()).append("\n");
        }

        try {
            FileUtil.saveFile(FILE_PATH, accountText.toString(), false);
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
        }
    }

    public SimpleList<Account> loadAccounts() throws IOException {
        SimpleList<Account> accounts = new SimpleList<>();
        File file = new File(FILE_PATH);

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error initializing account file");
        }

        for (String accountText : FileUtil.readFile(FILE_PATH)) {
            if (accountText.trim().isEmpty()) continue;
            try {
                String[] split = accountText.split("@@");
                String userId = split[0];
                String walletId = split[1];
                String accountId = split[2];
                String bankName = split[3];
                String accountNumber = split[4];
                AccountType accountType = AccountType.valueOf(split[5]);
                double balance = Double.parseDouble(split[6]);
                String category = split[7];

                Account account = new Account(userId, walletId, accountId, bankName, accountNumber,
                        accountType, balance, category);
                accounts.addLast(account);
            } catch (Exception e) {
                System.err.println("Error loading account from line: " + accountText + " — " + e.getMessage());
            }
        }

        return accounts;
    }


    public SimpleList<Account> loadAccountsFromXML() {
        try {
            return (SimpleList<Account>) FileUtil.loadSerializedXMLResource(XML_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error loading accounts from XML file: " + e.getMessage());
            return new SimpleList<>();
        } catch (ClassCastException e) {
            System.err.println("Type conversion error loading accounts from XML file: " + e.getMessage());
            return new SimpleList<>();
        }
    }

    public void saveAccountToXML(Account account) {
        try {
            SimpleList<Account> accounts = loadAccountsFromXML();
            accounts.addLast(account);
            FileUtil.saveSerializedXMLResource(XML_FILE_PATH, accounts);
        } catch (IOException e) {
            System.out.println("Error saving account to XML: " + e.getMessage());
        }
    }

    public void saveAccountToBinary(Account account) {
        try {
            SimpleList<Account> accounts = loadAccountsFromBinary();
            accounts.addLast(account);
            FileUtil.saveSerializedResource(BINARY_FILE_PATH, accounts);
            System.out.println("Account saved to binary: " + account);
        } catch (Exception e) {
            System.out.println("Error saving accounts to binary: " + e.getMessage());
        }
    }

    private SimpleList<Account> loadAccountsFromBinary() {
        try {
            return (SimpleList<Account>) FileUtil.loadSerializedResource(BINARY_FILE_PATH);
        } catch (Exception e) {
            System.out.println("Error loading accounts from binary: " + e.getMessage());
            return new SimpleList<>();
        }
    }
}

