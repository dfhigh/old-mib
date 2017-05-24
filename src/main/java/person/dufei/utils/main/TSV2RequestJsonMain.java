package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import person.dufei.utils.convert.LineConverter;
import person.dufei.utils.convert.PredictorTsvLineConverter;
import person.dufei.utils.profiler.input.FileInputProvider;
import person.dufei.utils.profiler.input.InputProvider;

@Slf4j
public class TSV2RequestJsonMain {

    private static final ObjectMapper OM = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String tsvFilePath = System.getProperty("tsvPath");
        String accessToken = System.getProperty("accessToken");
        String batchStr = System.getProperty("batchSize");
        String delimiter = System.getProperty("delimiter");
        String schema = System.getProperty("firstLineSchema");
        validateString(tsvFilePath);
        validateString(accessToken);

        int batchSize = StringUtils.isBlank(batchStr) ? 1 : Integer.parseInt(batchStr);
        delimiter = StringUtils.isBlank(delimiter) ? "\t" : delimiter;
        boolean isFirstLineSchema = StringUtils.isNotBlank(schema) && Boolean.parseBoolean(schema);
        LineConverter<PredictorRequest> lc = new PredictorTsvLineConverter();
        InputProvider<PredictorRequest> ip = new FileInputProvider<>(tsvFilePath, lc, delimiter, isFirstLineSchema, batchSize, accessToken);
        int threshold = 0;
        while (true) {
            PredictorRequest pr = ip.getInputQueue().poll();
            if (pr == null) {
                threshold++;
                if (threshold >= 3) break;
                sleepQuietly(1000);
                continue;
            }
            threshold = 0;
            log.info("{}", OM.writeValueAsString(pr));
        }
    }

    private static void validateString(String parameter) {
        if (StringUtils.isBlank(parameter)) {
            throw new IllegalArgumentException();
        }
    }

    private static void sleepQuietly(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // impossible
        }
    }

}
