package anticheat.rules;

import anticheat.Action;
import anticheat.CheatConfig;
import anticheat.SeverityLevel;

public class SpeedHackRule implements CheatRule {
    public String evaluate(Action action) {
        if (action.type.equals("MOVE") && action.value > CheatConfig.SPEED_THRESHOLD)
            return getRuleName();
        return null;
    }

    public String getRuleName() {
        return "SPEED_HACK";
    }

    public SeverityLevel getSeverity() {
        return SeverityLevel.HIGH;
    }
}
