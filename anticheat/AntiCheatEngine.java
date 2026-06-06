package anticheat;

import anticheat.rules.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AntiCheatEngine {

    private HashMap<String, PlayerRecord> playerRecords = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private ArrayList<CheatRule> rules = new ArrayList<>();

    public AntiCheatEngine() {
        rules.add(new SpeedHackRule());
        rules.add(new ScoreJumpRule());
        rules.add(new NegativeScoreRule());
        rules.add(new TeleportRule());
        rules.add(new AimbotRule());
        rules.add(new RapidFireRule());

        loadPersistedViolations();
        ViolationLogger.startSession();
    }

    private void loadPersistedViolations() {
        ArrayList<RuleViolation> past = ViolationLogger.loadAll();
        for (RuleViolation v : past) {
            playerRecords.putIfAbsent(v.playerId, new PlayerRecord(v.playerId));
            playerRecords.get(v.playerId).addViolation(v);
        }
    }

    public RiskResult evaluate(Action action) {
        if (!ActionValidator.isValid(action)) {
            System.out.println("REJECTED: " + ActionValidator.getInvalidReason(action));
            return new RiskResult("UNKNOWN", 0, "SAFE", "INVALID");
        }

        lock.lock();
        try {
            playerRecords.putIfAbsent(action.playerId, new PlayerRecord(action.playerId));
            PlayerRecord record = playerRecords.get(action.playerId);

            String ruleTriggered = "CLEAN";
            SeverityLevel severity = null;

            for (CheatRule rule : rules) {
                String result = rule.evaluate(action);
                if (result != null) {
                    ruleTriggered = result;
                    severity = rule.getSeverity();
                    break;
                }
            }

            if (!ruleTriggered.equals("CLEAN")) {
                RuleViolation violation = new RuleViolation(
                    action.playerId, ruleTriggered,
                    severity, action.timestamp, action.value
                );
                record.addViolation(violation);
                ViolationLogger.log(violation);
            }

            return new RiskResult(
                action.playerId,
                record.getViolationCount(),
                record.getStatus(),
                ruleTriggered
            );

        } finally {
            lock.unlock();
        }
    }

    public void resetPlayer(String playerId) {
        lock.lock();
        try {
            if (playerRecords.containsKey(playerId)) {
                playerRecords.get(playerId).reset();
                System.out.println("Reset record for: " + playerId);
            }
        } finally {
            lock.unlock();
        }
    }

    public PlayerRecord getPlayerRecord(String playerId) {
        lock.lock();
        try {
            return playerRecords.get(playerId);
        } finally {
            lock.unlock();
        }
    }

    public HashMap<String, PlayerRecord> getAllRecords() {
        lock.lock();
        try {
            return new HashMap<>(playerRecords);
        } finally {
            lock.unlock();
        }
    }
}
