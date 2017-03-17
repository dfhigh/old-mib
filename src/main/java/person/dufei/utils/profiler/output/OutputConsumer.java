package person.dufei.utils.profiler.output;

import java.util.concurrent.BlockingQueue;

/**
 * Consumer for blocking queue wrapped output.
 */
public interface OutputConsumer<T> {

    /**
     * Consume output from given blocking queue.
     * @param output queue that contains output
     */
    default void consume(BlockingQueue<T> output) {
        new Thread(() -> {
            while (true) output.poll();
        }).start();
    }

}
