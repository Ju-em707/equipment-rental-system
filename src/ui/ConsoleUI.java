package ui;

import services.*;
import models.*;
import utils.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ConsoleUI {
    private final AuthenticationService authService;
    private final RentalService rentalService;
    private final ReportGenerator reportGenerator;
    private final Scanner scanner;
    private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleUI(AuthenticationService authService, RentalService rentalService) {
        this.authService = authService;
        this.rentalService = rentalService;
        this.reportGenerator = new ReportGenerator(rentalService, authService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n\t\t========================================");
        System.out.println("\t\t\t\t EQUIPMENT RENTAL SYSTEM");
        System.out.println("\t\t========================================\n");

        while (true) {
            try {
                if (!authService.isLoggedIn()) {
                    showLoginMenu();
                } else {
                    showMainMenu();
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                break;
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("""
                \t\t=== LOGIN REQUIRED ===\
                \n1. Login\
                \n2. View Default Accounts\
                \n3. Exit
                """);

        int choice = getIntInput("Select option: ");

        switch (choice) {
            case 1: handleLogin(); break;
            case 2: showDefaultAccounts(); break;
            case 3:
                System.out.println("Thank you for using Equipment Rental System!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void handleLogin() {
        System.out.println("\n\t\t=== USER LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isBlank()) {
            System.out.println("Username cannot be blank.");
            pressEnterToContinue(scanner);
            clearScreen();
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        AuthenticationService.AuthenticationResult result = authService.login(username, password);

        if (result.success()) {
            User user = result.user();
            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + user.getFullName());
            System.out.println("Role: " + user.getUserType());

            if (user.getLastLogin() != null) {
                System.out.println("Last login: " + user.getLastLogin().format(DATETIME_FORMATTER));
            }
        } else {
            System.out.println("\nLogin failed: " + result.message());
        }

        pressEnterToContinue(scanner);
        clearScreen();
    }

    private void showMainMenu() {
        clearScreen();
        User currentUser = authService.getCurrentUser();

        System.out.println("\n\t\t========================================");
        System.out.printf("\t\t\tWelcome, %s\n", currentUser.getFullName());
        System.out.printf("\t\t\tRole: %s | ID: %s\n", currentUser.getUserType(), currentUser.getUserId());
        System.out.println("\t\t========================================");

        if (currentUser.isAdmin()) {
            showAdminMenu();
        } else {
            showCustomerMenu();
        }
    }

    private void showAdminMenu() {
        while (true) {
            System.out.println("""
                    \t\t=== ADMIN DASHBOARD ===\
                    \n1. Equipment Management\
                    \n2. User Management\
                    \n3. Rental Management\
                    \n4. Reports & Analytics\
                    \n5. System Overview\
                    \n6. My Profile\
                    \n7. Logout
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: showEquipmentManagementMenu(); break;
                case 2: showUserManagementMenu(); break;
                case 3: showRentalManagementMenu(); break;
                case 4: showReportsMenu(); break;
                case 5: showSystemOverview(); break;
                case 6: showUserProfile(); break;
                case 7: handleLogout(); return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
            clearScreen();
        }
    }

    private void showCustomerMenu() {
        while (true) {
            System.out.println("""
                    \t\t=== CUSTOMER DASHBOARD ===\
                    \n1. Browse Available Equipment\
                    \n2. Rent Equipment\
                    \n3. Return Equipment\
                    \n4. My Rentals\
                    \n5. My Rental History\
                    \n6. My Profile\
                    \n7. Logout
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: showAvailableEquipment(); break;
                case 2: handleRentEquipment(); break;
                case 3: handleReturnEquipment(); break;
                case 4: showMyActiveRentals(); break;
                case 5: showMyRentalHistory(); break;
                case 6: showUserProfile(); break;
                case 7: handleLogout(); return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
            clearScreen();
        }
    }

    private void showEquipmentManagementMenu() {
        while (true) {
            clearScreen();
            System.out.println("""
                    \t\t=== EQUIPMENT MANAGEMENT ===\
                    \n1. View All Equipment\
                    \n2. Add New Equipment\
                    \n3. Search Equipment\
                    \n4. Equipment Usage Statistics\
                    \n5. Back to Main Menu
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: showAllEquipment(); continue;
                case 2: handleAddEquipment(); continue;
                case 3: handleSearchEquipment(); continue;
                case 4: showEquipmentStats(); continue;
                case 5: return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private void showUserManagementMenu() {
        while (true) {
            clearScreen();
            System.out.println("""
                    \t\t=== USER MANAGEMENT ===\
                    \n1. View All Users\
                    \n2. Register New Customer\
                    \n3. Unlock User Accounts\
                    \n4. User Activity Report\
                    \n5. Back to Main Menu
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: showAllUsers(); continue;
                case 2: handleRegisterCustomer(); continue;
                case 3: handleUnlockAccount(); continue;
                case 4: showUserActivityReport(); continue;
                case 5: return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private void showRentalManagementMenu() {
        while (true) {
            clearScreen();
            System.out.println("""
                    \t\t=== RENTAL MANAGEMENT ===\
                    \n1. View All Active Rentals\
                    \n2. View Overdue Rentals\
                    \n3. Process Equipment Return\
                    \n4. Customer Rental History\
                    \n5. Back to Main Menu
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: showAllActiveRentals(); continue;
                case 2: showOverdueRentals(); continue;
                case 3: handleReturnEquipment(); continue;
                case 4: handleCustomerRentalHistory(); continue;
                case 5: return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private void showReportsMenu() {
        while (true) {
            clearScreen();
            System.out.println("""
                    \t\t=== REPORTS & ANALYTICS ===\
                    \n1. Daily Summary\
                    \n2. Monthly Report\
                    \n3. Customer Analytics\
                    \n4. Equipment Performance\
                    \n5. Financial Summary\
                    \n6. Security Audit\
                    \n7. Back to Main Menu
                    """);

            int choice = getIntInput("\nSelect option: ");

            switch (choice) {
                case 1: handleDailySummary(); continue;
                case 2: handleMonthlyReport(); continue;
                case 3: showCustomerAnalytics(); continue;
                case 4: showEquipmentPerformance(); continue;
                case 5: showFinancialSummary(); continue;
                case 6: showSecurityAudit(); continue;
                case 7: return;
                default: System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private void showAllEquipment() {
        System.out.println("\n\t\t\t=== ALL EQUIPMENT ===");
        List<Equipment> equipment = rentalService.getAllEquipment();

        if (equipment.isEmpty()) {
            System.out.println("\nNo equipment found.");
            pressEnterToContinue(scanner);
            return;
        }

        Map<String, List<Equipment>> byCategory = new HashMap<>();
        for (Equipment eq : equipment) {
            byCategory.computeIfAbsent(eq.getCategory(), k -> new ArrayList<>()).add(eq);
        }

        for (Map.Entry<String, List<Equipment>> entry : byCategory.entrySet()) {
            System.out.println("\n" + entry.getKey().toUpperCase() + ":");
            System.out.printf("%-8s %-25s %-12s %-15s%n", "ID", "Name", "Rate/day", "Status");
            System.out.println("-".repeat(60));

            for (Equipment eq : entry.getValue()) {
                System.out.printf("%-8s %-25s $%-11.2f %-15s%n",
                        eq.getId(), eq.getName(), eq.getRentPerDay(), eq.getAvailability());
            }
        }

        System.out.println("\nTotal Equipment: " + equipment.size());

        pressEnterToContinue(scanner);
    }

    private void showAvailableEquipment() {
        System.out.println("\n\t\t\t=== AVAILABLE EQUIPMENT ===");
        List<Equipment> available = rentalService.getAvailableEquipment();

        if (available.isEmpty()) {
            System.out.println("\nNo equipment currently available for rent.");
            pressEnterToContinue(scanner);
            return;
        }

        Map<String, List<Equipment>> byCategory = new HashMap<>();
        for (Equipment eq : available) {
            byCategory.computeIfAbsent(eq.getCategory(), k -> new ArrayList<>()).add(eq);
        }

        for (Map.Entry<String, List<Equipment>> entry : byCategory.entrySet()) {
            System.out.println("\n" + entry.getKey().toUpperCase() + ":");
            System.out.printf("%-8s %-25s %-12s%n", "ID", "Name", "Rate/day");
            System.out.println("-".repeat(45));

            for (Equipment eq : entry.getValue()) {
                System.out.printf("%-8s %-25s %-11.2f%n", eq.getId(), eq.getName(), eq.getRentPerDay());
            }
        }

        System.out.println("\nTotal Available: " + available.size());

        pressEnterToContinue(scanner);
    }

    private void handleAddEquipment() {
        System.out.println("\n\t\t\t=== ADD NEW EQUIPMENT ===");

        System.out.print("Equipment Name: ");
        String name = scanner.nextLine().trim();

        if (name.isBlank()) {
            System.out.println("\nEquipment name cannot be blank.");
            pressEnterToContinue(scanner);
            return;
        }

        double rentPerDay = getDoubleInput("Daily Rent Rate ($): ");
        if (rentPerDay <= 0) {
            System.out.println("\nRent rate must be greater than 0.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.print("Category (or press Enter for 'General'): ");
        String category = scanner.nextLine().trim();

        if (category.isBlank()) {
            category = Constants.DEFAULT_CATEGORY;
        }

        if (rentalService.addEquipment(name, rentPerDay, category)) {
            System.out.println("\nEquipment added successfully!");
        } else {
            System.out.println("\nFailed to add Equipment.");
        }

        pressEnterToContinue(scanner);
    }

    private void handleRentEquipment() {
        System.out.println("\n\t\t=== RENT EQUIPMENT ===");

        showAvailableEquipment();

        if (rentalService.getAvailableEquipment().isEmpty()) {
            System.out.println("\nNo equipment available for rent.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.print("\nEnter Equipment ID: ");
        String equipmentId = scanner.nextLine().trim().toUpperCase();

        Equipment equipment = rentalService.findEquipmentById(equipmentId);
        if (equipment == null) {
            System.out.println("\nEquipment not found.");
            pressEnterToContinue(scanner);
            return;
        }

        if (!equipment.isAvailable()) {
            System.out.println("\nEquipment is not available for rent.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.println("Selected: " + equipment.getName() + " - $" + equipment.getRentPerDay() + "/day");

        int days = getIntInput("Number of days to rent: ");
        if (days <= 0) {
            System.out.println("Number of days to rent must be greater than 0.");
            pressEnterToContinue(scanner);
            return;
        }

        double totalCost = equipment.getRentPerDay() * days;
        System.out.printf("Total Cost: $%.2f\n", totalCost);

        if (confirmAction("Confirm rental?")) {
            String result = rentalService.rentEquipment(equipmentId, days);
            System.out.println(result);
        } else {
            System.out.println("Rental cancelled.");
        }

        pressEnterToContinue(scanner);
    }

    private void handleReturnEquipment() {
        System.out.println("\n\t\t\t=== RETURN EQUIPMENT ===");

        List<Rental> activeRentals = rentalService.getActiveRentals();
        if (activeRentals.isEmpty()) {
            System.out.println("\nNo active rentals found.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.println("ACTIVE RENTALS:");
        System.out.printf("%-8s %-12s %-20s %-12s %-8s%n",
                "Rental ID", "Equipment", "Customer", "Start Date", "Days");
        System.out.println("-".repeat(65));

        for (Rental rental : activeRentals) {
            Equipment eq = rentalService.findEquipmentById(rental.getEquipmentId());
            String equipmentName = eq != null ? eq.getName() : "Unknown";

            System.out.printf("%-8s %-12s %-20s %-12s %-8d%n",
                    rental.getRentalId(), equipmentName, rental.getCustomerId(),
                    rental.getStartDate(), rental.getDaysRented());

            if (rental.isOverdue()) {
                System.out.printf("\t\t!!! OVERDUE by %d days !!!%n", rental.getDaysOverdue());
            }
        }

        System.out.print("\nEnter Rental ID to return: ");
        String rentalId = scanner.nextLine().trim().toUpperCase();

        if (confirmAction("Confirm equipment return?")) {
            String result = rentalService.returnEquipment(rentalId);
            System.out.println(result);
        } else {
            System.out.println("Return cancelled.");
        }

        pressEnterToContinue(scanner);
    }

    private void showMyActiveRentals() {
        System.out.println("\n\t\t\t=== MY ACTIVE RENTALS ===");
        List<Rental> myRentals = rentalService.getActiveRentals();

        if (myRentals.isEmpty()) {
            System.out.println("\nYou have no active rentals.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.printf("%-10s %-20s %-12s %-8s %-12s %-8s%n",
                "Rental ID", "Equipment", "Start Date", "Days", "Total Cost", "Status");
        System.out.println("-".repeat(75));

        for (Rental rental : myRentals) {
            Equipment eq = rentalService.findEquipmentById(rental.getEquipmentId());
            String equipmentName = eq != null ? eq.getName() : "Unknown";
            String status = rental.isOverdue() ? "OVERDUE" : "Active";

            System.out.printf("%-10s %-20s %-12s %-8d $%-11.2f %-8s%n",
                    rental.getRentalId(), equipmentName, rental.getStartDate(),
                    rental.getDaysRented(), rental.getTotalCost(), status);

            if (rental.isOverdue()) {
                System.out.printf("\n\t\tExpected return: %s (%d days overdue)%n",
                        rental.getExpectedReturnDate(), rental.getDaysOverdue());
            } else {
                System.out.printf("\n\t\tExpected return: %s%n", rental.getExpectedReturnDate());
            }
        }

        pressEnterToContinue(scanner);
    }

    private void showMyRentalHistory() {
        System.out.println("\n\t\t\t=== MY RENTAL HISTORY ===");
        User currentUser = authService.getCurrentUser();
        List<ReturnRecord> history = rentalService.getCustomerHistory(currentUser.getUserId());

        if (history.isEmpty()) {
            System.out.println("\nNo rental history found.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.printf("%-10s %-20s %-12s %-12s %-12s %-10s%n",
                "Rental ID", "Equipment", "Start Date", "End Date", "Total Cost", "Late Fee");
        System.out.println("-".repeat(80));

        double totalSpent = 0.0;
        for (ReturnRecord record : history) {
            Equipment eq = rentalService.findEquipmentById(record.getEquipmentId());
            String equipmentName = eq != null ? eq.getName() : "Unknown";

            System.out.printf("%-10s %-20s %-12s %-12s $%-11.2f $%-9.2f%n",
                    record.getRentalId(), equipmentName, record.getStartDate(),
                    record.getEndDate(), record.getTotalCost(), record.getLateFee());

            totalSpent += record.getFinalAmount();
        }

        System.out.println("-".repeat(80));
        System.out.printf("%nTotal Rentals: %d | Total Amount Spent: $%.2f%n",
                history.size(), totalSpent);

        pressEnterToContinue(scanner);
    }

    // admin operations
    private void showAllUsers() {
        System.out.println("\n\t\t\t=== ALL USERS ===");
        List<User> users = authService.getAllUsers();

        System.out.printf("%-8s %-12s %-25s %-10s %-12s %-15s%n",
                "User ID", "Username", "Full Name",
                "Type", "Status", "Last Login");
        System.out.println("-".repeat(90));

        for (User user : users) {
            String lastLogin = user.getLastLogin() != null ?
                    user.getLastLogin().format(DATETIME_FORMATTER)
                    : "Never";

            System.out.printf("%-8s %-12s %-25s %-10s %-12s %-12s%n",
                    user.getUserId(), user.getUsername(), user.getFullName(),
                    user.getUserType(), user.getStatus(), lastLogin);
        }

        System.out.println("\nTotal Users: " + users.size());

        pressEnterToContinue(scanner);
    }

    private void handleRegisterCustomer() {
        System.out.println("\n\t\t\t=== REGISTER NEW CUSTOMER ===");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Full Name ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        String validation = ValidationUtils.validateUserRegistration(username, password,
                fullName, email);
        if (validation != null) {
            System.out.println("\nValidation error: " + validation);
            pressEnterToContinue(scanner);
            return;
        }

        if (authService.registerCustomer(username, password, fullName, email)) {
            System.out.println("\nCustomer registered successfully!");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }

        pressEnterToContinue(scanner);
    }

    private void showSystemOverview() {
        System.out.println("\n\t\t\t=== SYSTEM OVERVIEW ===");

        List<Equipment> allEquipment = rentalService.getAllEquipment();
        List<User> allUsers = authService.getAllUsers();
        List<Rental> activeRentals = rentalService.getAllActiveRentals();

        long availableCount = allEquipment.stream().filter(Equipment::isAvailable).count();
        long rentedCount = allEquipment.stream()
                .filter(eq -> "Rented".equalsIgnoreCase(eq.getAvailability()))
                .count();

        long customerCount = allUsers.stream().filter(User::isCustomer).count();
        long adminCount = allUsers.stream().filter(User::isAdmin).count();
        long lockedAccounts = allUsers.stream().filter(u -> u.getStatus().equals(User.AccountStatus.LOCKED)).count();

        long overdueCount = activeRentals.stream().filter(Rental::isOverdue).count();

        System.out.println("\tEQUIPMENT STATUS:");
        System.out.printf("Total Equipment: %d%n", allEquipment.size());
        System.out.printf("Available: %d%n", availableCount);
        System.out.printf("Currently Rented: %d%n", rentedCount);

        System.out.println("\n\tUSER STATISTICS:");
        System.out.printf("Total Users: %d%n", allUsers.size());
        System.out.printf("Customers: %d%n", customerCount);
        System.out.printf("Administrators: %d%n", adminCount);
        System.out.printf("Locked Accounts: %d%n", lockedAccounts);

        System.out.println("\n\tRENTAL STATUS:");
        System.out.printf("Active Rentals: %d%n", activeRentals.size());
        System.out.printf("Overdue Rentals: %d%n", overdueCount);

        if (overdueCount > 0) {
            System.out.println("\n\tATTENTION: There are overdue rentals requiring attention!");
        }

        pressEnterToContinue(scanner);
    }

    // report handlers
    private void handleDailySummary() {
        LocalDate date = getDateInput("\nEnter date (yyyy-mm-dd) or press Enter for today: ",
                "yyyy-MM-dd");
        if (date == null) {
            date = LocalDate.now();
        }

        String report = reportGenerator.generateDailySummary(date);
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    private void handleMonthlyReport() {
        int year = getIntInput("Enter year (e.g., 2025): ");
        int month = getIntInput("Enter month (1-12): ");

        if (month < 1 || month > 12) {
            System.out.println("Invalid month. Please enter 1-12.");
            pressEnterToContinue(scanner);
            return;
        }

        String report = reportGenerator.generateMonthlyReport(year, month);
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    private void showCustomerAnalytics() {
        String report = reportGenerator.generateCustomerAnalyticsReport();
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    private void showEquipmentPerformance() {
        String report = reportGenerator.generateEquipmentPerformanceReport();
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    private void showFinancialSummary() {
        String report = reportGenerator.generateFinancialSummaryReport();
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    private void showSecurityAudit() {
        String report = reportGenerator.generateSecurityAuditReport();
        System.out.println("\n" + report);

        pressEnterToContinue(scanner);
    }

    // utilities
    private void showDefaultAccounts() {
        System.out.println("""
                \n\t\t=== DEFAULT SYSTEM ACCOUNTS ===\
                \n\n\tADMINISTRATOR ACCOUNT:\
                \nUsername: admin\
                \nPassword: admin123\
                \nAccess: Full system management
                """);

        System.out.println("""
                \n\nCUSTOMER ACCOUNTS:\
                \nUsername: john.doe\
                \nPassword: customer123\
                \nAccess: Rental operations only
                """);

        System.out.println("""
                \n\nUsername: jane.doe\
                \nPassword: customer123\
                \nAccess: Rental operations only
                """);

        System.out.println("\nNote: Change default passwords after first login!");

        pressEnterToContinue(scanner);
        clearScreen();
    }

    private void handleLogout() {
        if (confirmAction("Are you sure you want to logout?")) {
            authService.logout();
            pressEnterToContinue(scanner);
            clearScreen();
        }
    }

    private boolean confirmAction(String message) {
        System.out.print(message + "(y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.contains("yes");
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private LocalDate getDateInput(String prompt, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (input.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(input, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            return LocalDate.now();
        }
    }

    private void handleSearchEquipment() {
        System.out.print("\nEnter search term: ");
        String searchTerm = scanner.nextLine().trim();

        List<Equipment> results = rentalService.searchEquipment(searchTerm);
        if (results.isEmpty()) {
            System.out.println("\nNo equipment found matching: " + searchTerm);
            pressEnterToContinue(scanner);
            return;
        }

        System.out.println("\nSEARCH RESULTS:");
        for (Equipment eq : results) {
            System.out.println("-" + eq.toString());
        }

        pressEnterToContinue(scanner);
    }

    private void showEquipmentStats() {
        System.out.println("Equipment usage statistics feature coming soon...");

        pressEnterToContinue(scanner);
    }

    private void handleUnlockAccount() {
        System.out.print("\nEnter username to unlock: ");
        String username = scanner.nextLine().trim();

        if (authService.unlockUserAccount(username)) {
            System.out.println("\nAccount unlocked successfully!");
        } else {
            System.out.println("\nFailed to unlock account. User not found or not locked.");
        }

        pressEnterToContinue(scanner);
    }

    private void showUserActivityReport() {
        System.out.println("\nUser activity report feature coming soon...");

        pressEnterToContinue(scanner);
    }

    private void showAllActiveRentals() {
        System.out.println("\n\t\t\t=== ACTIVE RENTALS ===");
        List<Rental> allRentals = rentalService.getAllActiveRentals();

        if (allRentals.isEmpty()) {
            System.out.println("\nNo active rentals found.");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.printf("%-10s %-12s %-20s %-12s %-8s %-12s %-8s%n",
                "Rental ID", "Equipment", "Customer", "Start Date",
                "Days", "Total Cost", "Status");
        System.out.println("-".repeat(85));

        for (Rental rental : allRentals) {
            Equipment eq = rentalService.findEquipmentById(rental.getEquipmentId());
            String equipmentName = eq != null ? eq.getName() : "Unknown";
            User user = rentalService.findCustomerById(rental.getCustomerId());
            String customerName = user != null ? user.getFullName() : "Unknown Customer";
            String status = rental.isOverdue() ? Constants.RENTAL_STATUS_OVERDUE : Constants.RENTAL_STATUS_ACTIVE;

            System.out.printf("%-10s %-12s %-20s %-12s %-8d $%-11.2f %-8s%n",
                    rental.getRentalId(), equipmentName, customerName,
                    rental.getStartDate(), rental.getDaysRented(),
                    rental.getTotalCost(), status);

            if (rental.isOverdue()) {
                System.out.printf("\n\t\t!!! %d days overdue = Expected return: %s !!!",
                        rental.getDaysOverdue(), rental.getExpectedReturnDate());
            }
        }

        System.out.println("\nTotal Active Rental Days: " + allRentals.size());

        pressEnterToContinue(scanner);
    }

    private void showOverdueRentals() {
        System.out.println("\n\t\t\t=== OVERDUE RENTALS ===");
        List<Rental> overdueRentals = rentalService.getOverdueRentals();

        if (overdueRentals.isEmpty()) {
            System.out.println("No overdue rentals. Great job!");
            pressEnterToContinue(scanner);
            return;
        }

        System.out.printf("%-10s %-12s %-20s %-12s %-8s %-12s%n",
                "Rental ID", "Equipment", "Customer",
                "Expected", "Days Late", "Late Fees");
        System.out.println("-".repeat(80));

        double totalLateFees = 0.0;
        for (Rental rental : overdueRentals) {
            Equipment eq = rentalService.findEquipmentById(rental.getEquipmentId());
            String equipmentName = eq != null ? eq.getName() : "Unknown";
            User user = rentalService.findCustomerById(rental.getCustomerId());
            String customerName = user != null ? user.getFullName() : "Unknown Customer";
            long daysLate = rental.getDaysOverdue();
            double lateFee = daysLate * Constants.LATE_FEE_PER_DAY;
            totalLateFees += lateFee;

            System.out.printf("%-10s %-12s %-20s %-12s %-8d $%-11.2f%n",
                    rental.getRentalId(), equipmentName, customerName,
                    rental.getExpectedReturnDate(), daysLate, totalLateFees);
        }

        System.out.println("-".repeat(80));
        System.out.printf("\nTotal Overdue: %d | Potential Late Fees: $%.2f%n",
                overdueRentals.size(), totalLateFees);

        pressEnterToContinue(scanner);
    }

    private void handleCustomerRentalHistory() {
        System.out.print("\nEnter customer name or ID: ");
        String customerIdentifier = scanner.nextLine().trim();

        if (customerIdentifier.isBlank()) {
            System.out.println("\nCustomer identifier cannot be blank.");
            pressEnterToContinue(scanner);
            return;
        }

        List<Rental> activeRentals = rentalService.getCustomerRentals(customerIdentifier);
        List<ReturnRecord> history = rentalService.getCustomerHistory(customerIdentifier);

        if (activeRentals.isEmpty() &&  history.isEmpty()) {
            System.out.println("\nNo rental records found for: " + customerIdentifier);
            pressEnterToContinue(scanner);
            return;
        }

        System.out.println("\n\t\t\t=== CUSTOMER RENTAL HISTORY ===");
        System.out.println("Customer: " + customerIdentifier);

        if (!activeRentals.isEmpty()) {
            System.out.println("\nACTIVE RENTALS:");
            for (Rental rental : activeRentals) {
                System.out.println("-" + rental.toString());
                if (rental.isOverdue()) {
                    System.out.println("\t!!! OVERDUE by " + rental.getDaysOverdue() + " days !!!");
                }
            }
        }

        if (!history.isEmpty()) {
            System.out.println("\nRETURN HISTORY:");
            double totalSpent = 0.0;
            for (ReturnRecord record : history) {
                System.out.println("-" + record.toString());
                totalSpent += record.getFinalAmount();
            }
            System.out.printf("Total Amount Spent: $%.2fn", totalSpent);
        }

        pressEnterToContinue(scanner);
    }

    private void showUserProfile() {
        User currentUser = authService.getCurrentUser();
        System.out.println("\n\t\t=== USER PROFILE ===" +
                "\nUser ID: " + currentUser.getUserId() +
                "\nUsername: " + currentUser.getUsername() +
                "\nFull Name: " + currentUser.getFullName() +
                "\nEmail: " + currentUser.getEmail() +
                "\nUser Type: " + currentUser.getUserType() +
                "\nAccount Status: " + currentUser.getStatus() +
                "\nAccount Created: " + currentUser.getCreatedDate().format(DATETIME_FORMATTER));

        if (currentUser.getLastLogin() != null) {
            System.out.println("Last Login: " + currentUser.getLastLogin().format(DATETIME_FORMATTER));
        }

        if (currentUser.isCustomer()) {
            System.out.println("\n" + rentalService.getCurrentUserRentalSummary());
        }

        pressEnterToContinue(scanner);
    }

    private void clearScreen() {
        System.out.println("\n".repeat(30));
    }

    private void pressEnterToContinue(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}