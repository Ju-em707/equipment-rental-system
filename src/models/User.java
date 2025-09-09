package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    public enum UserType {
        ADMIN, CUSTOMER
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, LOCKED
    }

    private String userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private UserType userType;
    private AccountStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdDate;
    private int failedLoginAttempts;

    public User(String userId, String username, String password, String fullName,
                String email, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.status = AccountStatus.ACTIVE;
        this.createdDate = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public User(String userId, String username, String passwordHash, String fullName,
                String email, UserType userType,  AccountStatus status,
                LocalDateTime lastLogin, LocalDateTime createdDate, int failedAttempts) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.status = status;
        this.lastLogin = lastLogin;
        this.createdDate = createdDate;
        this.failedLoginAttempts = failedAttempts;
    }


    public String hashPassword(String password) {
        return "HASH_" + password.hashCode() + "_" + username.hashCode();
    }

    public boolean verifyPassword(String password) {
        return hashPassword(password).equals(this.passwordHash);
    }

    public void updatePassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
        this.failedLoginAttempts = 0;
    }

    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.status = AccountStatus.LOCKED;
        }
    }

    public boolean canLogin() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isCustomer() {
        return userType == UserType.CUSTOMER;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String lastLoginStr = lastLogin != null ? lastLogin.format(formatter) : "Never";

        return String.format("User ID: %s | Username: %s | Name: %s | Type: %s | Status: %s | Last Login: %s",
                userId, username, fullName, userType, status, lastLoginStr);
    }

    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String lastLoginStr = lastLogin != null ? lastLogin.format(formatter) : "";
        String createdDateStr = createdDate != null ? createdDate.format(formatter) : "";

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d",
                userId, username, passwordHash, fullName, email,
                userType, status, lastLoginStr, createdDateStr, failedLoginAttempts);
    }

}
