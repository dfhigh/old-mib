package person.dufei.utils.profiler;

import lombok.Data;

/**
 * Created by dufei on 17/2/27.
 */
public interface SimpleProfiler {

    /**
     * Start the profiling process asynchronously. This method returns immediately.
     */
    void start();

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
     * return requests completed during profiling, should be updated on real time
     * @return current completed requests
     */
    long getRequestsCompleted();

    /**
     * return latency stats in milliseconds of all current finished requests, should be updated on real time
     * @return tp999
     */
    LatencyStats getLatencyStats();

    @Data
    class LatencyStats {
        private int size;
        private long tp50;
        private long tp90;
        private long tp99;
        private long tp999;
        private long tp9999;
    }

}
