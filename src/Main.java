import services.AuthenticationService;
import services.RentalService;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        try {
            AuthenticationService authService = new AuthenticationService();
            RentalService rentalService = new RentalService(authService);

            ConsoleUI consoleUI = new ConsoleUI(authService, rentalService);
            consoleUI.start();
        } catch (Exception e) {
            System.err.println("\nCritical error starting the application: " + e.getMessage());
            System.exit(1);
        }
    }
}
