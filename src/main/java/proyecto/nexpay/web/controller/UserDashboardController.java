package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.service.Session;
import proyecto.nexpay.web.datastructures.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class UserDashboardController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/dashboard")
    public String showUserDashboard(Model model) {
        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();

        User user = nexpay.getUserCRUD().safeRead(userId);
        WalletNode walletNode = user.getWalletGraph().findWalletNode(walletId);

        if (user == null || walletNode == null) return "redirect:/login";

        model.addAttribute("name", user.getName());
        model.addAttribute("walletName", walletNode.getWallet().getName());
        model.addAttribute("walletBalance", walletNode.getWallet().getBalance());

        return "user-dashboard";
    }

    @GetMapping("/user/accounts")
    public String viewMyAccounts(@RequestParam(required = false) String editId, Model model) {
        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        WalletNode node = nexpay.getUserCRUD().safeRead(userId).getWalletGraph().findWalletNode(walletId);
        if (node == null) return "redirect:/dashboard";

        SimpleList<Account> accounts = node.getWallet().getAccounts();
        model.addAttribute("accounts", accounts);

        if (editId != null) {
            Account accountToEdit = nexpay.getAccountCRUD().safeRead(editId);
            model.addAttribute("accountToEdit", accountToEdit);
        }

        return "user-accounts";
    }

    @PostMapping("/user/accounts")
    public String createAccount(@RequestParam String bankName,
                                @RequestParam AccountType accountType,
                                RedirectAttributes redirectAttributes) {

        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        String accountId = AccountCodeGenerator.generateUniqueCode(5, nexpay.getAccounts());
        String accountNumber = AccountNumberGenerator.generateUniqueNumber(10, nexpay.getAccounts());
        double balance = 0.0;

        Account newAccount = new Account(userId, walletId, accountId, bankName, accountNumber, accountType, balance);
        Account created = nexpay.getAccountCRUD().create(newAccount);

        if (created != null) {
            WalletNode node = nexpay.getUserCRUD().safeRead(userId).getWalletGraph().findWalletNode(walletId);
            if (node != null) {
                node.getWallet().addAccount(created);
                node.getWallet().updateBalance();
            }
        }

        updateUserTotalBalance(userId);
        redirectAttributes.addFlashAttribute("successMessage", "Cuenta creada exitosamente.");
        return "redirect:/user/accounts";
    }

    @PostMapping("/user/account/delete")
    public String deleteAccount(@RequestParam String id, RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        Account account = nexpay.getAccountCRUD().safeRead(id);

        if (account != null && account.getUserId().equals(userId)) {
            nexpay.getAccountCRUD().delete(id);
            updateUserTotalBalance(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Cuenta eliminada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para eliminar esta cuenta.");
        }

        return "redirect:/user/accounts";
    }

    private void updateUserTotalBalance(String userId) {
        double total = 0.0;
        for (int i = 0; i < nexpay.getAccounts().getSize(); i++) {
            Account acc = nexpay.getAccounts().get(i);
            if (acc.getUserId().equals(userId)) {
                total += acc.getBalance();
            }
        }

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user != null) {
            user.setTotalBalance(total);
            nexpay.getUserCRUD().update(user);
        }
    }

    @PostMapping("/user/account/update")
    public String updateAccount(@ModelAttribute("accountToEdit") Account updatedAccount,
                                RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        Account account = nexpay.getAccountCRUD().safeRead(updatedAccount.getId());
        if (account != null && account.getUserId().equals(userId)) {
            account.setBankName(updatedAccount.getBankName());
            account.setAccountType(updatedAccount.getAccountType());
            nexpay.getAccountCRUD().update(account);
            redirectAttributes.addFlashAttribute("successMessage", "Cuenta actualizada.");
        }

        return "redirect:/user/accounts";
    }

    @GetMapping("/user/transactions")
    public String viewUserTransactions(Model model) {
        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        WalletNode node = nexpay.getUserCRUD().safeRead(userId).getWalletGraph().findWalletNode(walletId);
        if (node == null) return "redirect:/dashboard";

        model.addAttribute("transactions", node.getWallet().getTransactions());
        return "user-transactions";
    }

    @PostMapping("/user/transactions/undo")
    public String undoLastUserTransaction(RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        boolean success = nexpay.getTManager().undoLastTransaction();

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Última transacción revertida.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No hay transacción para revertir.");
        }

        return "redirect:/user/transactions";
    }

    @GetMapping("/user/transactions/new")
    public String showTransactionForm(Model model) {
        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        WalletNode node = nexpay.getUserCRUD().safeRead(userId).getWalletGraph().findWalletNode(walletId);
        if (node == null) return "redirect:/dashboard";

        model.addAttribute("accounts", node.getWallet().getAccounts());
        return "User-Make-Transaction";
    }

    @PostMapping("/user/transactions")
    public String createTransaction(@RequestParam String sourceAccountNumber,
                                    @RequestParam(required = false) String destinationAccountNumber,
                                    @RequestParam TransactionType type,
                                    @RequestParam double amount,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String scheduledDate,
                                    RedirectAttributes redirectAttributes) {

        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        try {
            Transaction.Builder builder = new Transaction.Builder(
                    userId,
                    walletId,
                    TransactionCodeGenerator.generateUniqueCode(5, nexpay.getTransactions()),
                    LocalDate.now(),
                    type,
                    amount,
                    sourceAccountNumber
            );

            if (destinationAccountNumber != null && !destinationAccountNumber.isBlank()) {
                builder.withDestinationAccountNumber(destinationAccountNumber);
            }

            if (description != null) {
                builder.withDescription(description);
            }

            if (scheduledDate != null && !scheduledDate.isBlank()) {
                LocalDateTime scheduled = LocalDateTime.parse(scheduledDate);
                builder.withScheduledDate(scheduled);
            }

            Transaction tx = builder.build();

            if (scheduledDate != null && !scheduledDate.isBlank()) {
                LocalDateTime scheduled = LocalDateTime.parse(scheduledDate);
                nexpay.getSManager().scheduleTransaction(tx, scheduled);
                redirectAttributes.addFlashAttribute("successMessage", "Transacción programada con éxito.");
            } else {
                nexpay.getTManager().executeTransaction(tx);
                redirectAttributes.addFlashAttribute("successMessage", "Transacción creada con éxito.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/transactions";
    }
}



