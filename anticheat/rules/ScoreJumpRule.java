package anticheat.rules;

import anticheat.Action;
import anticheat.CheatConfig;
import anticheat.SeverityLevel;

public class ScoreJumpRule implements CheatRule {
    public String evaluate(Action action) {
        if (action.type.equals("SCORE") && action.value > CheatConfig.SCORE_THRESHOLD)
            return getRuleName();
        return null;
    }

    public String getRuleName() {
        return "SCORE_JUMP";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.HIGH;
    }
}
