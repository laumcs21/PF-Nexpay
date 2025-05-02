package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;

public class AccountSearch {

    private static Nexpay nexpay;

    static {
        nexpay = Nexpay.getInstance();
    }

    public static Account findAccountByNumber(String number) {
        return findAccountByNumberRecursive(nexpay.getAccounts(), number, 0);
    }

    private static Account findAccountByNumberRecursive(SimpleList<Account> accounts, String number, int index) {
        if (index >= accounts.getSize()) {
            return null;
        }

        Account account = accounts.get(index);
        if (account.getAccountNumber().equals(number)) {
            return account;
        }

        return findAccountByNumberRecursive(accounts, number, index + 1);
    }
}
