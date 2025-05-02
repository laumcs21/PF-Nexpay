package proyecto.nexpay.web.model;

import java.io.Serializable;
import java.util.Optional;
import proyecto.nexpay.web.persistence.AccountPersistence;
import proyecto.nexpay.web.datastructures.SimpleList;

public class AccountCRUD implements CRUD<Account>, Serializable {
    private Nexpay nexp;
    private AccountPersistence accountPersistence = new AccountPersistence();

    public AccountCRUD(Nexpay nexp) {
        this.nexp = nexp;
    }

    public Optional<Account> findAccountById(String id) {
        return findAccountRecursively(nexp.getAccounts(), id, 0);
    }

    private Optional<Account> findAccountRecursively(SimpleList<Account> accounts, String id, int index) {
        if (index >= accounts.getSize()) {
            return Optional.empty();
        }

        Account account = accounts.get(index);
        if (account.getId().equals(id)) {
            return Optional.of(account);
        }

        return findAccountRecursively(accounts, id, index + 1);
    }

    public static boolean searchString(String phrase, String search) {
        return phrase.contains(search);
    }

    @Override
    public void update(Account account) {
        delete(account.getId());
        nexp.getAccounts().addLast(account);
        accountPersistence.saveAllAccounts(nexp.getAccounts());
    }

    @Override
    public Account create(Account account) {
        if (findAccountById(account.getId()).isPresent()) {
            throw new IllegalArgumentException("The account is already created.");
        }
        nexp.getAccounts().addLast(account);
        accountPersistence.saveAllAccounts(nexp.getAccounts());
        return account;
    }

    @Override
    public void delete(String id) {
        Account account = read(id);
        nexp.getAccounts().remove(account);
        accountPersistence.saveAllAccounts(nexp.getAccounts());
    }

    @Override
    public Account read(String id) {
        return findAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("The account is not registered."));
    }
}



