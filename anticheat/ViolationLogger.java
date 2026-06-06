package anticheat;

import java.io.*;
import java.util.ArrayList;

public class ViolationLogger {
    private static final String LOG_FILE = "violations.log";
    private static final String DELIMITER = "|";

    public static void startSession() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write("--- SESSION START:" + System.currentTimeMillis() + "---");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Could not write session header: " + e.getMessage());
        }
    }

    public static void log(RuleViolation violation) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(buildLine(violation));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Could not write violation: " + e.getMessage());
        }
    }

    private static String buildLine(RuleViolation v) {
        return v.playerId + DELIMITER + v.ruleName + DELIMITER + v.severity.name() + DELIMITER + v.timestamp + DELIMITER + v.actionValue;
    }

    public static ArrayList<RuleViolation> loadAll() {
        ArrayList<RuleViolation> loaded = new ArrayList<>();
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            System.out.println("No existing log found. Starting fresh.");
            return loaded;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            int loadedCount = 0;
            int skipped = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty())
                    continue;
                if (line.startsWith("---"))
                    continue;
                RuleViolation v = parseLine(line, lineNumber);
                if (v != null) {
                    loaded.add(v);
                    loadedCount++;
                } else {
                    skipped++;
                }
            }
            System.out.println("Loaded " + loadedCount + " past violation(s)." + (skipped > 0 ? " (" + skipped + " skipped)" : ""));
        } catch (IOException e) {
            System.out.println("Could not read log: " + e.getMessage());
        }
        return loaded;
    }

    private static RuleViolation parseLine(String line, int lineNumber) {
        String[] parts = line.split("\\" + DELIMITER);
        if (parts.length != 5) {
            System.out.println("Skipping line " + lineNumber + ": expected 5 fields, got " + parts.length);
            return null;
        }
        try {
            String playerId = parts[0];
            String ruleName = parts[1];
            SeverityLevel severity = SeverityLevel.valueOf(parts[2]);
            long timestamp = Long.parseLong(parts[3]);
            double actionValue = Double.parseDouble(parts[4]);
            return new RuleViolation(playerId, ruleName, severity, timestamp, actionValue);
        } catch (IllegalArgumentException e) {
            System.out.println("Skipping line " + lineNumber + ": " + e.getMessage());
            return null;
        }
    }

    public static void displayFullLog() {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            System.out.println("No log file found.");
            return;
        }
        System.out.println("===== FULL VIOLATION LOG =====");
        System.out.println("File: " + file.getAbsolutePath());
        System.out.println("------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.startsWith("---") && !line.trim().isEmpty())
                    count++;
            }
            System.out.println("------------------------------");
            System.out.println("Total logged violations: " + count);
            System.out.println("==============================\n");
        } catch (IOException e) {
            System.out.println("Could not read log: " + e.getMessage());
        }
    }
}
