package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;

public class WalletGraph {
    private SimpleList<WalletNode> walletNodes;

    public WalletGraph() {
        this.walletNodes = new SimpleList<>();
    }

    public void addWallet(Wallet wallet) {
        walletNodes.addLast(new WalletNode(wallet));
    }

    public WalletNode findWalletNode(String walletId) {
        for (WalletNode node : walletNodes) {
            if (node.getWallet().getWalletId().equals(walletId)) {
                return node;
            }
        }
        return null;
    }

    public void transfer(String fromId, String toId, double amount) {
        WalletNode from = findWalletNode(fromId);
        WalletNode to = findWalletNode(toId);

        if (from == null || to == null) throw new IllegalArgumentException("Wallet not found");
        if (from.getWallet().getBalance() < amount) throw new IllegalArgumentException("Insufficient funds");

        from.getWallet().setBalance(from.getWallet().getBalance() - amount);
        to.getWallet().setBalance(to.getWallet().getBalance() + amount);
        from.addEdge(to, amount);
    }

    public void removeWallet(String walletId) {
        WalletNode node = findWalletNode(walletId);
        if (node == null) return;
        walletNodes.remove(node);
        for (WalletNode w : walletNodes) {
            w.removeEdgeTo(node);
        }
    }

    public String getEdges(String walletId) {
        WalletNode node = findWalletNode(walletId);
        if (node == null) return "Wallet not found.";

        StringBuilder sb = new StringBuilder("Edges from ").append(walletId).append(":\n");
        for (Edge e : node.getEdges()) {
            sb.append(" → To: ").append(e.getDestination().getWallet().getWalletId())
                    .append(" | Amount: ").append(e.getAmount()).append("\n");
        }
        return sb.toString();
    }

    public SimpleList<WalletNode> getWalletNodes() {
        return walletNodes;
    }
}


