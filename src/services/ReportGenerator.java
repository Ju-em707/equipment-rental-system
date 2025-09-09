package services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import models.*;


public class ReportGenerator {
    private final RentalService rentalService;
    private final AuthenticationService authService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportGenerator(RentalService rentalService, AuthenticationService authService) {
        this.rentalService = rentalService;
        this.authService = authService;
    }

    private boolean requireAuthentication() {
        if (!authService.isLoggedIn()) {
            System.err.println("Access denied: Please log in to generate reports.");
            return false;
        }
        return true;
    }

    private boolean requireAdminAccess() {
        if (!authService.isCurrentUserAdmin()) {
            System.err.println("Access denied: Admin privileges required for this report.");
            return false;
        }
        return true;
    }

    public String generateDailySummary(LocalDate date) {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        report.append("\n\t=== DAILY SUMMARY FOR ").append(date.format(DATE_FORMATTER)).append(" ===\n\n");

        List<Rental> dailyRentals = rentalService.getDailyRentals(date);
        double dailyRevenue = rentalService.getDailyRevenue(date);

        report.append("Total Rentals: ").append(dailyRentals.size()).append("\n");
        report.append("Daily Revenue: $").append(String.format("%.2f", dailyRevenue)).append("\n\n");

        if (!dailyRentals.isEmpty()) {
            report.append("RENTALS:\n");
            for (Rental rental : dailyRentals) {
                report.append("- ").append(rental.toString()).append("\n");
            }
        }

        List<Rental> overdueRentals = rentalService.getOverdueRentals();
        if (!overdueRentals.isEmpty()) {
            report.append("\nOVERDUE RENTALS: ").append(overdueRentals.size()).append("\n");
            for (Rental rental : overdueRentals) {
                report.append("- ").append(rental.toString())
                        .append(" (").append(rental.getDaysOverdue()).append(" days late)\n");
            }
        }

        return report.toString();
    }

    public String generateUserRentalSummary() {
        if (!requireAuthentication()) return "Access denied.";

        User currentUser = authService.getCurrentUser();
        StringBuilder report = new StringBuilder();

        report.append("\n\t=== RENTAL SUMMARY FOR ").append(currentUser.getFullName().toUpperCase()).append(" ===\n\n");

        List<Rental> activeRentals = rentalService.getCustomerRentals(currentUser.getUserId());
        List<ReturnRecord> history = rentalService.getCustomerHistory(currentUser.getUserId());

        report.append("Active Rentals: ").append(activeRentals.size()).append("\n");
        report.append("Rental History: ").append(history.size()).append("\n");

        double totalSpent = history.stream().mapToDouble(ReturnRecord::getFinalAmount).sum();
        report.append("Total Amount Spent: $").append(String.format("%.2f", totalSpent)).append("\n\n");

        // Active rentals details
        if (!activeRentals.isEmpty()) {
            report.append("ACTIVE RENTALS:\n");
            for (Rental rental : activeRentals) {
                report.append("- ").append(rental.toString());
                if (rental.isOverdue()) {
                    report.append(" *** OVERDUE by ").append(rental.getDaysOverdue()).append(" days ***");
                }
                report.append("\n");
            }
            report.append("\n");
        }

        // Recent history (last 5)
        if (!history.isEmpty()) {
            report.append("RECENT RENTAL HISTORY:\n");
            history.stream()
                    .sorted(Comparator.comparing(ReturnRecord::getEndDate).reversed())
                    .limit(5)
                    .forEach(record -> report.append("- ").append(record).append("\n"));
        }

        return report.toString();
    }

