package models;

import java.time.LocalDate;

public abstract class RentalTransaction {
    protected String rentalId;
    protected String equipmentId;
    protected String customerId;
    protected LocalDate startDate;
    protected double totalCost;

    public RentalTransaction(String rentalId, String equipmentId, String customerId,
                             LocalDate startDate, double totalCost) {
        this.rentalId = rentalId;
        this.equipmentId = equipmentId;
        this.customerId = customerId;
        this.startDate = startDate;
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

    public abstract String toCsvString();
}
