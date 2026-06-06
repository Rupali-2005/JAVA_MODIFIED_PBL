package anticheat.rules;

import anticheat.Action;
import anticheat.SeverityLevel;

public class RapidFireRule implements CheatRule {
    public String evaluate(Action action) {
        if (action.type.equals("SHOOT") && action.value > 80)
            return getRuleName();
        return null;
    }

    public String getRuleName() {
        return "RAPID_FIRE";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.MEDIUM;
    }
}
