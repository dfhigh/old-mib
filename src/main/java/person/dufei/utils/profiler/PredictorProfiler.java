package person.dufei.utils.profiler;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import person.dufei.utils.convert.PredictorTSV2RequestJsonConverter;
import person.dufei.utils.http.HttpJsonPostDataProvider;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PredictorProfiler {

    private static final ObjectMapper OM = new ObjectMapper();

    private static volatile boolean stop = false;

    public static List<Double> getScores(String url, String tsvFile, String schema, String token, int concurrency) throws Exception {
        List<PredictorRequest> requests = PredictorTSV2RequestJsonConverter.convert(tsvFile, schema, token, 400);
        int size = requests.size();
        for (int i = 0; i < size; i++) {
            requests.get(i).setRequestId(String.valueOf(i));
        }
        BlockingQueue<PredictorRequest> input = new ArrayBlockingQueue<>(size, false, requests);
        PredictorResponse[] responses = new PredictorResponse[size];
        ExecutorService es = Executors.newFixedThreadPool(concurrency);
        for (int i = 0; i < concurrency; i++) {
            es.submit(() -> {
                while (!stop) {
                    PredictorRequest req = input.poll();
                    if (req != null) {
                        try {
                            String json = OM.writeValueAsString(req);
                            PredictorResponse res = OM.readValue(HttpJsonPostDataProvider.getData(url, json, ContentType.TEXT_PLAIN), PredictorResponse.class);
                            responses[Integer.parseInt(req.getRequestId())] = res;
                        } catch (Exception e) {
                            log.error("exception of http", e);
                            responses[Integer.parseInt(req.getRequestId())] = null;
                        }
                    }
                }
            });
        }
        while (!input.isEmpty());
        stop = true;
        Thread.sleep(1000);
        List<Double> scores = Lists.newArrayList();
        for (PredictorResponse response : responses) {
            if (response != null) {
                response.getInstances().forEach((item) -> scores.add(item.getScore()));
            } else {
                for (int i = 0; i < 400; i++) scores.add(0.0d);
            }
        }
        es.shutdown();
        return scores;
    }

}
