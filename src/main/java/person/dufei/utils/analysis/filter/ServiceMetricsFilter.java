package person.dufei.utils.analysis.filter;

import person.dufei.utils.data.ServiceMetrics;

import static java.util.Objects.requireNonNull;

public class ServiceMetricsFilter implements MetricsFilter {

    private final String expectedService;

    public ServiceMetricsFilter(final String expectedService) {
        this.expectedService = requireNonNull(expectedService);
    }

    @Override
    public boolean apply(ServiceMetrics metrics) {
        return expectedService.equals(metrics.getService());
    }

}
