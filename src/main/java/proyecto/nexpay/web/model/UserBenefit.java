package proyecto.nexpay.web.model;

import java.io.Serializable;
import java.time.LocalDate;

public class UserBenefit implements Serializable {

    private boolean discountActive;
    private LocalDate freeWithdrawalsUntil;

    public UserBenefit() {
        this.discountActive = false;
        this.freeWithdrawalsUntil = null;
    }

    public boolean isDiscountActive() {
        return discountActive;
    }

    public void activateDiscount() {
        this.discountActive = true;
    }

    public void deactivateDiscount() {
        this.discountActive = false;
    }

    public void activateFreeWithdrawalsFor30Days() {
        this.freeWithdrawalsUntil = LocalDate.now().plusDays(30);
    }

    public boolean hasFreeWithdrawalsActive() {
        return freeWithdrawalsUntil != null && LocalDate.now().isBefore(freeWithdrawalsUntil);
    }

    public LocalDate getFreeWithdrawalsUntil() {
        return freeWithdrawalsUntil;
    }

    public void setFreeWithdrawalsUntil(LocalDate date) {
        this.freeWithdrawalsUntil = date;
    }
}
