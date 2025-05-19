package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;

public class WalletNode {
    private Wallet wallet;
    private SimpleList<Edge> edges;

    public WalletNode(Wallet wallet) {
        this.wallet = wallet;
        this.edges = new SimpleList<>();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void addEdge(WalletNode destination, double amount) {
        edges.addLast(new Edge(destination, amount));
    }

    public void removeEdgeTo(WalletNode target) {
        SimpleList<Edge> updated = new SimpleList<>();
        for (Edge e : edges) {
            if (!e.getDestination().equals(target)) {
                updated.addLast(e);
            }
        }
        edges = updated;
    }

    public SimpleList<Edge> getEdges() {
        return edges;
    }
}



