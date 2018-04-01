package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorResponse;
import com._4paradigm.predictor.PredictorStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.InputProvider;

/**
 * Created by dufei on 17/5/16.
 */
@Slf4j
public class HttpPredictionProfiler extends PredictionProfiler {

    @Override
    protected void predict(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
        int threshold = 0;
        HttpResponse res = null;
        try (CloseableHttpClient http = HttpClients.createDefault()) {
            while (true) {
                PredictorRequest pr = ip.getInputQueue().poll();
                if (pr == null) {
                    threshold++;
                    if (threshold >= 3) break;
                    sleepQuietly(1000);
                    continue;
                }
                HttpPost post = new HttpPost(pc.getUrl());
                post.setEntity(new ByteArrayEntity(OM.writeValueAsBytes(pr), ContentType.APPLICATION_JSON));
                long start = System.currentTimeMillis();
                res = http.execute(post);
                long end = System.currentTimeMillis();
                requestsSent.incrementAndGet();
                PredictorResponse response = deserializeQuietly(EntityUtils.toByteArray(res.getEntity()), PredictorResponse.class);
                if (response.getStatus() == PredictorStatus.OK) {
                    requests200.incrementAndGet();
                    response.getInstances().forEach(item -> outputQueue.offer(ImmutablePair.of(Integer.valueOf(item.getId()), item.getScore())));
                } else {
                    requests500.incrementAndGet();
                }
                latencyQueue.add(end-start);
            }
        } catch (Exception e) {
            log.error("caught exception when doing http post...", e);
            if (res != null) EntityUtils.consumeQuietly(res.getEntity());
            throw new RuntimeException(e);
        }
    }

    private void sleepQuietly(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // impossible
        }
    }

}
