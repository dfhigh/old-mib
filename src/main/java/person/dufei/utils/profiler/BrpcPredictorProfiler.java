package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.PredictResponse;
import com._4paradigm.predictor.client.PredictorClient;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.profiler.output.SyncPredictorBrpcExecutor;

import java.util.function.Consumer;

@Slf4j
public class BrpcPredictorProfiler extends SimpleProfiler<PredictRequest> {

    public BrpcPredictorProfiler(final PipeInputProvider<PredictRequest> inputProvider, final Consumer<PredictResponse> handler,
                                 final PredictorClient client, final int concurrency) {
        super(inputProvider, concurrency);
        setConsumer(new SyncPredictorBrpcExecutor(client, handler, latency -> {
            latencyQueue.offer(latency);
            requestsCompleted.incrementAndGet();
        }));
    }
}
