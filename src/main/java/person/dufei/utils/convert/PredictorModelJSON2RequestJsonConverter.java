package person.dufei.utils.convert;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorRequestItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.data.Metadata;
import person.dufei.utils.data.ModelDescriptor;
import person.dufei.utils.data.Schema;
import person.dufei.utils.http.HttpGetDataProvider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PredictorModelJSON2RequestJsonConverter {

    private static final ObjectMapper OM = new ObjectMapper();

    public static PredictorRequest convert(String modelUrl, String accessToken) throws Exception {
        String responseJson = HttpGetDataProvider.getData(modelUrl);
        ModelDescriptor md = OM.readValue(responseJson, ModelDescriptor.class);
        String meta = md.getMeta();
        if (meta == null || meta.isEmpty()) {
            throw new RuntimeException("empty metadata");
        }
        Metadata metadata = OM.readValue(meta, Metadata.class);
        PredictorRequest request = new PredictorRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setAccessToken(accessToken);
        request.setResultLimit(10);
        request.setCommonFeatures(Maps.newHashMap());
        List<List<String>> data = metadata.getInputData().getPreview();
        List<Schema> schemas = metadata.getInputData().getSchema();
        List<PredictorRequestItem> items = Lists.newArrayListWithCapacity(data.size());
        for (int i = 0; i < data.size(); i++) {
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
