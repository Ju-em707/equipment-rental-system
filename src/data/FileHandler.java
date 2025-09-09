package data;

import models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    private static final String EQUIPMENT_FILE = "equipment.csv";
    private static final String RENTALS_FILE = "rentals.csv";
    private static final String RETURNS_FILE = "returns.csv";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static List<Equipment> loadEquipment() {
        List<Equipment> equipment = new ArrayList<>();
        try {
            if (!Files.exists(Path.of(EQUIPMENT_FILE))) {
                createEquipmentFile();
            }

            List<String> lines = Files.readAllLines(Path.of(EQUIPMENT_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String category = parts.length > 4 ? parts[4].trim() : "General";
                    equipment.add(new Equipment(
                            parts[0].trim(),
                            parts[1].trim(),
                            Double.parseDouble(parts[2].trim()),
                            parts[3].trim(),
                            category
                    ));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading equipment: " + e.getMessage());
        }
        return equipment;
    }

    public static void saveEquipment(List<Equipment> equipment) {
        try {
            List<String> lines = new ArrayList<>();
            for (Equipment eq : equipment) {
                lines.add(eq.toCsvString());
            }
            Files.write(Path.of(EQUIPMENT_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving equipment: " + e.getMessage());
        }
    }

    public static List<Rental> loadRentals() {
        List<Rental> rentals = new ArrayList<>();

        try {
            if (!Files.exists(Path.of(RENTALS_FILE))) {
                createRentalsFile();
            }

            List<String> lines = Files.readAllLines(Path.of(RENTALS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    rentals.add(new Rental(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            LocalDate.parse(parts[3].trim(), DATE_TIME_FORMATTER),
                            Integer.parseInt(parts[4].trim()),
                            Double.parseDouble(parts[5].trim())
                            ));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading rentals: " + e.getMessage());
        }
        return rentals;
    }

    public static void saveRentals(List<Rental> rentals) {
        try {
            List<String> lines = new ArrayList<>();
            for (Rental rental : rentals) {
                lines.add(rental.toCsvString());
            }
            Files.write(Path.of(RENTALS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving rentals: " + e.getMessage());
        }
    }

    public static List<ReturnRecord> loadReturns() {
        List<ReturnRecord> returns = new ArrayList<>();
        try {
            if (!Files.exists(Path.of(RETURNS_FILE))) {
                createReturnsFile();
            }

            List<String> lines = Files.readAllLines(Path.of(RETURNS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    ReturnRecord record = new ReturnRecord(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            LocalDate.parse(parts[3].trim(), DATE_TIME_FORMATTER),
                            LocalDate.parse(parts[4].trim(), DATE_TIME_FORMATTER),
                            Double.parseDouble(parts[5].trim())
                    );
                    if (parts.length > 6) {
                        record.setLateFee(Double.parseDouble(parts[6].trim()));
                    }
                    if (parts.length > 7) {
                        record.setCondition(parts[7].trim());
                    }
                    returns.add(record);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading returns: " + e.getMessage());
        }
        return returns;
    }

    public static void saveReturns(List<ReturnRecord> returns) {
        try {
            List<String> lines = new ArrayList<>();
            for (ReturnRecord record : returns) {
                lines.add(record.toCsvString());
            }
            Files.write(Path.of(RETURNS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving returns: " + e.getMessage());
        }
    }

    private static void createEquipmentFile() throws IOException {
        List<String> initialData = Arrays.asList(
                "E101,Projector,20.00,Available,Electronics",
                "E102,Sound System,50.00,Available,Audio",
                "E103,Laptop,30.00,Available,Electronics",
                "E104,Camera,25.00,Available,Photography",
                "E105,Microphone,10.00,Available,Audio"
        );
        Files.write(Path.of(EQUIPMENT_FILE), initialData);
    }

    private static void createRentalsFile() throws IOException {
        Files.createFile(Path.of(RENTALS_FILE));
    }

    private static void createReturnsFile() throws IOException {
        Files.createFile(Path.of(RETURNS_FILE));
    }
}
