package anticheat.rules;

import anticheat.Action;
import anticheat.SeverityLevel;

public class NegativeScoreRule implements CheatRule {
    public String evaluate(Action action) {
        if (action.type.equals("SCORE") && action.value < 0)
            return getRuleName();
        return null;
    }

    public String getRuleName() {
        return "NEGATIVE_SCORE";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.LOW;
    }
}
