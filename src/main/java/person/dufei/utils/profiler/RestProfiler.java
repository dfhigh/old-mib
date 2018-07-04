package person.dufei.utils.profiler;

import com._4paradigm.prophet.rest.client.AsyncHttpOperator;
import com._4paradigm.prophet.rest.client.HttpOperator;
import com._4paradigm.prophet.rest.client.callback.HttpResponseHandler;
import com._4paradigm.prophet.rest.client.callback.LatencyAwareHttpResponseHandler;
import com._4paradigm.prophet.rest.pipe.PipeDriver;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.PipeOutputConsumer;
import com._4paradigm.prophet.rest.pipe.io.impl.AsyncRestExecutor;
import com._4paradigm.prophet.rest.pipe.io.impl.SyncRestExecutor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com._4paradigm.prophet.rest.utils.ResponseInterceptor.REQUEST_TIME_KEY;
import static com._4paradigm.prophet.rest.utils.Validator.validateIntPositive;
import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class RestProfiler<R extends HttpUriRequest, T> implements SimpleProfiler {

    private final AtomicBoolean started;
    private final AtomicLong requestsCompleted;
    private final PipeOutputConsumer<R, Void> consumer;
    private final BlockingQueue<Long> latencyQueue;
    private final PipeDriver<R, Void> pipeDriver;
    private long start;

    public RestProfiler(final HttpOperator http, final PipeInputProvider<R> inputProvider, final HttpResponseHandler<T> handler, int concurrency, boolean async) {
        validateObjectNotNull(http, "http operator");
        validateObjectNotNull(inputProvider, "input provider");
        validateObjectNotNull(handler, "response handler");
        validateIntPositive(concurrency, "concurrency");
        this.started = new AtomicBoolean(false);
        this.requestsCompleted = new AtomicLong(0);
        this.latencyQueue = new LinkedBlockingQueue<>();
        this.consumer = async ? new AsyncRestExecutor<>((AsyncHttpOperator) http, handler, request ->
            new BasicAsyncResponseConsumer() {
                @Override
                protected HttpResponse buildResult(final HttpContext context) {
                    Long requestTime = (Long) context.removeAttribute(REQUEST_TIME_KEY);
                    if (requestTime != null) latencyQueue.offer(System.currentTimeMillis() - requestTime);
                    requestsCompleted.incrementAndGet();
                    return super.buildResult(context);
                }
            }) : new SyncRestExecutor<>(http, new LatencyAwareHttpResponseHandler<>(handler, latency -> {
                requestsCompleted.incrementAndGet();
                latencyQueue.offer(latency);
            }));
        this.pipeDriver = new PipeDriver<>(inputProvider, consumer, concurrency);
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            start = System.currentTimeMillis();
            pipeDriver.start();
        }
    }

    @Override
    public long getDurationMilli() {
        return System.currentTimeMillis() - start;
    }

    @Override
    public long getRequestsSent() {
        return consumer.consumed();
    }

    @Override
    public long getRequestsCompleted() {
        return requestsCompleted.get();
    }

    @Override
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

}
