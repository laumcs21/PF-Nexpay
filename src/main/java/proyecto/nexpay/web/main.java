package proyecto.nexpay.web;

import proyecto.nexpay.web.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class main {
    public static void main(String[] args) {

        Nexpay nexpay = Nexpay.getInstance();

        // Crear un usuario
        User user = new User("005", "1111", "Sebastian Rios", "seb@gmail.com", "3123456789", "Calle 123", 0);
        nexpay.getUserCRUD().create(user);

        // Crear dos monederos para el usuario
        Wallet wallet1 = new Wallet(WalletCodeGenerator.generateUniqueCode(6, nexpay.getWallets()), user.getId(), "Principal");
        Wallet wallet2 = new Wallet(WalletCodeGenerator.generateUniqueCode(6, nexpay.getWallets()), user.getId(), "Ahorros");
        if (nexpay.getWalletCRUD().create(wallet1) != null) {
            user.getWalletGraph().addWallet(wallet1);
        }
        if (nexpay.getWalletCRUD().create(wallet2) != null) {
            user.getWalletGraph().addWallet(wallet2);
        }


        // Crear una transacción directa
        Transaction transaction = new Transaction.Builder(
                user.getId(), wallet1.getWalletId(), "ABCDE", LocalDate.now(), TransactionType.DEPOSITO, 200000, "123456")
                .withDescription("Depósito monedero")
                .build();

        try {
            nexpay.getTManager().executeTransaction(transaction);


            // Mostrar transacciones en wallet1
            WalletNode walletNode1 = user.getWalletGraph().findWalletNode(wallet1.getWalletId());
            if (walletNode1 != null) {
                System.out.println("Transacciones en monedero: " + wallet1.getWalletId());
                for (Transaction t : walletNode1.getWallet().getTransactions()) {
                    System.out.println("  - " + t);
                }
            }


            System.out.println("Saldo Wallet1: " + wallet1.getBalance());
            System.out.println("Saldo Wallet2: " + wallet2.getBalance());
            System.out.println("Saldo total usuario: " + user.getTotalBalance());

        } catch (IllegalArgumentException e) {
            System.out.println("Error al ejecutar la transacción: " + e.getMessage());
        }
    }
}




