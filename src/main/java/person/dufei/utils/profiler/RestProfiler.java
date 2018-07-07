package person.dufei.utils.profiler;

import com._4paradigm.prophet.rest.client.AsyncHttpOperator;
import com._4paradigm.prophet.rest.client.HttpOperator;
import com._4paradigm.prophet.rest.client.callback.HttpResponseHandler;
import com._4paradigm.prophet.rest.client.callback.LatencyAwareHttpResponseHandler;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.impl.AsyncRestExecutor;
import com._4paradigm.prophet.rest.pipe.io.impl.SyncRestExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;


import static com._4paradigm.prophet.rest.utils.ResponseInterceptor.REQUEST_TIME_KEY;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class RestProfiler<R extends HttpUriRequest, T> extends SimpleProfiler<R> {

    public RestProfiler(final HttpOperator http, final PipeInputProvider<R> inputProvider, final HttpResponseHandler<T> handler, int concurrency, boolean async) {
        super(inputProvider, concurrency);
        setConsumer(async ? new AsyncRestExecutor<>((AsyncHttpOperator) http, handler, request ->
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
                    latencyQueue.offer(latency);}
                ))
        );
    }

}
