package person.dufei.utils.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public class ServiceMetrics {

    private final Date date;
    private final String service;
    private final String operation;
    private final String endpoint;
    private final String requestId;
    private final String status;
    private final Map<String, Long> counters;
    private final Map<String, String> metrics;
    private final List<Latency> latencies;

    public ServiceMetrics(final List<String> logLines) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // example: Time:2016-12-16 16:29:55.468
        this.date = sdf.parse(logLines.get(0).trim().substring(5).trim());
        // example: Service:predictor
        this.service = logLines.get(1).trim().substring(8).trim();
        // example: Operation:predict
        this.operation = logLines.get(2).trim().substring(10).trim();
        // example: Endpoint:predictor_predict_docker02:7711
        this.endpoint = logLines.get(3).trim().substring(9).trim();
        // example: RequestId:c4de56c4-9443-4e3f-8d3f-d8cdfc42c15c
        this.requestId = logLines.get(4).trim().substring(10).trim();
        // example: Status:OK
        this.status = logLines.get(5).trim().substring(7).trim();
        // example: Counters:{request.success=1, cannon.requests=1}
        this.counters = Maps.newHashMap();
        String countersStr = logLines.get(6).trim().substring(9).trim();
        if (countersStr.length() > 2) {
            String[] entries = countersStr.substring(1, countersStr.length() - 1).trim().split(",");
            for (String entry : entries) {
                String[] fields = entry.trim().split("=");
                counters.put(fields[0].trim(), Long.parseLong(fields[1].trim()));
            }
        }
        // example: Metrics:{cannon.hit=149, signature.total=2062}
        this.metrics = Maps.newHashMap();
        String metricsStr = logLines.get(7).trim().substring(8).trim();
        if (metricsStr.length() > 2) {
            String[] entries = metricsStr.substring(1, metricsStr.length() - 1).trim().split(",");
            for (String entry : entries) {
                String[] fields = entry.trim().split("=");
                metrics.put(fields[0].trim(), fields[1].trim());
            }
        }
        // example: Timers:[prediction.latency:129000 MICROSECONDS, fe.latency:67000 MICROSECONDS]
        this.latencies = Lists.newArrayList();
        String timersStr = logLines.get(8).trim().substring(7).trim();
        if (timersStr.length() > 2) {
            String[] entries = timersStr.substring(1, timersStr.length() - 1).trim().split(",");
            for (String entry : entries) {
                String[] fields = entry.trim().split(":");
                String[] subs = fields[1].trim().split(" ");
                latencies.add(new Latency(fields[0], Long.parseLong(subs[0].trim()), TimeUnit.valueOf(subs[1].trim())));
            }
        }
    }

    public long getCounter(String name) {
        return counters.get(name);
    }

    public String getMetric(String name) {
        return metrics.get(name);
    }

    public int getIntMetric(String name) {
        return Integer.parseInt(metrics.get(name));
    }

    public long getLongMetric(String name) {
        return Long.parseLong(metrics.get(name));
    }

    public double getDoubleMetric(String name) {
        return Double.parseDouble(metrics.get(name));
    }

    public boolean getBoolMetric(String name) {
        return Boolean.parseBoolean(metrics.get(name));
    }

}
