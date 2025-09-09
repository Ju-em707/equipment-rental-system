package utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EQUIPMENT_ID_PATTERN = Pattern.compile("^E\\d{3,}$");
    private static final Pattern RENTAL_ID_PATTERN = Pattern.compile("^R\\d{3,}$");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[AC]\\d{3,}$"); // Admin or Customer ID
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isValidEquipmentId(String id) {
        return id != null && EQUIPMENT_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidRentalId(String id) {
        return id != null && RENTAL_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidCustomerName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isValidRentDays(int days) {
        return days > 0 && days <= 365;
    }

    public static boolean isValidRentRate(double rate) {
        return rate >= 0 && rate <= 10000;
    }

    public static boolean isValidUserId(String userId) {
        return userId != null && USER_ID_PATTERN.matcher(userId.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && password.length() <= 50;
    }

    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().length() >= 2 && fullName.trim().length() <= 100;
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                .replaceAll("[,\\r\\n]", " ") // remove any of the
                .replaceAll("\\s+", " ") // contained chars
                .trim(); // trim the space replacement
    }

    public static String sanitizeUsername(String username) {
        if (username == null) return "";
        return username.trim().toLowerCase().replaceAll("[^a-zA-Z0-9._-]", "");
    }

    public static String validateUserRegistration(String username, String password,
                                                   String fullName, String email) {
        if (!isValidUsername(username)) {
            return "Username must be 3-20 characters and contain only letters, numbers, dots, underscores, or hyphens.";
        }

        if (!isValidPassword(password)) {
            return "Password must be between 6-50 characters.";
        }

        if (!isValidFullName(fullName)) {
            return "Full name must be 2-100 characters.";
        }

        if (!isValidEmail(email)) {
            return "Invalid email format";
        }

        return null;
    }
}
