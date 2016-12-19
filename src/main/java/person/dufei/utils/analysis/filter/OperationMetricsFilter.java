package person.dufei.utils.analysis.filter;

import lombok.NonNull;
import person.dufei.utils.data.ServiceMetrics;

import static java.util.Objects.requireNonNull;

public class OperationMetricsFilter implements MetricsFilter {

    private final String expectedOperation;

    public OperationMetricsFilter(final String expectedOperation) {
        this.expectedOperation = requireNonNull(expectedOperation);
    }

    @Override
    public boolean apply(@NonNull ServiceMetrics metrics) {
        return expectedOperation.equals(metrics.getOperation());
    }

}
