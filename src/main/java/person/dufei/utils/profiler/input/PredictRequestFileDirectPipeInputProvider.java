package person.dufei.utils.profiler.input;

import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.utils.Schema;

import java.util.List;

public class PredictRequestFileDirectPipeInputProvider extends PredictRequestFilePipeInputProvider<PredictRequest> {

    public PredictRequestFileDirectPipeInputProvider(String uri, String fileName, int batchSize, String delimiter,
                                                     boolean isFirstLineSchema, List<Schema> schemas, String accessToken) {
        super(uri, fileName, batchSize, delimiter, isFirstLineSchema, schemas, accessToken);
    }

    @Override
    PredictRequest convert(List<Schema> schemas, List<String> lines, int startIndex) {
        return convert2PR(schemas, lines, startIndex);
    }
}
