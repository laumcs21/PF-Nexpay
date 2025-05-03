package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.datastructures.*;
import proyecto.nexpay.web.service.Session;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AdminDashboardController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/admin-dashboard")
    public String showAdminDashboard() {
        if (!Session.isAdmin()) {
            return "redirect:/login";
        }
        return "admin-dashboard";
    }

    @GetMapping("/admin/users")
    public String manageUsers(@RequestParam(required = false) String search, Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        SimpleList<User> allUsers = nexpay.getUsers();
        SimpleList<User> filteredUsers = new SimpleList<>();

        for (int i = 0; i < allUsers.getSize(); i++) {
            User user = allUsers.get(i);
            if (search == null || user.getId().toLowerCase().contains(search.toLowerCase())
                    || user.getName().toLowerCase().contains(search.toLowerCase())) {
                filteredUsers.addFirst(user);
            }
        }

        model.addAttribute("users", filteredUsers);
        model.addAttribute("search", search);

        return "admin-users";
    }

    @PostMapping("/admin/user/delete")
    public String deleteUser(@RequestParam String id) {
        if (!Session.isAdmin()) return "redirect:/login";

        nexpay.getUserCRUD().delete(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/user/edit")
    public String showEditUserForm(@RequestParam String id, Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(id);
        if (user == null) return "redirect:/admin/users";

        model.addAttribute("user", user);
        return "admin-edit-user";
    }

    @PostMapping("/admin/user/update")
    public String updateUser(@RequestParam String id,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address) {

        if (!Session.isAdmin()) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(id);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);
        }

        nexpay.getUserCRUD().update(user);

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/accounts")
    public String manageAccounts(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) String editId,
                                 Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        SimpleList<Account> allAccounts = nexpay.getAccounts();
        SimpleList<Account> filteredAccounts = new SimpleList<>();

        for (int i = 0; i < allAccounts.getSize(); i++) {
            Account account = allAccounts.get(i);
            if (search == null || account.getId().toLowerCase().contains(search.toLowerCase())
                    || account.getUserId().toLowerCase().contains(search.toLowerCase())) {
                filteredAccounts.addLast(account);
            }
        }

        // Si se pasa un editId, agregamos al modelo el objeto account correspondiente
        if (editId != null) {
            Account accountToEdit = nexpay.getAccountCRUD().safeRead(editId);
            model.addAttribute("accountToEdit", accountToEdit);
        }

        model.addAttribute("accounts", filteredAccounts);
        model.addAttribute("search", search);
        return "Admin-Accounts";  // Página donde se lista y edita la cuenta
    }

    // Método para manejar la eliminación de una cuenta
    @PostMapping("/admin/account/delete")
    public String deleteAccount(@RequestParam String id) {
        if (!Session.isAdmin()) return "redirect:/login";

        nexpay.getAccountCRUD().delete(id);
        return "redirect:/admin/accounts";
    }

    // Método para mostrar el formulario de edición de cuenta
    @GetMapping("/admin/account/edit")
    public String showEditAccountForm(@RequestParam String id, Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        Account account = nexpay.getAccountCRUD().safeRead(id);
        if (account == null) return "redirect:/admin/accounts";

        model.addAttribute("accountToEdit", account);  // Cambié 'account' a 'accountToEdit'
        return "Admin-Accounts";  // Regresamos a la misma página de cuentas, donde se editará
    }

    // Método para manejar la actualización de una cuenta
    @PostMapping("/admin/account/update")
    public String updateAccount(@ModelAttribute("accountToEdit") Account account) {
        if (!Session.isAdmin()) return "redirect:/login";

        // Actualiza la cuenta
        if (account != null) {
            nexpay.getAccountCRUD().update(account);
        }

        return "redirect:/admin/accounts";
    }

    @PostMapping("/admin/accounts")
    public String createAccount(@RequestParam String userId,
                                @RequestParam String bankName,
                                @RequestParam AccountType accountType) {
        if (!Session.isAdmin()) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) {
            return "redirect:/admin/accounts?error=userNotFound";
        }

        String generatedId = AccountCodeGenerator.generateUniqueCode(5, nexpay.getAccounts());
        String generatedAccountNumber = AccountNumberGenerator.generateUniqueNumber(5, nexpay.getAccounts());
        double balance = 0.0;

        Account newAccount = new Account(userId, generatedId, bankName, generatedAccountNumber, accountType, balance);
        nexpay.getAccountCRUD().create(newAccount);

        updateUserTotalBalance(user);

        return "redirect:/admin/accounts";
    }

    private void updateUserTotalBalance(User user) {
        double total = 0.0;

        for (int i = 0; i < nexpay.getAccounts().getSize(); i++) {
            Account account = nexpay.getAccounts().get(i);
            if (account.getUserId().equals(user.getId())) {
                total += account.getBalance();
            }
        }

        user.setTotalBalance(total);
        nexpay.getUserCRUD().update(user);
    }

    @GetMapping("/admin/transactions")
    public String manageTransactions(@RequestParam(required = false) String search, String error, Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        var all = nexpay.getTransactions();
        var filtered = new DoubleLinkedList<Transaction>();

        for (int i = 0; i < all.getSize(); i++) {
            Transaction t = all.get(i);
            if (search == null || t.getId().toLowerCase().contains(search.toLowerCase())) {
                filtered.addLast(t);
            }
        }

        model.addAttribute("transactions", filtered);
        model.addAttribute("search", search);
        model.addAttribute("errorMessage", error);
        return "admin-transactions";
    }

    @PostMapping("/admin/transactions")
    public String createTransaction(@RequestParam String userId,
                                    @RequestParam String sourceAccountNumber,
                                    @RequestParam(required = false) String destinationAccountNumber,
                                    @RequestParam TransactionType type,
                                    @RequestParam double amount,
                                    @RequestParam(required = false) String description,
                                    RedirectAttributes redirectAttributes) {

        if (!Session.isAdmin()) return "redirect:/login";

        try {
            var transaction = new Transaction.Builder(
                    userId,
                    TransactionCodeGenerator.generateUniqueCode(5, nexpay.getTransactions()),
                    LocalDate.now(),
                    type,
                    amount,
                    sourceAccountNumber
            )
                    .withDestinationAccountNumber(destinationAccountNumber)
                    .withDescription(description)
                    .build();

            nexpay.getTManager().executeTransaction(transaction);
            return "redirect:/admin/transactions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/transactions";
        }
    }

    @PostMapping("/admin/transactions/undo")
    public String undoTransaction() {
        if (!Session.isAdmin()) return "redirect:/login";

        nexpay.getTManager().undoLastTransaction();
        return "redirect:/admin/transactions";
    }

}







