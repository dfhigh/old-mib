package person.dufei.utils.analysis;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.data.ServiceMetrics;

import java.util.List;
import java.util.Map;

@Slf4j
public class CounterAnalyzer {

    public static void analyze(List<ServiceMetrics> metrics) {
        if (metrics.isEmpty()) return;
        Map<String, Long> sum = Maps.newHashMap();
        for (ServiceMetrics sm : metrics) {
            Map<String, Long> requestCounter = sm.getCounters();
            for (Map.Entry<String, Long> entry : requestCounter.entrySet()) {
                String key = entry.getKey();
                long value = entry.getValue();
                sum.put(key, (sum.containsKey(key) ? sum.get(key) + value : value));
            }
        }
        int size = metrics.size();
        double duration = (double) (metrics.get(size-1).getDate().getTime()-metrics.get(0).getDate().getTime()) / 1000.0;
        log.info("counter distribution on {} requests in {} seconds:", size, duration);
        log.info("name\t\t\ttotal\t\taverage\t\ttps");
        log.info("==================================================================================");
        for (Map.Entry<String, Long> entry : sum.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();
            log.info("{}\t\t{}\t\t{}\t\t{}", key, value, (double) value/size, value/duration);
        }
        log.info("----------------------------------------------------------------------------------------------");
    }

}
