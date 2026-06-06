package anticheat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActionQueue {
    private BlockingQueue<Action> queue = new LinkedBlockingQueue<>();

    public void enqueue(Action action) {
        try {
            queue.put(action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Action dequeue() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
