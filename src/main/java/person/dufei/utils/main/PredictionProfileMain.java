package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import person.dufei.utils.convert.PredictorTsvLineConverter;
import person.dufei.utils.profiler.PredictionProfiler;
import person.dufei.utils.profiler.SimpleProfiler;
import person.dufei.utils.profiler.config.ProfileConfig;
import person.dufei.utils.profiler.input.FileInputProvider;
import person.dufei.utils.profiler.input.InputProvider;

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
        InputProvider<PredictorRequest> ip = new FileInputProvider<>(tsv, new PredictorTsvLineConverter(), pc);
        SimpleProfiler<PredictorRequest, String> profiler = new PredictionProfiler();
        profiler.profile(ip, pc);
        while (true) {
            log.info("profile duration: {}, requests sent: {}, waiting requests: {}, 200: {}, 500: {}, tp50: {}, tp90: {}, tp99: {}, tp999: {}",
                    profiler.getDurationMilli(),
                    profiler.getRequestsSent(),
                    ip.getInputQueue().size(),
                    profiler.get200s(),
                    profiler.get500s(),
                    profiler.getP50Milli(),
                    profiler.getP90Milli(),
                    profiler.getP99Milli(),
                    profiler.getP999Milli());
            Thread.sleep(3000);
        }
    }

}
