package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.service.Session;
import proyecto.nexpay.web.datastructures.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nexpay.web.util.GraphStats;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class UserDashboardController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/dashboard")
    public String showUserDashboard(Model model) {
        String userId = Session.getUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) {
            model.addAttribute("errorMessage", "Your session is invalid or user does not exist.");
            return "error-page"; // o redirige a login
        }

        String walletId = Session.getSelectedWalletId();
        WalletNode node = user.getWalletGraph().findWalletNode(walletId);

        if (node == null) {
            model.addAttribute("errorMessage", "No wallet selected or wallet does not exist.");
            return "error-page";
        }

        model.addAttribute("name", user.getName());
        model.addAttribute("walletName", node.getWallet().getName());
        model.addAttribute("walletBalance", node.getWallet().getBalance());

        model.addAttribute("points", user.getPoints());
        model.addAttribute("rank", nexpay.getRankManager().getRank(user.getId(), user.getPoints()));

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
                                @RequestParam String category,  // ⬅️ nuevo
                                RedirectAttributes redirectAttributes) {


        String userId = Session.getUserId();
        String walletId = Session.getSelectedWalletId();
        if (userId == null || walletId == null) return "redirect:/login";

        String accountId = AccountCodeGenerator.generateUniqueCode(5, nexpay.getAccounts());
        String accountNumber = AccountNumberGenerator.generateUniqueNumber(10, nexpay.getAccounts());
        double balance = 0.0;

        Account newAccount = new Account(userId, walletId, accountId, bankName, accountNumber, accountType, balance, category);
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

    @GetMapping("/user/points")
    @ResponseBody
    public String showPoints() {
        String userId = Session.getUserId();
        if (userId == null) return "No estás logueado";

        User user = nexpay.getUserCRUD().safeRead(userId);
        int points = user.getPoints();
        String rank = nexpay.getRankManager().getRank(user.getId(), points);

        return "Puntos acumulados: " + points + "\nRango: " + rank;
    }

    @GetMapping("/user/benefits")
    public String showBenefitsPage(Model model) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        int points = user.getPoints();
        String rank = nexpay.getRankManager().getRank(user.getId(), points);

        model.addAttribute("points", points);
        model.addAttribute("rank", rank);
        return "user-benefits";
    }

    @PostMapping("/user/redeem")
    public String redeemBenefit(@RequestParam String benefit, RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        int points = user.getPoints();
        boolean benefitApplied = false;
        String rank = nexpay.getRankManager().getRank(user.getId(), points);

        switch (benefit) {
            case "discount":
                if (!rank.equals("Plata") && !rank.equals("Oro") && !rank.equals("Platino")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "This benefit requires rank Plata or higher.");
                    break;
                }
                if (points >= 100) {
                    user.setPoints(points - 100);
                    user.getBenefits().activateDiscount();
                    System.out.println("Puntos actualizados: " + user.getPoints());
                    redirectAttributes.addFlashAttribute("successMessage", "Descuento de comision del 10% activado correctamente.");
                    benefitApplied = true;
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Insufficient points.");
                }
                break;

            case "free_withdrawals":
                if (!rank.equals("Oro") && !rank.equals("Platino")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "This benefit requires rank Oro or higher.");
                    break;
                }
                if (points >= 500) {
                    user.setPoints(points - 500);
                    user.getBenefits().activateFreeWithdrawalsFor30Days();
                    System.out.println("Puntos actualizados: " + user.getPoints());
                    redirectAttributes.addFlashAttribute("successMessage", "Recargo Gratis por 30 dias.");
                    benefitApplied = true;
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Puntos Insuficientes.");
                }
                break;

            case "bonus_50":
                if (!rank.equals("Platino")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "This benefit requires rank Platino.");
                    break;
                }
                if (points >= 1000) {
                    user.setPoints(points - 1000);

                    WalletNode node = user.getWalletGraph().findWalletNode(Session.getSelectedWalletId());
                    if (node != null && !node.getWallet().getAccounts().isEmpty()) {
                        Account account = node.getWallet().getAccounts().get(0);
                        account.setBalance(account.getBalance() + 50);
                        nexpay.getAccountCRUD().update(account);

                        node.getWallet().updateBalance();

                        // Recalcular y actualizar total balance del usuario
                        double total = 0.0;
                        for (WalletNode wn : user.getWalletGraph().getWalletNodes()) {
                            total += wn.getWallet().getBalance();
                        }
                        user.setTotalBalance(total);

                        redirectAttributes.addFlashAttribute("successMessage", "$Bono de 50$ aplicado exitosamente.");
                        benefitApplied = true;
                        System.out.println("Puntos actualizados: " + user.getPoints());
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "No se encontro cuenta para aplicar el bono.");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Puntos insuficientes.");
                }
                break;

            default:
                redirectAttributes.addFlashAttribute("errorMessage", "Beneficio no valido.");
        }

        // Solo si se aplicó correctamente el beneficio:
        if (benefitApplied) {
            nexpay.getUserCRUD().update(user); // persistir cambios del usuario
            System.out.println("Puntos actualizados: " + user.getPoints());
            nexpay.getPointManager().addPoints(user.getId(), user.getPoints());
            nexpay.getRankManager().insertOrUpdate(user.getId(), user.getPoints());
            nexpay.getPointHistoryManager().register(user.getId(), "Redeemed benefit: " + benefit, user.getPoints());
            nexpay.getPointHistoryManager().saveHistory();
        }

        return "redirect:/user/benefits";


    }

    @GetMapping("/user/points/history")
    public String showPointHistory(Model model) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        SimpleList<PointHistoryEntry> history = nexpay.getPointHistoryManager().getHistoryForUser(userId);
        model.addAttribute("history", history);
        return "user-point-history";
    }


    @GetMapping("/user/graph/categories")
    public String showCategoryGraph(Model model) {
        DirectedGraph<String> graph = nexpay.getCategoryGraphManager().getGraph();

        GraphStats stats = GraphStats.calculate(graph);

        model.addAttribute("graphType", "Categorías de Gasto");
        model.addAttribute("graph", graph.getAllNodes());
        model.addAttribute("stats", stats);
        return "graph-analysis";
    }





}



