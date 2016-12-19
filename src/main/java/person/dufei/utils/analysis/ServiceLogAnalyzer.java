package person.dufei.utils.analysis;

import com.google.common.collect.Lists;
import person.dufei.utils.analysis.filter.MetricsFilter;
import person.dufei.utils.data.ServiceMetrics;
import person.dufei.utils.io.file.FileReadUtils;

import java.util.List;

public class ServiceLogAnalyzer {

    public static void analyze(String logPath, MetricsFilter filter) throws Exception {
        List<String> lines = FileReadUtils.getLines(logPath);
        List<ServiceMetrics> metrics = Lists.newArrayList();
        List<String> block = Lists.newArrayList();
        for (String line : lines) {
            if (line.trim().startsWith("----")) {
                ServiceMetrics sm = new ServiceMetrics(block);
                block.clear();
                if (filter.apply(sm)) metrics.add(sm);
            } else {
                block.add(line.trim());
            }
        }
        if (!block.isEmpty()) {
            ServiceMetrics sm = new ServiceMetrics(block);
            if (filter.apply(sm)) metrics.add(sm);
        }
        CounterAnalyzer.analyze(metrics);
        LatencyAnalyzer.analyze(metrics);
    }

}
