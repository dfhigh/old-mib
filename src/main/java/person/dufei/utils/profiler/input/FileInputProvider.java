package person.dufei.utils.profiler.input;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import person.dufei.utils.convert.LineConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class FileInputProvider<I> implements InputProvider<I> {

    private static final int QUEUE_SIZE = 500;

    private final String fileName;
    private final int batchSize;
    private final String accessToken;
    private final String delimiter;
    private final boolean firstLineSchema;
    private final LineConverter<I> lc;
    private final BlockingQueue<I> queue;

    public FileInputProvider(final String fileName, final LineConverter<I> lc, final String delimiter,
                             final boolean firstLineSchema, final int batchSize, final String accessToken) {
        this.fileName = requireNonNull(fileName);
        this.batchSize = Math.max(1, batchSize);
        this.lc = requireNonNull(lc);
        this.delimiter = requireNonNull(delimiter);
        this.firstLineSchema = firstLineSchema;
        this.accessToken = requireNonNull(accessToken);
        this.queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        new Thread(this::populate).start();
    }

    @Override
    public BlockingQueue<I> getInputQueue() {
        return queue;
    }

    private void populate() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = null;
            String[] columnNames;
            if (firstLineSchema) {
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
            while ((line = br.readLine()) != null) {
                if (lines.size() >= batchSize) {
                    queue.put(lc.convert(columnNames, lines, delimiter, accessToken, startIndex));
                    startIndex += batchSize;
                    lines.clear();
                }
                lines.add(line);
            }
            if (lines.size() > 0) queue.put(lc.convert(columnNames, lines, delimiter, accessToken, startIndex));
        } catch (Exception e) {
            log.error("caught exception when opening {} for read...", fileName, e);
            throw new RuntimeException(e);
        }
    }

}
