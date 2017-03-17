package person.dufei.utils.main;

import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.profiler.PredictorProfiler;

import java.util.List;

@Slf4j
public class TSV2PredictionScoreMain {

    public static void main(String[] args) throws Exception {
        String asEndpoint = System.getProperty("asEndpoint");
        String tsvFilePath = System.getProperty("tsvPath");
        String accessToken = System.getProperty("accessToken");
        String schemaJsonPath = System.getProperty("schemaJson");
        String concurrencyStr = System.getProperty("concurrency");
        validateString(asEndpoint);
        validateString(tsvFilePath);
        validateString(accessToken);
        validateString(schemaJsonPath);
        validateString(concurrencyStr);
        int concurrency = Integer.parseInt(concurrencyStr);

        List<Double> scores = PredictorProfiler.getScores(asEndpoint, tsvFilePath, schemaJsonPath, accessToken, concurrency);
        scores.forEach(score -> log.info("{}", score));
    }

    private static void validateString(String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException(parameter);
        }
    }

}
