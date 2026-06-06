package anticheat;
// Defines attribute of each player
public class Action {
    public String playerId;
    public String type;
    public long timestamp;
    public double value;
    public Action(String playerId, String type, long timestamp, double value) {
        this.playerId = playerId;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }
}
