package person.dufei.utils.convert;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorRequestItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.profiler.config.ProfileConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class PredictorTsvLineConverter implements LineConverter<PredictorRequest> {

    @Override
    public PredictorRequest convert(String[] columnNames, List<String> lines, String delimiter, String accessToken, int startIndex) {
        int size = lines.size();
        PredictorRequest pr = new PredictorRequest();
        pr.setRequestId(UUID.randomUUID().toString());
        pr.setResultLimit(size);
        pr.setAccessToken(accessToken);
        pr.setCommonFeatures(Maps.newHashMap());
        List<PredictorRequestItem> pris = Lists.newArrayListWithCapacity(size);
        pr.setRawInstances(pris);
        for (int i = 0; i < size; i++) {
            PredictorRequestItem pri = new PredictorRequestItem();
            pri.setId(String.valueOf(startIndex + i));
            Map<String, String> features = Maps.newHashMapWithExpectedSize(columnNames.length);
            String[] values = lines.get(i).split(delimiter, -1);
            for (int j = 0; j < columnNames.length; j++) features.put(columnNames[j], values[j]);
            pri.setRawFeatures(features);
            pris.add(pri);
        }
        return pr;
    }

}
