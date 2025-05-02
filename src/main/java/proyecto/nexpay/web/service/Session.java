package proyecto.nexpay.web.service;

public class Session {
    private static boolean isAdmin;
    private static String userId;

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
}
