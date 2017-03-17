package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorStatus;
import com._4paradigm.predictor.client.PredictorClient;
//import com._4paradigm.predictor.client.PredictorService;
import com._4paradigm.predictor.client.ThriftPredictorClient;
//import com._4paradigm.predictor.client.discovery.EtcdPredictorServiceDiscovery;
//import com._4paradigm.predictor.client.factory.EtcdPredictorServiceClient;
import com._4paradigm.predictor.client.utils.SimpleJsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ByteArrayEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.InputProvider;

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
public class PredictionProfiler implements SimpleProfiler<PredictorRequest, String> {

    private static final ObjectMapper OM = new ObjectMapper();

    private final AtomicLong requestsSent;
    private final AtomicLong requests200;
    private final AtomicLong requests500;
    private final BlockingQueue<Long> latencyQueue;
    private final BlockingQueue<String> outputQueue;
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
    public BlockingQueue<String> profile(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
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

//    private void predict(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
//        try (CloseableHttpClient http = HttpClients.createDefault()) {
//            while (true) {
//                PredictorRequest pr = ip.getInputQueue().poll();
//                if (pr == null) continue;
//                HttpPost post = new HttpPost(pc.getUrl());
//                post.setEntity(new ByteArrayEntity(OM.writeValueAsBytes(pr)));
//                long start = System.currentTimeMillis();
////              log.info("sending {} in {}...", pr.getRequestId(), Thread.currentThread().getId());
//                HttpResponse res = http.execute(post);
////              log.info("sent {} in {}...", pr.getRequestId(), Thread.currentThread().getId());
//                long end = System.currentTimeMillis();
//                requestsSent.incrementAndGet();
//                EntityUtils.consume(res.getEntity());
//                if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) requests200.incrementAndGet();
//                else requests500.incrementAndGet();
//                latencyQueue.add(end-start);
//            }
//        } catch (Exception e) {
//            log.error("caught exception when doing http post...", e);
//            throw new RuntimeException(e);
//        }
//    }

    private void predict(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
        try {
            PredictorClient client = new ThriftPredictorClient(pc.getUrl(), 3);
            while (true) {
                PredictorRequest pr = ip.getInputQueue().poll();
                if (pr == null) continue;
                long start = System.currentTimeMillis();
                byte[] res = client.predict(OM.writeValueAsBytes(pr));
                long end = System.currentTimeMillis();
                requestsSent.incrementAndGet();
                if (SimpleJsonUtils.extractIntField(res, "status") == PredictorStatus.OK.getValue()) requests200.incrementAndGet();
                else requests500.incrementAndGet();
                latencyQueue.add(end - start);
                if (pc.getSleep() > 0) Thread.sleep(pc.getSleep());
            }
        } catch (Exception e) {
            log.error("caught exception when profiling...", e);
            throw new RuntimeException(e);
        }
    }

}
