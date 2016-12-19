package person.dufei.utils.analysis.filter;

import person.dufei.utils.data.ServiceMetrics;

public class DefaultMetricsFilter implements MetricsFilter {

    @Override
    public boolean apply(ServiceMetrics metrics) {
        return true;
    }

    @Override
    public MetricsFilter and(MetricsFilter filter) {
        return filter;
    }

}
