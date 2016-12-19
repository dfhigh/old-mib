package person.dufei.utils.analysis.filter;

import lombok.NonNull;
import person.dufei.utils.data.ServiceMetrics;

public class DateMetricsFilter implements MetricsFilter {

    private final long start;
    private final long end;

    public DateMetricsFilter(final long start, final long end) {
        if (start <= 0 || end <= 0 || end < start) {
            throw new IllegalArgumentException("invalid start " + start + " and end " + end);
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean apply(@NonNull ServiceMetrics metrics) {
        long time = metrics.getDate().getTime();
        return start <= time && time <= end;
    }

}
