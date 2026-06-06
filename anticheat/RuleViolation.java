package anticheat;

public class RuleViolation {
    public final String playerId;
    public final String ruleName;
    public final SeverityLevel severity;
    public final long timestamp;
    public final double actionValue;

    public RuleViolation(String playerId, String ruleName, SeverityLevel severity, long timestamp, double actionValue) {
        this.playerId = playerId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.timestamp = timestamp;
        this.actionValue = actionValue;
    }

    public String toString() {
        return "[" + ruleName + " | Severity: " + severity + " | Weight:+" + severity.weight + " | Value: " + actionValue + "]";
    }
}
