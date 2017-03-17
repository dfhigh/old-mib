package person.dufei.utils.analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.data.Latency;
import person.dufei.utils.data.ServiceMetrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LatencyAnalyzer {

    public static void analyze(List<ServiceMetrics> metrics) throws Exception {
        if (metrics.isEmpty()) return;
        Map<String, List<Long>> latencies = Maps.newHashMap();
        for (ServiceMetrics sm : metrics) {
            List<Latency> requestTimers = sm.getLatencies();
            for (Latency latency : requestTimers) {
                List<Long> existed = latencies.get(latency.getName());
                if (existed == null) {
                    existed = Lists.newArrayList();
                    latencies.put(latency.getName(), existed);
                }
                existed.add(TimeUnit.MILLISECONDS.convert(latency.getDuration(), latency.getTu()));
            }
        }
        latencies.values().parallelStream().forEach(Collections::sort);
        int size = metrics.size();
        double duration = (double) (metrics.get(size-1).getDate().getTime()-metrics.get(0).getDate().getTime()) / 1000.0;
        log.info("timer distribution on {} requests in {} seconds:", size, duration);
        log.info("name\t\t\t\tmin\tmax\tp50\tp90\tp99\tp999");
        log.info("====================================================================================");
        for (Map.Entry<String, List<Long>> entry : latencies.entrySet()) {
            String name = entry.getKey();
            if (name.length() < 16) name += "\t";
            List<Long> durations = entry.getValue();
            int count = durations.size();
            log.info("{}\t\t{}\t{}\t{}\t{}\t{}\t{}", name, durations.get(0), durations.get(count-1),
                    durations.get(count/2), durations.get(count*9/10), durations.get(count*99/100), durations.get(count*999/1000));
        }
        log.info("----------------------------------------------------------------------------------------------");
    }

}
