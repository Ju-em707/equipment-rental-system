package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReturnRecord extends RentalTransaction {
    private LocalDate endDate;
    private double lateFee;
    private String condition;

    public ReturnRecord(String rentalId, String equipmentId, String customerId,
                        LocalDate startDate, LocalDate endDate, double totalCost) {
        super(rentalId, equipmentId, customerId, startDate, totalCost);
        this.endDate = endDate;
        this.lateFee = 0.0;
        this.condition = "Good";
    }

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

    @Override
    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s,%s,%s,%s,%s,%.2f,%.2f,%s",
                rentalId, equipmentId, customerId, startDate.format(formatter),
                endDate.format(formatter), totalCost, lateFee, condition);
    }

}
