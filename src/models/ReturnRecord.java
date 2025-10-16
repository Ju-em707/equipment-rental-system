package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReturnRecord {
    private String rentalId;
    private String equipmentId;
    private String customerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double lateFee;
    private String condition;
    private double totalCost;

    public ReturnRecord(String rentalId, String equipmentId, String customerId,
                        LocalDate startDate, LocalDate endDate, double totalCost) {
        this.rentalId = rentalId;
        this.equipmentId = equipmentId;
        this.customerId = customerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lateFee = 0.0;
        this.condition = "Good";
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

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getFinalAmount() {
        return totalCost + lateFee;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("Return ID: %s | Equipment: %s | Customer: %s | Start: %s | End: %s | Total: $%.2f | Late Fee: $%.2f | Condition: %s",
                rentalId, equipmentId, customerId, startDate.format(formatter),
                endDate.format(formatter), totalCost, lateFee, condition);
    }

    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s,%s,%s,%s,%s,%.2f,%.2f,%s",
                rentalId, equipmentId, customerId, startDate.format(formatter),
                endDate.format(formatter), totalCost, lateFee, condition);
    }

}
