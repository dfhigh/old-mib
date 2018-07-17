package person.dufei.utils.profiler.input;

import com._4paradigm.predictor.PredictItem;
import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.predictor.utils.Schema;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.impl.QueuePipeInputProvider;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import static com._4paradigm.prophet.rest.utils.Validator.validateCollectionNotEmptyContainsNoNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateIntPositive;
import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

abstract class PredictRequestFilePipeInputProvider<T> implements PipeInputProvider<T>, Runnable {

    private static final SimpleDateFormat DSDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    final String uri;
    private final int batchSize;
    private final String delimiter;
    private final boolean isFirstLineSchema;
    private final List<Schema> schemas;
    private final String accessToken;
    private final PipeInputProvider<T> internal;
    private final BufferedReader br;

    public PredictRequestFilePipeInputProvider(final String uri, final String fileName, final int batchSize, final String delimiter,
                                               final boolean isFirstLineSchema, final List<Schema> schemas, final String accessToken) {
        validateStringNotBlank(uri, "predictor uri");
        validateStringNotBlank(fileName, "file name");
        validateIntPositive(batchSize, "batch size");
        validateStringNotBlank(accessToken, "access token");
        validateCollectionNotEmptyContainsNoNull(schemas, "schemas");
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
    public void offer(T payload) {
        internal.offer(payload);
    }

    @Override
    public T take() {
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

    abstract T convert(List<Schema> schemas, List<String> lines, int startIndex);

    PredictRequest convert2PR(List<Schema> schemas, List<String> lines, int startIndex) {
        PredictRequest pr = new PredictRequest();
        pr.setRequestId(UUID.randomUUID().toString());
        pr.setResultLimit(lines.size());
        pr.setAccessToken(accessToken);
        List<PredictItem> pris = Lists.newArrayListWithCapacity(lines.size());
        pr.setRawInstances(pris);
        for (int i = 0; i < lines.size(); i++) {
            PredictItem pi = new PredictItem();
            pi.setId(String.valueOf(startIndex + i));
            List<Object> features = Lists.newArrayListWithCapacity(schemas.size());
            String[] values = lines.get(i).split(delimiter, -1);
            for (int j = 0; j < schemas.size(); j++) features.add(subConvert(schemas.get(j), values[j]));
            pi.setRawFeaturesList(features);
            pris.add(pi);
        }
        return pr;
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
