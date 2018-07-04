package person.dufei.utils.main;

import com._4paradigm.predictor.PredictResponse;
import com._4paradigm.predictor.Status;
import com._4paradigm.prophet.rest.client.AsyncHttpOperator;
import com._4paradigm.prophet.rest.client.HttpOperator;
import com._4paradigm.prophet.rest.client.SyncHttpOperator;
import com._4paradigm.prophet.rest.client.callback.HttpResponseHandler;
import com._4paradigm.prophet.rest.client.callback.JsonHttpResponseHandler;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.impl.InfiniteCyclingPipeInputProvider;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpPost;
import person.dufei.utils.profiler.RestProfiler;
import person.dufei.utils.profiler.SimpleProfiler;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.PredictRequestFilePipeInputProvider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class PredictorProfileMain {

    public static void main(String[] args) throws Exception {
        helpIntercept();
        String filePath = System.getProperty("filePath");
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("file path can't be blank");
        }
        AtomicLong succeeds = new AtomicLong(0);
        ProfileConfig pc = ProfileConfig.fromEnv();
        log.info("profile config is {}", pc);
        BlockingQueue<Pair<Integer, List<Double>>> outputQueue = new LinkedBlockingQueue<>();
        PipeInputProvider<HttpPost> inputProvider = new PredictRequestFilePipeInputProvider(pc.getUrl(), filePath,
                pc.getBatchSize(), pc.getDelimiter(), pc.isFirstLineSchema(), pc.getSchemas(), pc.getAccessToken());
        if (pc.isForever()) {
            inputProvider = new InfiniteCyclingPipeInputProvider<>(inputProvider);
        }
        HttpResponseHandler<PredictResponse> handler =  new JsonHttpResponseHandler<PredictResponse>(PredictResponse.class) {
            @Override
            protected void onSuccess(PredictResponse response) {
                if (response.getStatus() == Status.OK) succeeds.incrementAndGet();
                response.getInstances().forEach(item -> {
                    Pair<Integer, List<Double>> pair = Pair.of(Integer.parseInt(item.getId()), item.getScores());
                    while (!outputQueue.offer(pair)) {
                        sleepQuietly(100);
                    }
                });
            }
        };
        HttpOperator http = pc.isAsync() ? new AsyncHttpOperator(16, 16) : new SyncHttpOperator(16, 16);
        SimpleProfiler profiler = new RestProfiler<>(http, inputProvider, handler, pc.getConcurrency(), pc.isAsync());
        profiler.start();
        int threshold = 0;
        while (true) {
            long produced = inputProvider.provided(), sent = profiler.getRequestsSent(), completed = profiler.getRequestsCompleted();
            SimpleProfiler.LatencyStats ls = profiler.getLatencyStats();
            log.info("profile duration: {}, requests produced: {}, requests sent: {}, response received: {}, 200: {}, tp50: {}, tp90: {}, tp99: {}, tp999: {}, tp9999: {}",
                    profiler.getDurationMilli(),
                    inputProvider.provided(),
                    sent,
                    completed,
                    succeeds.get(),
                    ls.getTp50(),
                    ls.getTp90(),
                    ls.getTp99(),
                    ls.getTp999(),
                    ls.getTp9999());
            if (produced == sent && sent == completed) {
                threshold++;
                if (threshold >= 3 && inputProvider.isClosed()) break;
            } else {
                threshold = 0;
            }
            Thread.sleep(3000);
        }
        List<Pair<Integer, List<Double>>> pairs = Lists.newArrayList(outputQueue);
        pairs.sort(Comparator.comparing(Pair::getLeft));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pc.getOutputPath()))) {
            int previous = -1;
            for (Pair<Integer, List<Double>> pair : pairs) {
                int current = pair.getLeft();
                while (++previous < current) {
                    bw.write(previous + "\tN/A");
                    bw.newLine();
                }
                bw.write(current + "\t" + StringUtils.join(pair.getRight(), "\t"));
                bw.newLine();
            }
        }
        System.exit(0);
    }

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to profile prediction services, we support below configurations");
        log.info("");
        log.info("\t-h, print this message and exit");
        log.info("\t-f {file}, mandatory and must use absolute path of the file, file should be in text format");
        log.info("\t--first-line-schema, optional, if the first line of the input file is column names, default value is false");
        log.info("\t-s, optional, input data schema, could be json like '[{\"name\":\"col1\",\"type\":\"Int\"},{\"name\":\"col2\",\"type\":\"String\"}]' or an absolute file path that contains this json, if not present, schema will be pulled from /api/description of service");
        log.info("\t-d {delimiter}, optional, column delimiter of the input file, default value is '\\t'");
        log.info("\t--endpoint {endpoint}, mandatory, uri of the target service");
        log.info("\t-t {token}, mandatory, accessToken of the target service");
        log.info("\t-o {output}, optional, absolute output file path of prediction scores, default value is /tmp/score");
        log.info("\t-b {batch}, optional, how many lines of data to include in a single request, default value is 1");
        log.info("\t-c {concurrency}, optional, how many threads will be used to send requests in parallel, default value is 1");
        log.info("\t--async, optional, use async mode to profile if present");

        System.exit(0);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
