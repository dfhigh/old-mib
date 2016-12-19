package person.dufei.utils.analysis.filter;

import lombok.NonNull;
import person.dufei.utils.data.ServiceMetrics;

import static java.util.Objects.requireNonNull;

public class StatusMetricsFilter implements MetricsFilter {

    private final String expectedStatus;

    public StatusMetricsFilter(final String expectedStatus) {
        this.expectedStatus = requireNonNull(expectedStatus);
    }

    @Override
    public boolean apply(@NonNull ServiceMetrics metrics) {
        return expectedStatus.equals(metrics.getStatus());
    }

}
