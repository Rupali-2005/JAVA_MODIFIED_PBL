package anticheat;

import java.util.ArrayList;

public class PlayerRecord {
    public final String playerId;
    private int riskScore;
    private String status;
    private ArrayList<RuleViolation> violations;

    public PlayerRecord(String playerId) {
        this.playerId = playerId;
        this.riskScore = 0;
        this.status = "SAFE";
        this.violations = new ArrayList<>();
    }

    public void addViolation(RuleViolation violation) {
        violations.add(violation);
        riskScore += violation.severity.weight;
        status = computeStatus();
    }

    private String computeStatus() {
        if (riskScore >= CheatConfig.BLOCKED_SCORE)
            return "BLOCKED";
        if (riskScore >= CheatConfig.SUSPICIOUS_SCORE)
            return "SUSPICIOUS";
        return "SAFE";
    }

    public void reset() {
        riskScore = 0;
        status = "SAFE";
        violations = new ArrayList<>();
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getStatus() {
        return status;
    }

    public int getViolationCount() {
        return violations.size();
    }

    public ArrayList<RuleViolation> getViolations() {
        return violations;
    }
}
