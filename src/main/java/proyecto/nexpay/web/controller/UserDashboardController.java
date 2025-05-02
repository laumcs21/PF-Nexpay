package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proyecto.nexpay.web.model.Nexpay;
import proyecto.nexpay.web.model.User;
import proyecto.nexpay.web.service.Session;

@Controller
public class UserDashboardController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/dashboard")
    public String showUserDashboard(Model model) {
        User user = nexpay.getUserCRUD().read(Session.getUserId());

        if (user != null) {
            model.addAttribute("name", user.getName());
            model.addAttribute("totalBalance", user.getTotalBalance());
        }

        return "user-dashboard";
    }

}

