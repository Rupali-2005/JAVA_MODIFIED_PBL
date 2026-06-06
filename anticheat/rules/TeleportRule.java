package anticheat.rules;

import anticheat.Action;
import anticheat.CheatConfig;
import anticheat.SeverityLevel;
import java.util.HashMap;

public class TeleportRule implements CheatRule {
    private HashMap<String, Double> lastMoveValue = new HashMap<>();

    public String evaluate(Action action) {
        if (!action.type.equals("MOVE"))
            return null;
        double lastMove = lastMoveValue.getOrDefault(action.playerId, action.value);
        double delta = Math.abs(action.value - lastMove);
        lastMoveValue.put(action.playerId, action.value);
        if (delta > CheatConfig.TELEPORT_THRESHOLD)
            return getRuleName();
        return null;
    }

    public String getRuleName() {
        return "TELEPORT";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.HIGH;
    }
}
