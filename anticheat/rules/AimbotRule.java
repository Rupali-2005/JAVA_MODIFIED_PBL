package anticheat.rules;

import anticheat.Action;
import anticheat.CheatConfig;
import anticheat.SeverityLevel;
import java.util.*;
public class AimbotRule implements CheatRule {
    private HashMap<String, LinkedList<Double>> shootHistory = new HashMap<>();
    public String evaluate(Action action) {
        if (!action.type.equals("SHOOT"))
            return null;
        shootHistory.putIfAbsent(action.playerId, new LinkedList<>());
        LinkedList<Double> history = shootHistory.get(action.playerId);
        history.addLast(action.value);
        while (history.size() > CheatConfig.AIMBOT_WINDOW)
            history.removeFirst();
        if (history.size() < CheatConfig.AIMBOT_WINDOW)
            return null;
        for (double accuracy : history) {
            if (accuracy != 1.0)
                return null;
        }
        return getRuleName();
    }

    public String getRuleName() {
        return "AIMBOT";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.CRITICAL;
    }
}
