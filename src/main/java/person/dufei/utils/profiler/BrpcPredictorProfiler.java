package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.PredictResponse;
import com._4paradigm.predictor.brpc.client.PredictorBrpcClient;
import com._4paradigm.predictor.utils.Schema;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.profiler.output.SyncPredictorBrpcExecutor;

import java.util.List;
import java.util.function.Consumer;

import static com._4paradigm.prophet.rest.utils.Validator.validateCollectionNotEmptyContainsNoNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

@Slf4j
public class BrpcPredictorProfiler extends SimpleProfiler<PredictRequest> {

    public BrpcPredictorProfiler(final PipeInputProvider<PredictRequest> inputProvider, final Consumer<PredictResponse> handler,
                                 final String endpoints, final List<Schema> schemas, final int concurrency) {
        super(inputProvider, concurrency);
        validateStringNotBlank(endpoints, "service endpoints");
        validateCollectionNotEmptyContainsNoNull(schemas, "schemas");
        setConsumer(new SyncPredictorBrpcExecutor(new PredictorBrpcClient(endpoints, schemas), handler, latency -> {
            latencyQueue.offer(latency);
            requestsCompleted.incrementAndGet();
        }));
    }
}
