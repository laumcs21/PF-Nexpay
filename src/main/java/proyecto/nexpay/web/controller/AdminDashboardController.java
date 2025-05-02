package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.service.Session;

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

    @GetMapping("/admin/transactions")
    public String manageTransactions(Model model) {
        if (!Session.isAdmin()) return "redirect:/login";

        model.addAttribute("transactions", nexpay.getTransactions());
        return "admin-transactions";
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
    public String manageAccounts(@RequestParam(required = false) String search, Model model) {
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

        model.addAttribute("accounts", filteredAccounts);
        model.addAttribute("search", search);
        return "Admin-Accounts";
    }

    @PostMapping("/admin/account/delete")
    public String deleteAccount(@RequestParam String id) {
        if (!Session.isAdmin()) return "redirect:/login";

        nexpay.getAccountCRUD().delete(id);
        return "redirect:/Admin/Accounts";
    }

    @GetMapping("/test")
    public String testView() {
        return "Admin-Accounts";
    }

}

