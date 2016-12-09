package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.convert.PredictorModelJSON2RequestJsonConverter;

@Slf4j
public class Model2RequestJsonMain {

    private static final ObjectMapper OM = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String url = System.getProperty("modelUrl");
        String accessToken = System.getProperty("accessToken");
        if (url == null || accessToken == null) {
            throw new IllegalArgumentException();
        }
        PredictorRequest request = PredictorModelJSON2RequestJsonConverter.convert(url, accessToken);
        log.info("{}", OM.writeValueAsString(request));
    }

}
