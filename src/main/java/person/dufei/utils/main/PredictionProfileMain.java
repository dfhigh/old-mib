package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import person.dufei.utils.convert.PredictorTsvLineConverter;
import person.dufei.utils.profiler.HttpPredictionProfiler;
import person.dufei.utils.profiler.SimpleProfiler;
import person.dufei.utils.profiler.ThriftPredictionProfiler;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.FileInputProvider;
import person.dufei.utils.profiler.input.InputProvider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dufei on 17/3/6.
 */
@Slf4j
public class PredictionProfileMain {

    public static void main(String[] args) throws Exception {
        String tsv = System.getProperty("tsvPath");
        if (StringUtils.isBlank(tsv)) {
            throw new IllegalArgumentException("tsv path can't be blank");
        }
        ProfileConfig pc = ProfileConfig.fromEnv();
        InputProvider<PredictorRequest> ip = new FileInputProvider<>(tsv, new PredictorTsvLineConverter(),
                pc.getDelimiter(), pc.isFirstLineSchema(), pc.getBatchSize(), pc.getAccessToken());
        SimpleProfiler<PredictorRequest, Pair<Integer, Double>> profiler = getProfiler(pc);
        BlockingQueue<Pair<Integer, Double>> outputQueue = profiler.profile(ip, pc);
        long requestsSent = 0, threshold = 0;
        while (true) {
            long real = profiler.getRequestsSent();
            log.info("profile duration: {}, requests sent: {}, waiting requests: {}, 200: {}, 500: {}, tp50: {}, tp90: {}, tp99: {}, tp999: {}",
                    profiler.getDurationMilli(),
                    real,
                    ip.getInputQueue().size(),
                    profiler.get200s(),
                    profiler.get500s(),
                    profiler.getP50Milli(),
                    profiler.getP90Milli(),
                    profiler.getP99Milli(),
                    profiler.getP999Milli());
            if (requestsSent == real) {
                threshold++;
                if (threshold >= 3 && ip.getInputQueue().isEmpty()) break;
            } else {
                requestsSent = real;
                threshold = 0;
            }
            Thread.sleep(3000);
        }
        List<Pair<Integer, Double>> pairs = Lists.newArrayList(outputQueue);
        pairs.sort((pair1, pair2) -> pair1.getLeft().compareTo(pair2.getLeft()));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pc.getOutputPath()))) {
            for (Pair<Integer, Double> pair : pairs) {
                bw.write(pair.getLeft() + "\t" + pair.getRight());
                bw.newLine();
            }
        }
        System.exit(0);
    }

    private static SimpleProfiler<PredictorRequest, Pair<Integer, Double>> getProfiler(ProfileConfig pc) {
        switch (pc.getArch()) {
            case "http":
                return new HttpPredictionProfiler();
            case "thrift":
                return new ThriftPredictionProfiler();
            default:
                throw new IllegalArgumentException("unknown arch " + pc.getArch());
        }
    }

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to profile prediction services, we support below configurations");
        log.info("");
        log.info("\t\t-h, print this message and exit");
        log.info("\t\t-f {file}, mandatory and must use absolute path of the file, file should be in text format");
        log.info("\t\t--first-line-schema, optional, if the first line of the input file is column names, default value is false, if it's false, column names will be generated in format of 'col_{index}'");
        log.info("\t\t-d {delimiter}, optional, column delimiter of the input file, default value is '\t'");
        log.info("\t\t--endpoint {endpoint}, mandatory, uri of the target service");
        log.info("\t\t-t {token}, mandatory, accessToken of the target service");
        log.info("\t\t-o {output}, optional, absolute output file path of prediction scores, default value is /tmp/score");
        log.info("\t\t-b {batch}, optional, how many lines of data to include in a single request, default value is 1");
        log.info("\t\t-c {concurrency}, optional, how many threads will be used to send requests in parallel, default value is 1");

        System.exit(0);
    }

}
