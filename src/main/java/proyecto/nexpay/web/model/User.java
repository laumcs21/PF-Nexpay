package proyecto.nexpay.web.model;

import java.io.Serializable;

public class User extends Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private double totalBalance;
    private WalletGraph walletGraph;
    private Nexpay nexpay;
    private int points;
    private UserBenefit benefits = new UserBenefit();



    public User(String id, String password, String name, String email, String phone, String address, double totalBalance) {
        super(id, password, name, email, phone, address);
        this.totalBalance = totalBalance;
        this.walletGraph = new WalletGraph();
        this.nexpay = Nexpay.getInstance();
    }

    public WalletGraph getWalletGraph() {
        return walletGraph;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void updateTotalBalance() {
        double total = 0.0;

        for (WalletNode node : walletGraph.getWalletNodes()) {
            Wallet wallet = node.getWallet();
            wallet.updateBalance();
            total += wallet.getBalance();
        }

        this.totalBalance = total;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "User [totalBalance=" + totalBalance + "]";
    }

    public UserBenefit getBenefits() {
        return benefits;
    }

    public void setBenefits(UserBenefit benefits) {
        this.benefits = benefits;
    }

}





