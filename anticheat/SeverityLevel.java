package anticheat;

public enum SeverityLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(5);

    public final int weight;

    SeverityLevel(int weight) {
        this.weight = weight;
    }
}
