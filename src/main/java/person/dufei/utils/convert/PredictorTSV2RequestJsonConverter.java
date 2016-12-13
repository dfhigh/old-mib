package person.dufei.utils.convert;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorRequestItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.data.Schema;
import person.dufei.utils.io.file.FileReadUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PredictorTSV2RequestJsonConverter {

    public static PredictorRequest convert(String tsvFile, String schemaJsonFile, String accessToken) throws Exception {
        List<List<String>> data = FileReadUtils.getFields(tsvFile, "\t");
        List<Schema> schemas = FileReadUtils.getDeserializedContent(schemaJsonFile, new TypeReference<List<Schema>>(){});
        return subConvert(data, schemas, accessToken);
    }

    public static List<PredictorRequest> convert(String tsvFile, String schemaJsonFile, String accessToken, int batchSize) throws Exception{
        List<List<String>> data = FileReadUtils.getFields(tsvFile, "\t");
        List<Schema> schemas = FileReadUtils.getDeserializedContent(schemaJsonFile, new TypeReference<List<Schema>>(){});
        List<PredictorRequest> requests = Lists.newArrayList();
        int size = data.size(), start = 0;
        while (start + batchSize <= size) {
            requests.add(subConvert(data.subList(start, start+batchSize), schemas, accessToken));
            start += batchSize;
        }
        if (start < size) {
            requests.add(subConvert(data.subList(start, size), schemas, accessToken));
        }
        return requests;
    }

    private static PredictorRequest subConvert(List<List<String>> data, List<Schema> schemas, String accessToken) {
        int size = data.size();
        PredictorRequest request = new PredictorRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setAccessToken(accessToken);
        request.setResultLimit(size);
        request.setCommonFeatures(Maps.newHashMap());
        List<PredictorRequestItem> items = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            PredictorRequestItem item = new PredictorRequestItem();
            item.setId(String.valueOf(i));
            Map<String, String> map = Maps.newHashMapWithExpectedSize(schemas.size());
            List<String> row = data.get(i);
            for (int j = 0; j < schemas.size(); j++) {
                map.put(schemas.get(j).getName(), row.get(j));
            }
            item.setRawFeatures(map);
            items.add(item);
        }
        request.setRawInstances(items);
        return request;
    }

}
