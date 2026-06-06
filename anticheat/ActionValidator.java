package anticheat;

public class ActionValidator {
    private static final String[] VALID_TYPES = { "MOVE", "SHOOT", "SCORE" };

    public static boolean isValid(Action action) {
        if (action == null)
            return false;
        if (action.playerId == null || action.playerId.trim().isEmpty())
            return false;
        if (action.type == null)
            return false;
        if (!isValidType(action.type))
            return false;
        if (Double.isNaN(action.value) || Double.isInfinite(action.value))
            return false;
        if (action.timestamp <= 0)
            return false;
        return true;
    }

    public static String getInvalidReason(Action action) {
        if (action == null)
            return "Action is null";
        if (action.playerId == null || action.playerId.trim().isEmpty())
            return "Player ID is null or empty";
        if (action.type == null)
            return "Action type is null";
        if (!isValidType(action.type))
            return "Unknown type: " + action.type;
        if (Double.isNaN(action.value))
            return "Value is NaN";
        if (Double.isInfinite(action.value))
            return "Value is Infinite";
        if (action.timestamp <= 0)
            return "Invalid timestamp: " + action.timestamp;
        return "Unknown reason";
    }

    private static boolean isValidType(String type) {
        for (String valid : VALID_TYPES) {
            if (valid.equals(type))
                return true;
        }
        return false;
    }
}
