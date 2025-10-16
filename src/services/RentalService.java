package services;

import data.FileHandler;
import models.*;
import utils.Constants;
import utils.ValidationUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RentalService {
    private List<Equipment> equipment;
    private List<Rental> rentals;
    private List<ReturnRecord> returns;
    private int rentalCounter;
    private final AuthenticationService authService;

    public RentalService(AuthenticationService authService) {
        this.authService = authService;
        loadData();
        initializeRentalCounter();
    }

    private void loadData() {
        equipment = FileHandler.loadEquipment();
        rentals = FileHandler.loadRentals();
        returns = FileHandler.loadReturns();
    }

    private void initializeRentalCounter() {
        rentalCounter = 1;
        for (Rental rental : rentals) {
            String id = rental.getRentalId();
            if (id.startsWith("R")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num >= rentalCounter) {
                        rentalCounter = num + 1;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error initializing rental counter: " + e.getCause());
                }
            }
        }

        for (ReturnRecord record : returns) {
            String id = record.getRentalId();
            if (id.startsWith("R")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num >= rentalCounter) {
                        rentalCounter = num + 1;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error initializing rental counter (record): " + e.getCause());
                }
            }
        }
    }

    private boolean requireAdminAccess() {
        if (!authService.isCurrentUserAdmin()) {
            System.err.println("Access denied: Admin privileges required");
            return false;
        }
        return true;
    }

    private boolean requireAuthentication() {
        if (!authService.isLoggedIn()) {
            System.err.println("Access denied. Please log in first.");
            return false;
        }
        return true;
    }

    public boolean addEquipment(String name, double rentPerDay, String category) {
        if (!requireAdminAccess()) return false;

        String id = generateEquipmentId();
        Equipment eq= new Equipment(id, name, rentPerDay, "Available", category);
        equipment.add(eq);
        FileHandler.saveEquipment(equipment);
        return true;
    }

    private synchronized String generateEquipmentId() {
        int maxId = 100;
        for (Equipment eq : equipment) {
            if ((eq.getId().startsWith("E"))) {
                try {
                    int id = Integer.parseInt(eq.getId().substring(1));
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                    System.err.println("Error generating equipment id: " + e.getCause());
                }
            }
        }
        return String.format("E%03d", maxId + 1);
    }

    public List<Equipment> getAvailableEquipment() {
        return equipment.stream()
                .filter(Equipment::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Equipment> getAllEquipment() {
        return new ArrayList<>(equipment);
    }

    public List<ReturnRecord> getAllReturns() {
        if (!requireAdminAccess()) return new ArrayList<>();
        return new ArrayList<>(returns);
    }

    public Equipment findEquipmentById(String id) {
        return equipment.stream()
                .filter(eq -> eq.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean updateEquipmentStatus(String equipmentId, String newStatus) {
        if (!requireAdminAccess()) return false;

        Equipment eq = findEquipmentById(equipmentId);
        if (eq == null) return false;

        eq.setAvailability(newStatus);
        FileHandler.saveEquipment(equipment);
        return true;
    }

    public boolean removeEquipment(String equipmentId) {
        if (!requireAdminAccess()) return false;

        Equipment eq = findEquipmentById(equipmentId);
        if (eq == null) return false;

        boolean isRented = rentals.stream()
                        .anyMatch(r -> r.getEquipmentId().equals(equipmentId));

        if (isRented) {
            System.err.println("Cannot remove equipment that is currently rented.");
            return false;
        }

        equipment.remove(eq);
        FileHandler.saveEquipment(equipment);
        return true;
    }

    public List<Rental> getRentalsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (!requireAdminAccess()) return new ArrayList<>();

        return rentals.stream()
                .filter(r -> !r.getStartDate().isBefore(startDate) &&
                                    !r.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public List<ReturnRecord> getReturnsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (!requireAdminAccess()) return new ArrayList<>();

        return returns.stream()
                .filter(r -> !r.getEndDate().isBefore(startDate) &&
                                         !r.getEndDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public List<Equipment> getEquipmentByCategory(String category) {
        List<Equipment> categoryEquipment = equipment.stream()
                .filter(eq -> eq.getCategory().equalsIgnoreCase(category))
                .toList();

        if (authService.isCurrentUserCustomer()) {
            categoryEquipment = categoryEquipment.stream()
                    .filter(Equipment::isAvailable)
                    .toList();
        }

        return categoryEquipment;
    }

    public List<String> getAllCategories() {
        return equipment.stream()
                .map(Equipment::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public double calculatePotentialLateFees(String customerId) {
        if (!requireAuthentication()) return 0.0;

        User currentUser = authService.getCurrentUser();

        if (currentUser.isCustomer() && !currentUser.getUserId().equals(customerId)) {
            return 0.0;
        }

        return rentals.stream()
                .filter(r -> r.getCustomerId().equals(customerId))
                .filter(Rental::isOverdue)
                .mapToDouble(r -> r.getDaysOverdue() * Constants.LATE_FEE_PER_DAY)
                .sum();
    }

    public List<Equipment> searchEquipment(String searchTerm) {
        if (!requireAuthentication()) return new ArrayList<>();

        String term = searchTerm.toLowerCase();
        return equipment.stream()
                .filter(eq -> eq.getName().toLowerCase().contains(term) ||
                                        eq.getId().toLowerCase().contains(term) ||
                                        eq.getCategory().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }
    public List<Equipment> getEquipmentSortedByPrice(boolean ascending) {
        return equipment.stream()
                .sorted(ascending ?
                        Comparator.comparingDouble(Equipment::getRentPerDay) :
                        Comparator.comparingDouble(Equipment::getRentPerDay).reversed())
                .collect(Collectors.toList());
    }

    public String rentEquipment(String equipmentId, int days) {
        if (!requireAuthentication()) return "Please log in first.";

        User currentUser = authService.getCurrentUser();
        if (!currentUser.isCustomer()) {
            return "Only customers can rent equipment.";
        }

        Equipment eq = findEquipmentById(equipmentId);
        if (eq == null) {
            return "Equipment not found.";
        }
        if (!eq.isAvailable()) {
            return "Equipment is not available for rent.";
        }
        if (!ValidationUtils.isValidRentDays(days)) {
            return "Invalid rental days: " + days;
        }

        double totalCost = eq.getRentPerDay() * days;
        String rentalId = "R" + String.format("%03d", rentalCounter++);

        Rental rental = new Rental(rentalId, equipmentId, currentUser.getUserId(), LocalDate.now(), days, totalCost);
        rentals.add(rental);

        eq.setAvailability("Rented");

        FileHandler.saveRentals(rentals);
        FileHandler.saveEquipment(equipment);

        return "Equipment rented successfully. Rental ID: " + rentalId + ", Total Cost: $" + String.format("%.2f", totalCost);
    }

    public String returnEquipment(String rentalId) {
        if (!requireAuthentication()) return "Please log in first.";

        Rental rental = findRentalById(rentalId);
        if (rental == null) {
            return "Rental not found.";
        }

        User currentUser = authService.getCurrentUser();

        if (currentUser.isCustomer() && !rental.getCustomerId().equals(currentUser.getUserId())) {
            return "You can only return your own rentals.";
        }

        Equipment eq = findEquipmentById(rental.getEquipmentId());
        if (eq == null) {
            return "Equipment not found.";
        }

        LocalDate returnDate = LocalDate.now();
        double lateFee = 0.0;

        if (rental.isOverdue()) {
            long daysLate = rental.getDaysOverdue();
            lateFee = daysLate * Constants.LATE_FEE_PER_DAY;
        }

        ReturnRecord record = new ReturnRecord(
                rental.getRentalId(),
                rental.getEquipmentId(),
                rental.getCustomerId(),
                rental.getStartDate(),
                returnDate,
                rental.getTotalCost()
        );
        record.setLateFee(lateFee);

        returns.add(record);
        rentals.remove(rental);
        eq.setAvailability("Available");

        FileHandler.saveReturns(returns);
        FileHandler.saveRentals(rentals);
        FileHandler.saveEquipment(equipment);

        String result =  "Equipment returned successfully.";
        if (lateFee > 0) {
            result += " Late fee: $" + String.format("%.2f", lateFee);
        }
        result += " Total amount: $" + String.format("%.2f", rental.getTotalCost() + lateFee);

        return result;
    }

    public String forceReturnEquipment(String rentalId, String condition, double additionalFees) {
        if (!requireAdminAccess()) return "Access denied";

        Rental rental = findRentalById(rentalId);
        if (rental == null) {
            return "Rental not found.";
        }

        Equipment eq = findEquipmentById(rental.getEquipmentId());
        if (eq == null) {
            return "Equipment not found.";
        }

        LocalDate returnDate = LocalDate.now();

        double lateFee = 0.0;

        if (rental .isOverdue()) {
            long daysLate = rental.getDaysOverdue();
            lateFee = daysLate * Constants.LATE_FEE_PER_DAY;
        }

        ReturnRecord record = new ReturnRecord(
                rental.getRentalId(),
                rental.getEquipmentId(),
                rental.getCustomerId(),
                rental.getStartDate(),
                returnDate,
                rental.getTotalCost()
        );
        record.setLateFee(lateFee);
        record.setCondition(condition);

        returns.add(record);
        rentals.remove(rental);

        if ("Damage".equalsIgnoreCase(condition) || "Lost".equalsIgnoreCase(condition)) {
            eq.setAvailability("Maintenance");
        } else {
            eq.setAvailability("Available");
        }

        FileHandler.saveEquipment(equipment);
        FileHandler.saveRentals(rentals);
        FileHandler.saveReturns(returns);

        String result = "Equipment force-returned. Condition: " + condition;
        if (lateFee > 0 || additionalFees > 0) {
            result += "\n Total fees $" + String.format("%.2f", lateFee + additionalFees);
        }

        return result;
    }

    public User findCustomerById(String customerId) {
        return authService.getAllUsers().stream()
                .filter(user -> user.isCustomer() &&
                        user.getUserId().equals(customerId))
                .findFirst()
                .orElse(null);

    }

    public Rental findRentalById(String rentalId) {
        return rentals.stream()
                .filter(r -> r.getRentalId().equals(rentalId))
                .findFirst()
                .orElse(null);
    }

    public List<Rental> getActiveRentals() {
        if (!requireAuthentication()) return new ArrayList<>();

        User currentUser = authService.getCurrentUser();

        if (currentUser.isAdmin()) {
            return new ArrayList<>(rentals);
        } else {
            return rentals.stream()
                    .filter(r -> r.getCustomerId().equals(currentUser.getUserId()))
                    .collect(Collectors.toList());
        }
    }

    public List<Rental> getAllActiveRentals() {
        if (!requireAdminAccess()) return new ArrayList<>();
        return new ArrayList<>(rentals);
    }

    public List<Rental> getDailyRentals(LocalDate date) {
        if (!requireAdminAccess()) return new ArrayList<>();

        return rentals.stream()
                .filter(r -> r.getStartDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Rental> getCustomerRentals(String customerId) {
        if (!requireAuthentication()) return new ArrayList<>();

        User currentUser = authService.getCurrentUser();

        if (currentUser.isCustomer()) {
            customerId = currentUser.getUserId();
        }

        String finalCustomerId = customerId;
        return rentals.stream()
                .filter(r -> r.getCustomerId().equals(finalCustomerId))
                .collect(Collectors.toList());
    }

    public List<ReturnRecord> getCustomerHistory(String customerId) {
        if (!requireAuthentication()) return new ArrayList<>();

        User currentUser = authService.getCurrentUser();

        if (currentUser.isCustomer()) {
            customerId = currentUser.getUserId();
        }

        String finalCustomerId = customerId;
        return returns.stream()
                .filter(r -> r.getCustomerId().equals(finalCustomerId))
                .collect(Collectors.toList());
    }

    public List<Rental> getOverdueRentals() {
        if (!requireAdminAccess()) return new ArrayList<>();

        return rentals.stream()
                .filter(Rental::isOverdue)
                .collect(Collectors.toList());
    }

    public double getTotalRevenue() {
        return returns.stream()
                .mapToDouble(ReturnRecord::getFinalAmount)
                .sum();
    }

    public double getDailyRevenue(LocalDate date) {
        if (!requireAdminAccess()) return 0.0;

        return returns.stream()
                .filter(r -> r.getEndDate().equals(date))
                .mapToDouble(ReturnRecord::getFinalAmount)
                .sum();
    }

    public Map<String, Integer> getEquipmentUsageStats() {
        if (!requireAdminAccess()) return new HashMap<>();

        Map<String, Integer> stats = new HashMap<>();
        for (ReturnRecord record : returns) {
            Equipment eq = findEquipmentById(record.getEquipmentId());
            if (eq != null) {
                stats.put(eq.getName(), stats.getOrDefault(eq.getName(), 0) + 1);
            }
        }
        return stats;
    }

    public Map<String, Object> getEquipmentStatistics(String equipmentId) {
        if (!requireAdminAccess()) return new HashMap<>();

        Equipment eq = findEquipmentById(equipmentId);
        if (eq == null) return new HashMap<>();

        Map<String, Object> stats = new HashMap<>();

        long totalRentals = returns.stream()
                .filter(r -> r.getEquipmentId().equals(equipmentId))
                .count();

        double totalRevenue = returns.stream()
                .filter(r -> r.getEquipmentId().equals(equipmentId))
                .mapToDouble(ReturnRecord::getFinalAmount)
                .sum();

        long totalDaysRented = returns.stream()
                .filter(r -> r.getEquipmentId().equals(equipmentId))
                .mapToLong(r -> r.getEndDate().toEpochDay() - r.getStartDate().toEpochDay())
                .sum();

        boolean currentlyRented = rentals.stream()
                .anyMatch(r -> r.getEquipmentId().equals(equipmentId));

        stats.put("equipmentName", eq.getName());
        stats.put("totalRentals", totalRentals);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalDaysRented", totalDaysRented);
        stats.put("currentlyRented", currentlyRented);
        stats.put("averageRevenuePerRental", totalRentals > 0 ? totalRevenue / totalRentals : 0.0);

        return stats;
    }

    public String getCurrentUserRentalSummary() {
        if (!requireAuthentication()) return "Please log in first.";

        User currentUser = authService.getCurrentUser();
        List<Rental> userRentals = getCustomerRentals(currentUser.getUserId());
        List<ReturnRecord> userHistory = getCustomerHistory(currentUser.getUserId());

        StringBuilder summary = new StringBuilder();
        summary.append("=== RENTAL SUMMARY FOR ").append(currentUser.getFullName().toUpperCase()).append(" ===\n");
        summary.append("Active Rentals: ").append(userRentals.size()).append("\n");
        summary.append("Rental History: ").append(userHistory.size()).append("\n");

        double totalSpent = userHistory.stream()
                .mapToDouble(ReturnRecord::getFinalAmount)
                .sum();
        summary.append("Total Amount Spent: $").append(String.format("%.2f", totalSpent)).append("\n");

        long overdueCount = userRentals.stream()
                .mapToLong(r -> r.isOverdue() ? 1 : 0)
                .sum();
        if (overdueCount > 0) {
            summary.append("OVERDUE RENTALS: ").append(overdueCount).append("\n");
        }

        return summary.toString();
    }

    // for debugging
    public void refreshData() {
        if (!requireAdminAccess()) return;

        loadData();
        System.out.println("Data refreshed from files.");
    }

    public Map<String, Object> getSystemStatus() {
        if (!requireAdminAccess()) return new HashMap<>();

        Map<String, Object> status = new HashMap<>();

        status.put("totalEquipment", equipment.size());
        status.put("availableEquipment", equipment.stream().filter(Equipment::isAvailable).count());
        status.put("activeRentals", rentals.size());
        status.put("overdueRentals", rentals.stream().filter(Rental::isOverdue).count());
        status.put("totalReturns", returns.size());
        status.put("totalUsers", authService.getAllUsers());

        double totalRevenue = returns.stream()
                .mapToDouble(ReturnRecord::getFinalAmount)
                .sum();
        status.put("totalRevenue", totalRevenue);

        return status;
    }

}
