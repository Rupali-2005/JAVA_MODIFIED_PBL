package anticheat;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class AntiCheatMain {

    private static AntiCheatEngine engine = new AntiCheatEngine();
    private static ActionQueue queue = new ActionQueue();
    private static GameServer server = new GameServer(queue, engine);
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        System.out.println("===== ANTI-CHEAT ENGINE =====");
        System.out.println("Type 'help' for available commands.");
        System.out.println();

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help":
                    printHelp();
                    break;

                case "submit":
                    handleSubmit(parts);
                    break;

                case "queue":
                    handleQueue(parts);
                    break;

                case "status":
                    handleStatus(parts);
                    break;

                case "reset":
                    handleReset(parts);
                    break;

                case "log":
                    ViolationLogger.displayFullLog();
                    break;

                case "all":
                    handleAll();
                    break;

                case "config":
                    handleConfig(parts);
                    break;

                case "exit":
                    System.out.println("Exiting.");
                    server.stop();
                    running = false;
                    break;

                default:
                    System.out.println("Unknown command: " + command + ". Type 'help' for usage.");
            }
        }

        scanner.close();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  submit <playerId> <type> <value>     Evaluate an action (type: MOVE, SHOOT, SCORE)");
        System.out.println("  queue  <playerId> <type> <value>     Enqueue an action to the GameServer queue");
        System.out.println("  status <playerId>                    Show a player's record and violations");
        System.out.println("  reset  <playerId>                    Reset a player's record");
        System.out.println("  all                                  Show all player records");
        System.out.println("  log                                  Display the full violation log file");
        System.out.println("  config                               Show current thresholds");
        System.out.println("  config <key> <value>                 Change a threshold (e.g. config SPEED_THRESHOLD 2000)");
        System.out.println("  exit                                 Quit");
        System.out.println();
    }

    private static void handleSubmit(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Usage: submit <playerId> <type> <value>");
            return;
        }
        String playerId = parts[1];
        String type = parts[2].toUpperCase();
        double value;
        try {
            value = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value: " + parts[3]);
            return;
        }
        Action action = new Action(playerId, type, System.currentTimeMillis(), value);
        RiskResult result = engine.evaluate(action);
        String verdict = result.lastRule.equals("CLEAN") ? "CLEAN  " : "FLAGGED";
        System.out.println(verdict + " | " + result);
    }

    private static void handleQueue(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Usage: queue <playerId> <type> <value>");
            return;
        }
        String playerId = parts[1];
        String type = parts[2].toUpperCase();
        double value;
        try {
            value = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value: " + parts[3]);
            return;
        }
        Action action = new Action(playerId, type, System.currentTimeMillis(), value);
        if (!ActionValidator.isValid(action)) {
            System.out.println("REJECTED before queuing: " + ActionValidator.getInvalidReason(action));
            return;
        }
        queue.enqueue(action);
        System.out.println("Action queued for " + playerId + " [" + type + " " + value + "]");
    }

    private static void handleStatus(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: status <playerId>");
            return;
        }
        String playerId = parts[1];
        PlayerRecord record = engine.getPlayerRecord(playerId);
        if (record == null) {
            System.out.println("No record found for: " + playerId);
            return;
        }
        System.out.println("Player:     " + record.playerId);
        System.out.println("Status:     " + record.getStatus());
        System.out.println("Risk Score: " + record.getRiskScore());
        System.out.println("Violations: " + record.getViolationCount());
        ArrayList<RuleViolation> violations = record.getViolations();
        if (!violations.isEmpty()) {
            System.out.println("Details:");
            for (RuleViolation v : violations) {
                System.out.println("  " + v);
            }
        }
    }

    private static void handleReset(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: reset <playerId>");
            return;
        }
        engine.resetPlayer(parts[1]);
    }

    private static void handleAll() {
        HashMap<String, PlayerRecord> all = engine.getAllRecords();
        if (all.isEmpty()) {
            System.out.println("No player records yet.");
            return;
        }
        System.out.println();
        for (PlayerRecord record : all.values()) {
            System.out.println("  " + record.playerId + " | Status: " + record.getStatus()
                + " | Risk: " + record.getRiskScore()
                + " | Violations: " + record.getViolationCount());
        }
        System.out.println();
    }

    private static void handleConfig(String[] parts) {
        if (parts.length == 1) {
            System.out.println("SPEED_THRESHOLD:    " + CheatConfig.SPEED_THRESHOLD);
            System.out.println("SCORE_THRESHOLD:    " + CheatConfig.SCORE_THRESHOLD);
            System.out.println("TELEPORT_THRESHOLD: " + CheatConfig.TELEPORT_THRESHOLD);
            System.out.println("AIMBOT_WINDOW:      " + CheatConfig.AIMBOT_WINDOW);
            System.out.println("SUSPICIOUS_SCORE:   " + CheatConfig.SUSPICIOUS_SCORE);
            System.out.println("BLOCKED_SCORE:      " + CheatConfig.BLOCKED_SCORE);
            return;
        }
        if (parts.length != 3) {
            System.out.println("Usage: config <key> <value>");
            return;
        }
        String key = parts[1].toUpperCase();
        try {
            double val = Double.parseDouble(parts[2]);
            switch (key) {
                case "SPEED_THRESHOLD":
                    CheatConfig.SPEED_THRESHOLD = val;
                    break;
                case "SCORE_THRESHOLD":
                    CheatConfig.SCORE_THRESHOLD = val;
                    break;
                case "TELEPORT_THRESHOLD":
                    CheatConfig.TELEPORT_THRESHOLD = val;
                    break;
                case "AIMBOT_WINDOW":
                    CheatConfig.AIMBOT_WINDOW = (int) val;
                    break;
                case "SUSPICIOUS_SCORE":
                    CheatConfig.SUSPICIOUS_SCORE = (int) val;
                    break;
                case "BLOCKED_SCORE":
                    CheatConfig.BLOCKED_SCORE = (int) val;
                    break;
                default:
                    System.out.println("Unknown config key: " + key);
                    return;
            }
            System.out.println(key + " set to " + parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value: " + parts[2]);
        }
    }
}
