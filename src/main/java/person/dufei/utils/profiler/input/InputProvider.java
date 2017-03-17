package person.dufei.utils.profiler.input;

import java.util.concurrent.BlockingQueue;

/**
 * Provide input as a blocking queue.
 */
public interface InputProvider<T> {

    /**
     * pipe input into a a blocking queue.
     * @return blocking queue that contains input
     */
    BlockingQueue<T> getInputQueue();

}
