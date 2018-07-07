package person.dufei.utils.profiler.input;

import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.utils.Schema;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import java.util.List;

import static com._4paradigm.prophet.rest.utils.Serdes.serializeAsJsonBytes;

public class PredictRequestFileRestPipeInputProvider extends PredictRequestFilePipeInputProvider<HttpPost> {

    public PredictRequestFileRestPipeInputProvider(String uri, String fileName, int batchSize, String delimiter,
                                                   boolean isFirstLineSchema, List<Schema> schemas, String accessToken) {
        super(uri, fileName, batchSize, delimiter, isFirstLineSchema, schemas, accessToken);
    }

    @Override
    HttpPost convert(List<Schema> schemas, List<String> lines, int startIndex) {
        PredictRequest pr = convert2PR(schemas, lines, startIndex);
        HttpPost post = new HttpPost(uri);
        post.setEntity(new ByteArrayEntity(serializeAsJsonBytes(pr), ContentType.APPLICATION_JSON));
        return post;
    }
}
