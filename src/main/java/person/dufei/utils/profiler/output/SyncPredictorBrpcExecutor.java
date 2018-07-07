package person.dufei.utils.profiler.output;

import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.PredictResponse;
import com._4paradigm.predictor.brpc.client.PredictorBrpcClient;
import com._4paradigm.prophet.rest.pipe.io.PipeOutputConsumer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;

public class SyncPredictorBrpcExecutor implements PipeOutputConsumer<PredictRequest, Void> {

    private final PredictorBrpcClient predictor;
    private final AtomicLong consumed;
    private final Consumer<PredictResponse> handler;
    private final Consumer<Long> latencyHandler;

    public SyncPredictorBrpcExecutor(final PredictorBrpcClient predictor, final Consumer<PredictResponse> handler,
                                     final Consumer<Long> latencyHandler) {
        validateObjectNotNull(predictor, "predictor client");
        validateObjectNotNull(handler, "response handler");
        validateObjectNotNull(handler, "latency handler");
        this.predictor = predictor;
        this.handler = handler;
        this.latencyHandler = latencyHandler;
        this.consumed = new AtomicLong(0);
    }

    @Override
    public Void consume(PredictRequest payload) {
        try {
            long start = System.currentTimeMillis();
            PredictResponse response = predictor.predict(payload);
            latencyHandler.accept(System.currentTimeMillis()-start);
            handler.accept(response);
        } finally {
            consumed.incrementAndGet();
        }
        return null;
    }

    @Override
    public long consumed() {
        return consumed.get();
    }
}
