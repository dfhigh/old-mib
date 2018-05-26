package person.dufei.utils.main;

import com._4paradigm.predictor.PredictResponse;
import com._4paradigm.predictor.Status;
import com._4paradigm.prophet.rest.client.AsyncHttpOperator;
import com._4paradigm.prophet.rest.client.callback.JsonHttpResponseHandler;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
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
        String tsv = System.getProperty("tsvPath");
        if (StringUtils.isBlank(tsv)) {
            throw new IllegalArgumentException("tsv path can't be blank");
        }
        AtomicLong succeeds = new AtomicLong(0);
        ProfileConfig pc = ProfileConfig.fromEnv();
        BlockingQueue<Pair<Integer, Double>> outputQueue = new LinkedBlockingQueue<>();
        PipeInputProvider<HttpPost> inputProvider = new PredictRequestFilePipeInputProvider(pc.getUrl(), tsv,
                pc.getBatchSize(), pc.getDelimiter(), pc.isFirstLineSchema(), pc.getAccessToken());
        SimpleProfiler profiler = new RestProfiler<>(new AsyncHttpOperator(16, 16), inputProvider,
            new JsonHttpResponseHandler<PredictResponse>(PredictResponse.class) {
                @Override
                protected void onSuccess(PredictResponse response) {
                    if (response.getStatus() == Status.OK) succeeds.incrementAndGet();
                    response.getInstances().forEach(item -> outputQueue.offer(Pair.of(Integer.parseInt(item.getId()), item.getScore())));
                }
            }, pc.getConcurrency()
        );
        profiler.start();
        long requestsSent = 0, threshold = 0;
        while (true) {
            long real = profiler.getRequestsCompleted();
            SimpleProfiler.LatencyStats ls = profiler.getLatencyStats();
            log.info("start duration: {}, requests sent: {}, 200: {}, tp50: {}, tp90: {}, tp99: {}, tp999: {}",
                    profiler.getDurationMilli(),
                    real,
                    succeeds.get(),
                    ls.getTp50(),
                    ls.getTp90(),
                    ls.getTp99(),
                    ls.getTp999());
            if (requestsSent == real) {
                threshold++;
                if (threshold >= 3 && inputProvider.isClosed()) break;
            } else {
                requestsSent = real;
                threshold = 0;
            }
            Thread.sleep(3000);
        }
        List<Pair<Integer, Double>> pairs = Lists.newArrayList(outputQueue);
        pairs.sort(Comparator.comparing(Pair::getLeft));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pc.getOutputPath()))) {
            for (Pair<Integer, Double> pair : pairs) {
                bw.write(pair.getLeft() + "\t" + pair.getRight());
                bw.newLine();
            }
        }
        System.exit(0);
    }

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to start prediction services, we support below configurations");
        log.info("");
        log.info("\t-h, print this message and exit");
        log.info("\t-f {file}, mandatory and must use absolute path of the file, file should be in text format");
        log.info("\t--first-line-schema, optional, if the first line of the input file is column names, default value is false, if it's false, column names will be generated in format of 'col_{index}'");
        log.info("\t-d {delimiter}, optional, column delimiter of the input file, default value is '\\t'");
        log.info("\t--endpoint {endpoint}, mandatory, uri of the target service");
        log.info("\t-t {token}, mandatory, accessToken of the target service");
        log.info("\t-o {output}, optional, absolute output file path of prediction scores, default value is /tmp/score");
        log.info("\t-b {batch}, optional, how many lines of data to include in a single request, default value is 1");
        log.info("\t-c {concurrency}, optional, how many threads will be used to send requests in parallel, default value is 1");

        System.exit(0);
    }

}
