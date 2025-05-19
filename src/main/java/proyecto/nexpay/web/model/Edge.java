package proyecto.nexpay.web.model;

public class Edge {
    private WalletNode destination;
    private double amount;

    public Edge(WalletNode destination, double amount) {
        this.destination = destination;
        this.amount = amount;
    }

    public WalletNode getDestination() {
        return destination;
    }

    public double getAmount() {
        return amount;
    }
}



