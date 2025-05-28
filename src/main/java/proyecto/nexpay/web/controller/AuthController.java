package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nexpay.web.model.Nexpay;
import proyecto.nexpay.web.model.User;
import proyecto.nexpay.web.model.Administrator;
import proyecto.nexpay.web.service.Session;

@Controller
public class AuthController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        Model model) {
        Administrator admin = Administrator.getInstance();

        if (id.equals(admin.getId()) && password.equals(admin.getPassword())) {
            Session.setIsAdmin(true);
            Session.setUserId("admin");
            return "redirect:/admin-dashboard";
        }

        User user = nexpay.getUserCRUD().safeRead(id);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Identificación o contraseña incorrecta.");
            return "login";
        }

        Session.setIsAdmin(false);
        Session.setUserId(user.getId());

        return "redirect:/user/wallets";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String id,
                           @RequestParam String password,
                           @RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String address,
                           Model model) {

        User existingUser = nexpay.getUserCRUD().safeRead(id);
        if (existingUser != null) {
            model.addAttribute("error", "Ya existe un usuario con esa identificación.");
            return "register";
        }

        User newUser = new User(
                id,
                password,
                name,
                email,
                phone,
                address,
                0.0
        );

        nexpay.getUserCRUD().create(newUser);

        return "redirect:/login";
    }


    @GetMapping("/logout")
    public String logout() {
        Session.setUserId(null);
        Session.setIsAdmin(false);
        Session.setSelectedWalletId(null);
        return "redirect:/login";
    }

}

