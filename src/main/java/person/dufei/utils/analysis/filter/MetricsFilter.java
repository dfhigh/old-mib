package person.dufei.utils.analysis.filter;

import lombok.NonNull;
import person.dufei.utils.data.ServiceMetrics;

@FunctionalInterface
public interface MetricsFilter {

    MetricsFilter DEFAULT = new DefaultMetricsFilter();

    boolean apply(@NonNull ServiceMetrics metrics);

    default MetricsFilter and(@NonNull MetricsFilter filter) {
        return metrics -> apply(metrics) && filter.apply(metrics);
    }

}
