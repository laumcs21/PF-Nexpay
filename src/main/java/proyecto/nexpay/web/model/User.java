package proyecto.nexpay.web.model;
import java.io.Serializable;

public class User extends Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private double totalBalance;
    private Nexpay nexpay;

    public User(String id, String password, String name, String email, String phone, String address, double totalBalance) {
        super(id, password, name, email, phone, address);
        this.totalBalance = totalBalance;
        this.nexpay = Nexpay.getInstance();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public Nexpay getNexpay() {
        return nexpay;
    }

    public void setNexpay(Nexpay nexpay) {
        this.nexpay = nexpay;
    }

    public void updateTotalBalance() {
        double newTotalBalance = nexpay.getAccounts().stream()
                .filter(account -> account.getUserId().equals(this.getId()))
                .mapToDouble(Account::getBalance)
                .sum();

        this.totalBalance = newTotalBalance;
    }

    @Override
    public String toString() {
        return "User [totalBalance=" + totalBalance + ", nexpay=" + nexpay + "]";
    }
}





