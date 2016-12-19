package person.dufei.utils.analysis.filter;

import lombok.NonNull;
import person.dufei.utils.data.ServiceMetrics;

import static java.util.Objects.requireNonNull;

public class EndpointMetricsFilter implements MetricsFilter {

    private final String expectedEndpoint;

    public EndpointMetricsFilter(final String expectedEndpoint) {
        this.expectedEndpoint = requireNonNull(expectedEndpoint);
    }

    @Override
    public boolean apply(@NonNull ServiceMetrics metrics) {
        return expectedEndpoint.equals(metrics.getEndpoint());
    }

}
