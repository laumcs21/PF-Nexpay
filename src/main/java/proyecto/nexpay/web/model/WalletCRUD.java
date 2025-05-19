package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.persistence.WalletPersistence;

import java.io.Serializable;
import java.util.Optional;

public class WalletCRUD implements CRUD<Wallet>, Serializable {

    private Nexpay nexpay;
    private WalletPersistence persistence = WalletPersistence.getInstance();

    public WalletCRUD(Nexpay nexpay) {
        this.nexpay = nexpay;
    }

    public Optional<Wallet> findWalletById(String userId, String walletId) {
        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) return Optional.empty();

        for (WalletNode node : user.getWalletGraph().getWalletNodes()) {
            Wallet wallet = node.getWallet();
            if (wallet.getWalletId().equals(walletId)) {
                return Optional.of(wallet);
            }
        }
        return Optional.empty();
    }

    @Override
    public Wallet create(Wallet wallet) {
        if (findWalletById(wallet.getUserId(), wallet.getWalletId()).isPresent()) {
            throw new IllegalArgumentException("The wallet is already registered.");
        }

        User user = nexpay.getUserCRUD().safeRead(wallet.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found for wallet creation.");
        }

        WalletNode node = new WalletNode(wallet);
        user.getWalletGraph().getWalletNodes().addLast(node);
        persistence.saveWallet(wallet);
        return wallet;
    }

    @Override
    public void update(Wallet wallet) {
        delete(wallet.getWalletId());

        User user = nexpay.getUserCRUD().safeRead(wallet.getUserId());
        if (user != null) {
            user.getWalletGraph().getWalletNodes().addLast(new WalletNode(wallet));
        }

        SimpleList<Wallet> allWallets = new SimpleList<>();
        for (User u : nexpay.getUsers()) {
            for (WalletNode node : u.getWalletGraph().getWalletNodes()) {
                allWallets.addLast(node.getWallet());
            }
        }
        persistence.saveAllWallets(allWallets);
    }

    @Override
    public void delete(String walletId) {
        SimpleList<Wallet> allWallets = new SimpleList<>();

        for (User u : nexpay.getUsers()) {
            SimpleList<WalletNode> nodes = u.getWalletGraph().getWalletNodes();

            for (int i = 0; i < nodes.getSize(); i++) {
                WalletNode node = nodes.get(i);
                if (!node.getWallet().getWalletId().equals(walletId)) {
                    allWallets.addLast(node.getWallet());
                }
            }

            WalletNode toRemove = u.getWalletGraph().findWalletNode(walletId);
            if (toRemove != null) {
                u.getWalletGraph().getWalletNodes().remove(toRemove);
            }
        }

        persistence.saveAllWallets(allWallets);
    }


    @Override
    public Wallet read(String walletId) {
        for (User u : nexpay.getUsers()) {
            for (WalletNode node : u.getWalletGraph().getWalletNodes()) {
                if (node.getWallet().getWalletId().equals(walletId)) {
                    return node.getWallet();
                }
            }
        }
        throw new IllegalArgumentException("The wallet is not registered.");
    }
}