    public String generateMonthlyReport(int year, int month) {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        report.append("\n\t=== MONTHLY REPORT FOR ")
                .append(startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .append(" ===\n\n");

        // Get all returns in the month
        List<ReturnRecord> monthlyReturns = rentalService.getAllReturns().stream()
                .filter(r -> !r.getEndDate().isBefore(startDate) && !r.getEndDate().isAfter(endDate))
                .toList();

        // Revenue calculations
        double totalRevenue = monthlyReturns.stream().mapToDouble(ReturnRecord::getTotalCost).sum();
        double totalLateFees = monthlyReturns.stream().mapToDouble(ReturnRecord::getLateFee).sum();
        double finalRevenue = totalRevenue + totalLateFees;

        report.append("Total Rentals Completed: ").append(monthlyReturns.size()).append("\n");
        report.append("Base Revenue: $").append(String.format("%.2f", totalRevenue)).append("\n");
        report.append("Late Fees: $").append(String.format("%.2f", totalLateFees)).append("\n");
        report.append("Total Revenue: $").append(String.format("%.2f", finalRevenue)).append("\n\n");

        // Equipment usage statistics
        Map<String, Integer> equipmentUsage = new HashMap<>();
        Map<String, Double> equipmentRevenue = new HashMap<>();

        for (ReturnRecord record : monthlyReturns) {
            Equipment eq = rentalService.findEquipmentById(record.getEquipmentId());
            if (eq != null) {
                String name = eq.getName();
                equipmentUsage.put(name, equipmentUsage.getOrDefault(name, 0) + 1);
                equipmentRevenue.put(name, equipmentRevenue.getOrDefault(name, 0.0) + record.getFinalAmount());
            }
        }

        if (!equipmentUsage.isEmpty()) {
            report.append("EQUIPMENT USAGE STATISTICS:\n");
            equipmentUsage.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        String name = entry.getKey();
                        int count = entry.getValue();
                        double revenue = equipmentRevenue.getOrDefault(name, 0.0);
                        report.append(String.format("- %s: %d rentals, $%.2f revenue\n", name, count, revenue));
                    });
        }

