package services;

import data.UserFileHandler;
import models.User;

import java.util.List;

public class AuthenticationService {
    private List<User> users;
    private User currentUser;

    public AuthenticationService() {
        loadUsers();
    }

    public void loadUsers() {
        users = UserFileHandler.loadUsers();

        if (users.isEmpty()) {
            createDefaultAccounts();
        }
    }

    private void createDefaultAccounts() {
        System.out.println("Creating default accounts...");

        User admin = new User("A001", "admin", "admin123", "System Administrator",
                "admin@rental.com", User.UserType.ADMIN);
        User customer1 = new User("C001", "john.doe", "customer123", "John Doe",
                "john@email.com", User.UserType.CUSTOMER);
        User customer2 = new User("C002", "jane.doe", "customer123", "Jane Smith",
                "jane@email.com", User.UserType.CUSTOMER);

        users.add(admin);
        users.add(customer1);
        users.add(customer2);

        UserFileHandler.saveUsers(users);
        System.out.println("Default accounts created successfully!");
        System.out.println("Admin: username='admin', password='admin123'");
        System.out.println("Customer 1: username='john.doe', password='customer123'");
        System.out.println("Customer 2: username='jane.smith', password='customer123'");
    }

    public AuthenticationResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthenticationResult(false, "Username cannot be empty", null);
        }

        if (password == null || password.trim().isEmpty()) {
            return new AuthenticationResult(false, "Password cannot be empty", null);
        }

        User user = findUserByUsername(username);

        if (user == null) {
            return new AuthenticationResult(false, "Username not found", null);
        }

        if (!user.canLogin()) {
            String reason = user.getStatus() == User.AccountStatus.LOCKED ?
                    "Account is locked due to multiple failed login attempts" :
                    "Account is inactive";
            return new AuthenticationResult(false, reason, null);
        }

        if (!user.verifyPassword(password)) {
            user.recordFailedLogin();
            UserFileHandler.saveUsers(users);

            String message = "Invalid password";
            if (user.getStatus() == User.AccountStatus.LOCKED) {
                message += ". Account has been locked due to multiple failed attempts.";
            }
            return new AuthenticationResult(false, message, null);
        }

        user.recordSuccessfulLogin();
        this.currentUser = user;
        UserFileHandler.saveUsers(users);

        return new AuthenticationResult(true, "Login successful", user);
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Goodbye, " + currentUser.getFullName() + "!");
            currentUser = null;
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isCurrentUserCustomer() {
        return currentUser != null && currentUser.isCustomer();
    }

    // admin only
    public List<User> getAllUsers() {
        return users;
    }

    // admin only
    public boolean unlockUserAccount(String username) {
        User user = findUserByUsername(username);
        if (user != null && user.getStatus() == User.AccountStatus.LOCKED) {
            user.setStatus(User.AccountStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            UserFileHandler.saveUsers(users);
            return true;
        }
        return false;
    }

    private User findUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = findUserByUsername(username);
        if (user != null && user.verifyPassword(oldPassword)) {
            user.updatePassword(newPassword);
            UserFileHandler.saveUsers(users);
            return true;
        }
        return false;
    }

    public boolean registerCustomer(String username, String password, String fullName, String email) {
        if (findUserByUsername(username) != null) {
            return false;
        }

        String userId = generateCustomerId();
        User newUser = new User(userId, username, password, fullName, email, User.UserType.CUSTOMER);
        users.add(newUser);
        UserFileHandler.saveUsers(users);
        return true;
    }

    private String generateCustomerId() {
        int maxId = 0;
        for (User user : users) {
            if (user.getUserId().startsWith("C")) {
                try {
                    int id = Integer.parseInt(user.getUserId().substring(1));
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                    // ignore malformed IDs
                }
            }
        }
        return String.format("C%03d", maxId + 1);
    }

    public record AuthenticationResult(boolean success, String message, User user) {}
}
