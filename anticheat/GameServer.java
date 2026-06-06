package anticheat;

public class GameServer implements Runnable {

    private ActionQueue queue;
    private boolean running = true;
    private AntiCheatEngine engine;

    public GameServer(ActionQueue queue, AntiCheatEngine engine) {
        this.queue = queue;
        this.engine = engine;
    }

    public void stop() {
        running = false;
    }

    public void run() {
        System.out.println("Server Started");

        while (running) {
            Action action = queue.dequeue();

            if (action != null) {
                RiskResult result = engine.evaluate(action);

                System.out.println(
                    "Player " + result.playerId +
                    " | Status: " + result.status +
                    " | Rule: " + result.lastRule);
            }
        }
    }
}
