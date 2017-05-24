package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorResponse;
import com._4paradigm.predictor.PredictorStatus;
import com._4paradigm.predictor.client.PredictorClient;
import com._4paradigm.predictor.client.ThriftPredictorClient;
import com._4paradigm.predictor.client.request.RequestContext;
import com._4paradigm.predictor.client.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.InputProvider;

import java.nio.ByteBuffer;

/**
 * Created by dufei on 17/5/16.
 */
@Slf4j
public class ThriftPredictionProfiler extends PredictionProfiler {

    @Override
    protected void predict(InputProvider<PredictorRequest> ip, ProfileConfig pc) {
        try {
            PredictorClient client = new ThriftPredictorClient(pc.getUrl(), 3);
            while (true) {
                PredictorRequest pr = ip.getInputQueue().poll();
                if (pr == null) continue;
                long start = System.currentTimeMillis();
                RequestContext rc = new RequestContext(ByteBuffer.wrap(OM.writeValueAsBytes(pr)), "predictor");
                Response res = client.predict(rc);
                long end = System.currentTimeMillis();
                requestsSent.incrementAndGet();
                PredictorResponse response = deserializeQuietly(res.getPayload(), PredictorResponse.class);
                if (response.getStatus() == PredictorStatus.OK){
                    requests200.incrementAndGet();
                    response.getInstances().forEach(item -> outputQueue.offer(ImmutablePair.of(Integer.valueOf(item.getId()), item.getScore())));
                } else {
                    requests500.incrementAndGet();
                }
                latencyQueue.add(end - start);
                if (pc.getSleep() > 0) Thread.sleep(pc.getSleep());
            }
        } catch (Exception e) {
            log.error("caught exception when profiling...", e);
            throw new RuntimeException(e);
        }
    }

}
