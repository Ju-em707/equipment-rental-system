package data;

import models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserFileHandler {
    private static final String USERS_FILE = "users.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try {
            if (!Files.exists(Path.of(USERS_FILE))) {
                System.out.println("Users file not found. Will create default accounts.");
                return users;
            }

            List<String> lines = Files.readAllLines(Path.of(USERS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                try {
                    User user = parseUserFromCsv(line);
                    if (user != null) {
                        users.add(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing user line: " + line + " - " + e.getMessage());

                }
            }

            System.out.println("Loaded " + users.size() + " users from file.");

        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    public static void saveUsers(List<User> users) {
        try {
            List<String> lines = new ArrayList<>();
            for (User user : users) {
                lines.add(user.toCsvString());
            }
            Files.write(Path.of(USERS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private static User parseUserFromCsv(String line) {
        String[] parts = line.split(",");

        if (parts.length < 6) {
            System.err.println("Invalid user line format: " + line);
            return null;
        }

        try {
            String userId = parts[0].trim();
            String username = parts[1].trim();
            String passwordHash = parts[2].trim();
            String fullName = parts[3].trim();
            String email = parts[4].trim();
            User.UserType userType = User.UserType.valueOf(parts[5].trim());

            User.AccountStatus status = parts.length > 6 && !parts[6].trim().isEmpty() ?
                    User.AccountStatus.valueOf(parts[6].trim()) : User.AccountStatus.ACTIVE;

            LocalDateTime lastLogin = null;
            if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                try {
                    lastLogin = LocalDateTime.parse(parts[7].trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    // keep it null
                }
            }

            LocalDateTime createdDate;
            if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                try {
                    createdDate = LocalDateTime.parse(parts[8].trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    createdDate = LocalDateTime.now();
                }
            } else {
                createdDate = LocalDateTime.now();
            }

            int failedAttempts = 0;
            if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                try {
                    failedAttempts = Integer.parseInt(parts[9].trim());
                } catch (NumberFormatException e) {
                    // keep it 0
                }
            }

            return new User(userId, username, passwordHash, fullName, email,
                    userType, status, lastLogin, createdDate, failedAttempts);

        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing user enums: " + e.getMessage());
            return null;
        }
    }
}
