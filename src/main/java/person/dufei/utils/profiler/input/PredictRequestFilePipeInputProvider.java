package person.dufei.utils.profiler.input;

import com._4paradigm.predictor.PredictItem;
import com._4paradigm.predictor.PredictRequest;
import com._4paradigm.prophet.rest.pipe.io.PipeInputProvider;
import com._4paradigm.prophet.rest.pipe.io.impl.QueuePipeInputProvider;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static com._4paradigm.prophet.rest.utils.Serdes.serializeAsJsonBytes;
import static com._4paradigm.prophet.rest.utils.Validator.validateIntPositive;
import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

public class PredictRequestFilePipeInputProvider implements PipeInputProvider<HttpPost>, Runnable {

    private static final Map<String, String> EMPTY = ImmutableMap.of();

    private final String uri;
    private final int batchSize;
    private final String delimiter;
    private final boolean isFirstLineSchema;
    private final String accessToken;
    private final PipeInputProvider<HttpPost> internal;
    private final BufferedReader br;

    public PredictRequestFilePipeInputProvider(final String uri, final String fileName, final int batchSize, final String delimiter,
                                               final boolean isFirstLineSchema, final String accessToken) {
        validateStringNotBlank(uri, "predictor uri");
        validateStringNotBlank(fileName, "file name");
        validateIntPositive(batchSize, "batch size");
        validateStringNotBlank(delimiter, "file column delimiter");
        validateStringNotBlank(accessToken, "access token");
        this.uri = uri;
        this.batchSize = batchSize;
        this.delimiter = delimiter;
        this.isFirstLineSchema = isFirstLineSchema;
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
            String line = null;
            String[] columnNames;
            if (isFirstLineSchema) {
                columnNames = br.readLine().split(delimiter);
            } else {
                line = br.readLine();
                int count = line.split(delimiter).length;
                columnNames = IntStream.range(1, count+1).mapToObj(
                        i -> String.format("col_%d", i)
                ).toArray(String[]::new);
            }
            List<String> lines = Lists.newArrayListWithCapacity(batchSize);
            if (line != null) lines.add(line);
            int startIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (lines.size() >= batchSize) {
                    offer(convert(columnNames, lines, startIndex));
                    startIndex += batchSize;
                    lines.clear();
                }
                lines.add(line);
            }
            if (lines.size() > 0) offer(convert(columnNames, lines, startIndex));
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
    public void close() throws IOException {
        internal.close();
        if (br != null) br.close();
    }

    private HttpPost convert(String[] columnNames, List<String> lines, int startIndex) {
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
            Map<String, String> features = Maps.newHashMapWithExpectedSize(columnNames.length);
            String[] values = lines.get(i).split(delimiter, -1);
            for (int j = 0; j < columnNames.length; j++) features.put(columnNames[j], values[j]);
            pi.setRawFeatures(features);
            pris.add(pi);
        }
        HttpPost post = new HttpPost(uri);
        post.setEntity(new ByteArrayEntity(serializeAsJsonBytes(pr), ContentType.APPLICATION_JSON));
        return post;
    }
}
