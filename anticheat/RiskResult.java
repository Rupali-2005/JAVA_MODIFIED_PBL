package anticheat;

public class RiskResult {
    public String playerId;
    public int flagCount;
    public String status;
    public String lastRule;

    public RiskResult(String playerId, int flagCount, String status, String lastRule) {
        this.playerId = playerId;
        this.flagCount = flagCount;
        this.status = status;
        this.lastRule = lastRule;
    }

    public String toString() {
        return "[" + playerId + "] Status: " + status + " | Flags: " + flagCount + " | Last: " + lastRule;
    }
}
