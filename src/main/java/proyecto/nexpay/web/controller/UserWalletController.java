package proyecto.nexpay.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.service.Session;

@Controller
public class UserWalletController {

    private final Nexpay nexpay = Nexpay.getInstance();

    @GetMapping("/user/wallets")
    public String showUserWallets(Model model) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) return "redirect:/login";

        model.addAttribute("wallets", user.getWalletGraph().getWalletNodes());
        return "user-wallets";
    }

    @PostMapping("/user/wallets")
    public String createWallet(@RequestParam String name, RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) return "redirect:/login";

        String code = WalletCodeGenerator.generateUniqueCode(6, nexpay.getWallets());
        Wallet wallet = new Wallet(code, user.getId(), name);

        try {
            nexpay.getWalletCRUD().create(wallet);  // ← Ya se encarga de agregar al grafo
            redirectAttributes.addFlashAttribute("successMessage", "Monedero creado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo crear el monedero: " + e.getMessage());
        }

        return "redirect:/user/wallets";
    }


    @PostMapping("/user/wallet/delete")
    public String deleteWallet(@RequestParam String walletId, RedirectAttributes redirectAttributes) {
        String userId = Session.getUserId();
        if (userId == null) return "redirect:/login";

        User user = nexpay.getUserCRUD().safeRead(userId);
        if (user == null) return "redirect:/login";

        try {
            nexpay.getWalletCRUD().delete(walletId);
            WalletNode node = user.getWalletGraph().findWalletNode(walletId);
            if (node != null) {
                user.getWalletGraph().getWalletNodes().remove(node);
                redirectAttributes.addFlashAttribute("successMessage", "Monedero eliminado.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el monedero.");
        }

        return "redirect:/user/wallets";
    }

    @GetMapping("/user/wallet/access")
    public String accessWallet(@RequestParam String walletId) {
        Session.setSelectedWalletId(walletId);
        return "redirect:/dashboard";
    }
}

