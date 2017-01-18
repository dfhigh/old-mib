package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.convert.PredictorTSV2RequestJsonConverter;

import java.util.List;

@Slf4j
public class TSV2RequestJsonMain {

    private static final ObjectMapper OM = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String tsvFilePath = System.getProperty("tsvPath");
        String accessToken = System.getProperty("accessToken");
        String schemaJsonPath = System.getProperty("schemaJson");
        String batchStr = System.getProperty("batchSize");
        validateString(tsvFilePath);
        validateString(accessToken);
        validateString(schemaJsonPath);

        if (batchStr == null || batchStr.isEmpty()) {
            PredictorRequest request = PredictorTSV2RequestJsonConverter.convert(tsvFilePath, schemaJsonPath, accessToken);
            log.info("{}", OM.writeValueAsString(request));
        } else {
            List<PredictorRequest> requests = PredictorTSV2RequestJsonConverter.convert(tsvFilePath, schemaJsonPath, accessToken, Integer.parseInt(batchStr));
            for (PredictorRequest request : requests) {
                log.info("{}", OM.writeValueAsString(request));
            }
        }
    }

    private static void validateString(String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException(parameter);
        }
    }

}
