package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Rental{
    private String rentalId;
    private String equipmentId;
    private String customerId;
    private LocalDate startDate;
    private int daysRented;
    private String status;
    protected double totalCost;

    public Rental(String rentalId, String equipmentId, String customerId,
                  LocalDate startDate, int daysRented, double totalCost) {
        this.rentalId = rentalId;
        this.equipmentId = equipmentId;
        this.customerId = customerId;
        this.startDate = startDate;
        this.daysRented = daysRented;
        this.status = "Active";
        this.totalCost = totalCost;
    }

    public String getRentalId() { return rentalId; }
    public void setRentalId(String rentalId) { this.rentalId = rentalId; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public int getDaysRented() { return daysRented; }
    public void setDaysRented(int daysRented) { this.daysRented = daysRented; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getExpectedReturnDate() {
        return startDate.plusDays(daysRented);
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(getExpectedReturnDate());
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return LocalDate.now().toEpochDay() - getExpectedReturnDate().toEpochDay();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("Rental ID: %s | Equipment: %s | Customer: %s | Start: %s | Days: %d | Cost: $%.2f | Status: %s",
                rentalId, equipmentId, customerId, startDate.format(formatter), daysRented, totalCost, status);
    }

    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s,%s,%s,%s,%d,%.2f", rentalId, equipmentId, customerId, startDate.format(formatter), daysRented, totalCost);
    }
}
