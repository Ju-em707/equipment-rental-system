package utils;

public class Constants {
    public static final String EQUIPMENT_FILE = "equipment.csv";
    public static final String RENTALS_FILE = "rentals.csv";
    public static final String RETURNS_FILE = "returns.csv";
    public static final String USERS_FILE = "users.csv";

    public static final double LATE_FEE_PER_DAY = 50.0;

    public static final String EQUIPMENT_STATUS_AVAILABLE = "Available";
    public static final String EQUIPMENT_STATUS_RENTED = "Rented";
    public static final String EQUIPMENT_STATUS_MAINTENANCE = "Maintenance";

    public static final String RENTAL_STATUS_ACTIVE = "Active";
    public static final String RENTAL_STATUS_OVERDUE = "Overdue";
    public static final String RENTAL_STATUS_RETURNED = "Returned";

    public static final String DEFAULT_CATEGORY = "General";

    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_CUSTOMER_PASSWORD = "customer123";

    public static final String USERTYPE_ADMIN = "ADMIN";
    public static final String USERTYPE_CUSTOMER = "CUSTOMER";

    public static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    public static final String ACCOUNT_STATUS_INACTIVE = "INACTIVE";
    public static final String ACCOUNT_STATUS_LOCKED = "LOCKED";

}
