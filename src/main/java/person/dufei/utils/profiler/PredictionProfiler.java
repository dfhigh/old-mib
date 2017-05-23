package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictorRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.InputProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public abstract class PredictionProfiler implements SimpleProfiler<PredictorRequest, Pair<Integer, Double>> {

    protected static final ObjectMapper OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected final AtomicLong requestsSent;
    protected final AtomicLong requests200;
    protected final AtomicLong requests500;
    protected final BlockingQueue<Long> latencyQueue;
    protected final BlockingQueue<Pair<Integer, Double>> outputQueue;
    private long start;
    private ExecutorService es;

    public PredictionProfiler() {
        this.requestsSent = new AtomicLong(0);
        this.requests200 = new AtomicLong(0);
        this.requests500 = new AtomicLong(0);
        this.latencyQueue = new LinkedBlockingDeque<>();
        this.outputQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public BlockingQueue<Pair<Integer, Double>> profile(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
        int concurrency = pc.getConcurrency();
        es = Executors.newFixedThreadPool(concurrency);
        start = System.currentTimeMillis();
        for (int i = 0; i < concurrency; i++) {
            es.submit(() -> predict(ip, pc));
        }
        return outputQueue;
    }

    @Override
    public long getDurationMilli() {
        return System.currentTimeMillis() - start;
    }

    @Override
    public long getRequestsSent() {
        return requestsSent.get();
    }

    @Override
    public long get200s() {
        return requests200.get();
    }

    @Override
    public long get500s() {
        return requests500.get();
    }

    @Override
    public long getP999Milli() {
        if (latencyQueue.isEmpty()) return 0;
        List<Long> list = Lists.newArrayList(latencyQueue);
        Collections.sort(list);
        int index = (int) (list.size() * 0.999);
        return list.get(index);
    }

    @Override
    public long getP99Milli() {
        if (latencyQueue.isEmpty()) return 0;
        List<Long> list = Lists.newArrayList(latencyQueue);
        Collections.sort(list);
        int index = (int) (list.size() * 0.99);
        return list.get(index);
    }

    @Override
    public long getP90Milli() {
        if (latencyQueue.isEmpty()) return 0;
        List<Long> list = Lists.newArrayList(latencyQueue);
        Collections.sort(list);
        int index = (int) (list.size() * 0.9);
        return list.get(index);
    }

    @Override
    public long getP50Milli() {
        if (latencyQueue.isEmpty()) return 0;
        List<Long> list = Lists.newArrayList(latencyQueue);
        Collections.sort(list);
        int index = list.size() / 2;
        return list.get(index);
    }

    protected abstract void predict(InputProvider<PredictorRequest> ip, ProfileConfig pc);

    protected <T> T deserializeQuietly(byte[] bytes, Class<T> clazz) {
        try {
            return OM.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("failed to deserialize {} as {}", new String(bytes, StandardCharsets.UTF_8), clazz.getName());
            throw new RuntimeException(e);
        }
    }

}
