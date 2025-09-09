package models;

public class Equipment {
    private String id;
    private String name;
    private double rentPerDay;
    private String availability;
    private String category;

    public Equipment(String id, String name, double rentPerDay, String availability) {
        this.id = id;
        this.name = name;
        this.rentPerDay = rentPerDay;
        this.availability = availability;
        this.category = "General";
    }

    public Equipment(String id, String name, double rentPerDay, String availability, String category) {
        this.id = id;
        this.name = name;
        this.rentPerDay = rentPerDay;
        this.availability = availability;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(double rentPerDay) { this.rentPerDay = rentPerDay; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(availability);
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Rate: $%.2f/day | Status: %s | Category: %s",
                id, name, rentPerDay, availability, category);
    }

    public String toCsvString() {
        return String.format("%s,%s,%.2f,%s,%s", id, name, rentPerDay, availability, category);
    }
}
