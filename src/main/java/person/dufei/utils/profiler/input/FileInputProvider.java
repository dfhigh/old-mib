package person.dufei.utils.profiler.input;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.convert.LineConverter;
import person.dufei.utils.profiler.config.ProfileConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.Objects.requireNonNull;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class FileInputProvider<I> implements InputProvider<I> {

    private static final int QUEUE_SIZE = 500;

    private final String fileName;
    private final int batchSize;
    private final LineConverter<I> lc;
    private final BlockingQueue<I> queue;
    private final ProfileConfig pc;

    public FileInputProvider(final String fileName, final LineConverter<I> lc, final ProfileConfig pc) {
        this.fileName = requireNonNull(fileName);
        this.batchSize = Math.max(1, pc.getBatchSize());
        this.lc = requireNonNull(lc);
        this.pc = requireNonNull(pc);
        this.queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        new Thread(this::populate).start();
    }

    @Override
    public BlockingQueue<I> getInputQueue() {
        return queue;
    }

    private void populate() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            String head = br.readLine();
            List<String> lines = Lists.newArrayListWithCapacity(batchSize+1);
            while ((line = br.readLine()) != null) {
                if (lines.size() >= batchSize+1) {
                    queue.put(lc.convert(lines, pc));
                    lines.clear();
                    lines.add(head);
                }
                lines.add(line);
            }
        } catch (Exception e) {
            log.error("caught exception when opening {} for read...", fileName, e);
            throw new RuntimeException(e);
        }
    }

}
