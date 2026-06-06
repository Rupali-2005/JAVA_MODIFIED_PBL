package anticheat.rules;

import anticheat.Action;
import anticheat.SeverityLevel;

public interface CheatRule {
    String evaluate(Action action);
    String getRuleName();
    SeverityLevel getSeverity();
}
