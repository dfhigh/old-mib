package person.dufei.utils.profiler.input;

import com._4paradigm.predictor.PredictItem;
import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.impl.QueuePipeInputProvider;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import person.dufei.utils.profiler.config.Schema;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com._4paradigm.prophet.rest.utils.Serdes.serializeAsJsonBytes;
import static com._4paradigm.prophet.rest.utils.Validator.validateIntPositive;
import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

public class PredictRequestFilePipeInputProvider implements PipeInputProvider<HttpPost>, Runnable {

    private static final Map<String, Object> EMPTY = ImmutableMap.of();
    private static final SimpleDateFormat DSDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String uri;
    private final int batchSize;
    private final String delimiter;
    private final boolean isFirstLineSchema;
    private final Schema[] schemas;
    private final String accessToken;
    private final PipeInputProvider<HttpPost> internal;
    private final BufferedReader br;

    public PredictRequestFilePipeInputProvider(final String uri, final String fileName, final int batchSize, final String delimiter,
                                               final boolean isFirstLineSchema, final Schema[] schemas, final String accessToken) {
        validateStringNotBlank(uri, "predictor uri");
        validateStringNotBlank(fileName, "file name");
        validateIntPositive(batchSize, "batch size");
        validateStringNotBlank(accessToken, "access token");
        validateObjectNotNull(schemas, "schema array");
        this.uri = uri;
        this.batchSize = batchSize;
        this.delimiter = delimiter;
        this.isFirstLineSchema = isFirstLineSchema;
        this.schemas = schemas;
        this.accessToken = accessToken;
        this.internal = new QueuePipeInputProvider<>(new DisruptorBlockingQueue<>(512));
        try {
            this.br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        try (BufferedReader reader = br) {
            if (isFirstLineSchema) {
                br.readLine();
            }
            List<String> lines = Lists.newArrayListWithCapacity(batchSize);
            int startIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (lines.size() >= batchSize) {
                    offer(convert(schemas, lines, startIndex));
                    startIndex += batchSize;
                    lines.clear();
                }
                lines.add(line);
            }
            if (lines.size() > 0) offer(convert(schemas, lines, startIndex));
            internal.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void offer(HttpPost payload) {
        internal.offer(payload);
    }

    @Override
    public HttpPost take() {
        return internal.take();
    }

    @Override
    public boolean isClosed() {
        return internal.isClosed();
    }

    @Override
    public long provided() {
        return internal.provided();
    }

    @Override
    public void close() throws IOException {
        internal.close();
        if (br != null) br.close();
    }

    private HttpPost convert(Schema[] schemas, List<String> lines, int startIndex) {
        PredictRequest pr = new PredictRequest();
        pr.setRequestId(UUID.randomUUID().toString());
        pr.setResultLimit(batchSize);
        pr.setAccessToken(accessToken);
        pr.setCommonFeatures(EMPTY);
        List<PredictItem> pris = Lists.newArrayListWithCapacity(batchSize);
        pr.setRawInstances(pris);
        for (int i = 0; i < batchSize; i++) {
            PredictItem pi = new PredictItem();
            pi.setId(String.valueOf(startIndex + i));
            Map<String, Object> features = Maps.newHashMapWithExpectedSize(schemas.length);
            String[] values = lines.get(i).split(delimiter, -1);
            for (int j = 0; j < schemas.length; j++) features.put(schemas[j].getName(), subConvert(schemas[j], values[j]));
            pi.setRawFeatures(features);
            pris.add(pi);
        }
        HttpPost post = new HttpPost(uri);
        post.setEntity(new ByteArrayEntity(serializeAsJsonBytes(pr), ContentType.APPLICATION_JSON));
        return post;
    }

    private Object subConvert(Schema schema, String value) {
        validateObjectNotNull(schema, "schema");
        switch (schema.getType().toLowerCase()) {
            case "string":
                return value;
            case "int":
            case "integer":
                return StringUtils.isEmpty(value) ? 0 : Integer.valueOf(value);
            case "bigint":
            case "long":
                return StringUtils.isEmpty(value) ? 0L : Long.valueOf(value);
            case "double":
                return StringUtils.isEmpty(value) ? 0.0d : Double.valueOf(value);
            case "float":
                return StringUtils.isEmpty(value) ? 0 : Float.valueOf(value);
            case "smallint":
            case "short":
                return StringUtils.isEmpty(value) ? 0 : Short.valueOf(value);
            case "byte":
                return StringUtils.isEmpty(value) ? 0 : Byte.valueOf(value);
            case "boolean":
            case "bool":
                return StringUtils.isEmpty(value) ? false : Boolean.valueOf(value);
            case "date":
                try {
                    return DSDF.parse(value).getTime();
                } catch (Exception e) {
                    return 0L;
                }
            case "timestamp":
                try {
                    return TSDF.parse(value).getTime();
                } catch (Exception e) {
                    return 0L;
                }
            default:
                throw new IllegalArgumentException("unknown schema type " + schema.getType());
        }
    }
}
