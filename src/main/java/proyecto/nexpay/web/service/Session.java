package proyecto.nexpay.web.service;

public class Session {
    private static boolean isAdmin;
    private static String userId;
    private static String selectedWalletId; // Nuevo atributo

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static void setIsAdmin(boolean isAdmin) {
        Session.isAdmin = isAdmin;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        Session.userId = userId;
    }

    // Nuevos métodos para el wallet seleccionado
    public static String getSelectedWalletId() {
        return selectedWalletId;
    }

    public static void setSelectedWalletId(String selectedWalletId) {
        Session.selectedWalletId = selectedWalletId;
    }

    public static void clearSession() {
        Session.userId = null;
        Session.isAdmin = false;
        Session.selectedWalletId = null;
    }
}