        return report.toString();
    }

    public String generateCustomerAnalyticsReport() {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        report.append("\n\t=== CUSTOMER ANALYTICS REPORT ===\n\n");

        List<User> allUsers = authService.getAllUsers();
        List<User> customers = allUsers.stream()
                .filter(User::isCustomer)
                .toList();

        Map<String, Integer> customerRentalCount = new HashMap<>();
        Map<String, Double> customerSpending = new HashMap<>();
        Map<String, Integer> customerOverdueCount = new HashMap<>();

        // Analyze rental history
        List<ReturnRecord> allReturns = rentalService.getAllReturns();
        for (ReturnRecord record : allReturns) {
            String customerId = record.getCustomerId();
            customerRentalCount.put(customerId, customerRentalCount.getOrDefault(customerId, 0) + 1);
            customerSpending.put(customerId, customerSpending.getOrDefault(customerId, 0.0) + record.getFinalAmount());
        }

        // Analyze current overdue rentals
        List<Rental> overdueRentals = rentalService.getOverdueRentals();
        for (Rental rental : overdueRentals) {
            String customerId = rental.getCustomerId();
            customerOverdueCount.put(customerId, customerOverdueCount.getOrDefault(customerId, 0) + 1);
        }

        report.append("CUSTOMER STATISTICS:\n");
        report.append("Total Customers: ").append(customers.size()).append("\n");
        report.append("Active Customers: ").append(customerRentalCount.size()).append("\n\n");

        report.append("TOP CUSTOMERS BY SPENDING:\n");
        customerSpending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String customerId = entry.getKey();
                    User customer = allUsers.stream()
                            .filter(u -> u.getUserId().equals(customerId))
                            .findFirst().orElse(null);
                    String name = customer != null ? customer.getFullName() : "Unknown";
                    int rentalCount = customerRentalCount.getOrDefault(customerId, 0);
                    report.append(String.format("- %s (%s): %d rentals, $%.2f total\n",
                            name, customerId, rentalCount, entry.getValue()));
                });

        if (!customerOverdueCount.isEmpty()) {
            report.append("\nCUSTOMERS WITH OVERDUE RENTALS:\n");
            customerOverdueCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        String customerId = entry.getKey();
                        User customer = allUsers.stream()
                                .filter(u -> u.getUserId().equals(customerId))
                                .findFirst().orElse(null);
                        String name = customer != null ? customer.getFullName() : "Unknown";
                        report.append(String.format("- %s (%s): %d overdue rentals\n",
                                name, customerId, entry.getValue()));
                    });
        }

        return report.toString();
    }

    public String generateEquipmentPerformanceReport() {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        report.append("\n\t=== EQUIPMENT PERFORMANCE REPORT ===\n\n");

        List<Equipment> allEquipment = rentalService.getAllEquipment();
        List<ReturnRecord> allReturns = rentalService.getAllReturns();

        Map<String, Integer> equipmentRentals = new HashMap<>();
        Map<String, Double> equipmentRevenue = new HashMap<>();
        Map<String, Double> equipmentUtilization = new HashMap<>();

        // Calculate statistics
        for (ReturnRecord record : allReturns) {
            String equipmentId = record.getEquipmentId();
            Equipment equipment = rentalService.findEquipmentById(equipmentId);

            if (equipment != null) {
                String name = equipment.getName();
                equipmentRentals.put(name, equipmentRentals.getOrDefault(name, 0) + 1);
                equipmentRevenue.put(name, equipmentRevenue.getOrDefault(name, 0.0) + record.getFinalAmount());

                // Calculate utilization (days rented / total days since first rental)
                long daysRented = record.getEndDate().toEpochDay() - record.getStartDate().toEpochDay();
                equipmentUtilization.put(name, equipmentUtilization.getOrDefault(name, 0.0) + daysRented);
            }
        }

        report.append("EQUIPMENT PERFORMANCE:\n");
        report.append("Total Equipment Items: ").append(allEquipment.size()).append("\n");
        report.append("Equipment with Rental History: ").append(equipmentRentals.size()).append("\n\n");

        report.append("TOP PERFORMING EQUIPMENT:\n");
        equipmentRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String name = entry.getKey();
                    int rentals = equipmentRentals.getOrDefault(name, 0);
                    double utilization = equipmentUtilization.getOrDefault(name, 0.0);
                    report.append(String.format("- %s: %d rentals, $%.2f revenue, %.1f total days rented\n",
                            name, rentals, entry.getValue(), utilization));
                });

        // Equipment never rented
        List<String> neverRented = allEquipment.stream()
                .map(Equipment::getName)
                .filter(name -> !equipmentRentals.containsKey(name))
                .toList();

        if (!neverRented.isEmpty()) {
            report.append("\nEQUIPMENT NEVER RENTED:\n");
            neverRented.forEach(name -> report.append("- ").append(name).append("\n"));
        }

        return report.toString();
    }

    public String generateSecurityAuditReport() {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        report.append("\n\t=== SECURITY AUDIT REPORT ===\n\n");

        List<User> allUsers = authService.getAllUsers();

        // Account status summary
        long activeAccounts = allUsers.stream().filter(u -> u.getStatus() == User.AccountStatus.ACTIVE).count();
        long lockedAccounts = allUsers.stream().filter(u -> u.getStatus() == User.AccountStatus.LOCKED).count();
        long inactiveAccounts = allUsers.stream().filter(u -> u.getStatus() == User.AccountStatus.INACTIVE).count();

        report.append("ACCOUNT STATUS SUMMARY:\n");
        report.append("Total Users: ").append(allUsers.size()).append("\n");
        report.append("Active Accounts: ").append(activeAccounts).append("\n");
        report.append("Locked Accounts: ").append(lockedAccounts).append("\n");
        report.append("Inactive Accounts: ").append(inactiveAccounts).append("\n\n");

        // User type distribution
        long adminCount = allUsers.stream().filter(User::isAdmin).count();
        long customerCount = allUsers.stream().filter(User::isCustomer).count();

        report.append("USER TYPE DISTRIBUTION:\n");
        report.append("Administrators: ").append(adminCount).append("\n");
        report.append("Customers: ").append(customerCount).append("\n\n");

        // Security alerts
        report.append("SECURITY ALERTS:\n");

        List<User> lockedUsers = allUsers.stream()
                .filter(u -> u.getStatus() == User.AccountStatus.LOCKED)
                .toList();

        if (!lockedUsers.isEmpty()) {
            report.append("LOCKED ACCOUNTS:\n");
            for (User user : lockedUsers) {
                report.append(String.format("- %s (%s): %d failed attempts\n",
                        user.getFullName(), user.getUsername(), user.getFailedLoginAttempts()));
            }
        } else {
            report.append("- No locked accounts\n");
        }

        List<User> neverLoggedIn = allUsers.stream()
                .filter(u -> u.getLastLogin() == null)
                .toList();

        if (!neverLoggedIn.isEmpty()) {
            report.append("\nACCOUNTS NEVER LOGGED IN:\n");
            for (User user : neverLoggedIn) {
                report.append(String.format("- %s (%s): Created %s\n",
                        user.getFullName(), user.getUsername(),
                        user.getCreatedDate().format(DATETIME_FORMATTER)));
            }
        }

        return report.toString();
    }

    public String generateFinancialSummaryReport() {
        if (!requireAdminAccess()) return "Access denied.";

        StringBuilder report = new StringBuilder();
        report.append("\n\t=== FINANCIAL SUMMARY REPORT ===\n\n");

        List<ReturnRecord> allReturns = rentalService.getAllReturns();

        if (allReturns.isEmpty()) {
            report.append("No financial data available.\n");
            return report.toString();
        }

        // Calculate totals
        double totalBaseRevenue = allReturns.stream().mapToDouble(ReturnRecord::getTotalCost).sum();
        double totalLateFees = allReturns.stream().mapToDouble(ReturnRecord::getLateFee).sum();
        double totalRevenue = totalBaseRevenue + totalLateFees;

        // Calculate averages
        double averageRentalValue = totalBaseRevenue / allReturns.size();
        double averageLateFee = totalLateFees / allReturns.size();

        // Late fee statistics
        long rentalsWithLateFee = allReturns.stream().filter(r -> r.getLateFee() > 0).count();
        double lateFeePercentage = (double) rentalsWithLateFee / allReturns.size() * 100;

        report.append("REVENUE SUMMARY:\n");
        report.append(String.format("Total Rentals Processed: %d\n", allReturns.size()));
        report.append(String.format("Base Revenue: $%.2f\n", totalBaseRevenue));
        report.append(String.format("Late Fees: $%.2f\n", totalLateFees));
        report.append(String.format("Total Revenue: $%.2f\n\n", totalRevenue));

        report.append("AVERAGES:\n");
        report.append(String.format("Average Rental Value: $%.2f\n", averageRentalValue));
        report.append(String.format("Average Late Fee: $%.2f\n\n", averageLateFee));

        report.append("LATE FEE ANALYSIS:\n");
        report.append(String.format("Rentals with Late Fees: %d (%.1f%%)\n", rentalsWithLateFee, lateFeePercentage));

        if (rentalsWithLateFee > 0) {
            double averageLateFeeForLateRentals = totalLateFees / rentalsWithLateFee;
            report.append(String.format("Average Late Fee (for late rentals): $%.2f\n", averageLateFeeForLateRentals));
        }

        // Monthly breakdown for current year
        int currentYear = LocalDate.now().getYear();
        report.append(String.format("\nMONTHLY BREAKDOWN FOR %d:\n", currentYear));

        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            List<ReturnRecord> monthlyReturns = allReturns.stream()
                    .filter(r -> r.getEndDate().getYear() == currentYear &&
                            r.getEndDate().getMonthValue() == currentMonth)
                    .toList();

            if (!monthlyReturns.isEmpty()) {
                double monthlyRevenue = monthlyReturns.stream()
                        .mapToDouble(ReturnRecord::getFinalAmount)
                        .sum();

                report.append(String.format("- %s: %d rentals, $%.2f revenue\n",
                        LocalDate.of(currentYear, month, 1).format(DateTimeFormatter.ofPattern("MMMM")),
                        monthlyReturns.size(), monthlyRevenue));
            }
        }

        return report.toString();
    }
}
