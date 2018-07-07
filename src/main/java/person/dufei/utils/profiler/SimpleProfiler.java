package person.dufei.utils.profiler;

import com._4paradigm.prophet.rest.pipe.PipeDriver;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.PipeOutputConsumer;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com._4paradigm.prophet.rest.utils.Validator.validateIntPositive;
import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;

/**
 * Created by dufei on 17/2/27.
 */
public abstract class SimpleProfiler<R> {

    private final AtomicBoolean started;
    final AtomicLong requestsCompleted;
    private final PipeInputProvider<R> inputProvider;
    private PipeOutputConsumer<R, Void> consumer;
    final BlockingQueue<Long> latencyQueue;
    private PipeDriver<R, Void> pipeDriver;
    private final int concurrency;
    private long start;

    public SimpleProfiler(final PipeInputProvider<R> inputProvider, int concurrency) {
        validateObjectNotNull(inputProvider, "input provider");
        validateIntPositive(concurrency, "concurrency");
        this.inputProvider = inputProvider;
        this.started = new AtomicBoolean(false);
        this.concurrency = concurrency;
        this.requestsCompleted = new AtomicLong(0);
        this.latencyQueue = new LinkedBlockingQueue<>();
    }

    void setConsumer(PipeOutputConsumer<R, Void> consumer) {
        if (this.consumer == null) {
            validateObjectNotNull(consumer, "output consumer");
            this.consumer = consumer;
            this.pipeDriver = new PipeDriver<>(inputProvider, consumer, concurrency);
        }
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            start = System.currentTimeMillis();
            pipeDriver.start();
        }
    }

    public long getDurationMilli() {
        return System.currentTimeMillis() - start;
    }

    public long getRequestsSent() {
        return consumer.consumed();
    }

    public long getRequestsCompleted() {
        return requestsCompleted.get();
    }

    public LatencyStats getLatencyStats() {
        LatencyStats ls = new LatencyStats();
        if (latencyQueue.isEmpty()) return ls;
        List<Long> list = Lists.newArrayList(latencyQueue);
        Collections.sort(list);
        ls.setSize(list.size());
        ls.setTp9999(list.get((int) (list.size() * 0.9999)));
        ls.setTp999(list.get((int) (list.size() * 0.999)));
        ls.setTp99(list.get((int) (list.size() * 0.99)));
        ls.setTp90(list.get((int) (list.size() * 0.9)));
        ls.setTp50(list.get(list.size() / 2));
        return ls;
    }

    @Data
    public static class LatencyStats {
        private int size;
        private long tp50;
        private long tp90;
        private long tp99;
        private long tp999;
        private long tp9999;
    }

}
