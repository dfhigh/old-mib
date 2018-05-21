package person.dufei.utils.main;

import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import person.dufei.utils.profiler.input.PredictRequestFilePipeInputProvider;

import java.nio.charset.StandardCharsets;

import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

@Slf4j
public class TSV2RequestJsonMain {

    public static void main(String[] args) throws Exception {
        String tsvFilePath = System.getProperty("tsvPath");
        String accessToken = System.getProperty("accessToken");
        String batchStr = System.getProperty("batchSize");
        String delimiter = System.getProperty("delimiter");
        String schema = System.getProperty("firstLineSchema");
        validateStringNotBlank(tsvFilePath, "input file path");
        validateStringNotBlank(accessToken, "access token");

        int batchSize = StringUtils.isBlank(batchStr) ? 1 : Integer.parseInt(batchStr);
        delimiter = StringUtils.isBlank(delimiter) ? "\t" : delimiter;
        boolean isFirstLineSchema = StringUtils.isNotBlank(schema) && Boolean.parseBoolean(schema);
        PipeInputProvider<HttpPost> inputProvider = new PredictRequestFilePipeInputProvider("http://localhost", tsvFilePath, batchSize, delimiter, isFirstLineSchema, accessToken);
        int threshold = 0;
        while (true) {
            HttpPost post = inputProvider.take();
            if (post == null && inputProvider.isClosed()) {
                threshold++;
                if (threshold >= 3) break;
            }
            threshold = 0;
            if (post == null) continue;
            log.info("{}", EntityUtils.toString(post.getEntity(), StandardCharsets.UTF_8));
        }
    }

}
