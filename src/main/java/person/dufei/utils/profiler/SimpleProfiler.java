package person.dufei.utils.profiler;

import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.InputProvider;

import java.util.concurrent.BlockingQueue;

/**
 * Created by dufei on 17/2/27.
 */
public interface SimpleProfiler<I, O> {

    /**
     * Given input provider and profile, pipe output into a blocking queue.
     * @param ip input
     * @return blocking queue contains output
     */
    BlockingQueue<O> profile(InputProvider<I> ip, ProfileConfig pc);

    /**
     * return profiling duration in milliseconds.
     * @return duration
     */
    long getDurationMilli();

    /**
     * return requests sent during profiling, should be updated on real time
     * @return current sent requests
     */
    long getRequestsSent();

    /**
     * return successful requests, should be updated on real time
     * @return current succeeded requests
     */
    long get200s();

    /**
     * return failed requests, should be updated on real time
     * @return current failed requests
     */
    long get500s();

    /**
     * return tp999 latency in milliseconds of all current finished requests, should be updated on real time
     * @return tp999
     */
    long getP999Milli();

    /**
     * return tp99 latency in milliseconds of all current finished requests, should be updated on real time
     * @return tp99
     */
    long getP99Milli();

    /**
     * return tp90 latency in milliseconds of all current finished requests, should be updated on real time
     * @return tp90
     */
    long getP90Milli();

    /**
     * return tp50 latency in milliseconds of all current finished requests, should be updated on real time
     * @return tp50
     */
    long getP50Milli();

}
