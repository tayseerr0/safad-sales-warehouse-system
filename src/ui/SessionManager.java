package ui;

import model.AppUser;

public class SessionManager {

    private static AppUser currentUser;

    private SessionManager() {
    }

    public static void login(AppUser user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static AppUser getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUsername() {
        return currentUser == null ? "" : currentUser.getUsername();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
