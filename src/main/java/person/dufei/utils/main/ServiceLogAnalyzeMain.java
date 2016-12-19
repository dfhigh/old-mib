package person.dufei.utils.main;

import person.dufei.utils.analysis.ServiceLogAnalyzer;
import person.dufei.utils.analysis.filter.DateMetricsFilter;
import person.dufei.utils.analysis.filter.EndpointMetricsFilter;
import person.dufei.utils.analysis.filter.MetricsFilter;
import person.dufei.utils.analysis.filter.OperationMetricsFilter;
import person.dufei.utils.analysis.filter.ServiceMetricsFilter;
import person.dufei.utils.analysis.filter.StatusMetricsFilter;

import java.text.SimpleDateFormat;

/**
 * Created by dufei on 16/12/19.
 */
public class ServiceLogAnalyzeMain {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    public static void main(String[] args) throws Exception {
        String logPath = System.getProperty("logPath");
        if (logPath == null || logPath.isEmpty()) throw new IllegalArgumentException();
        MetricsFilter mf = MetricsFilter.DEFAULT;
        String service = System.getProperty("service");
        if (service != null && !service.isEmpty()) mf = mf.and(new ServiceMetricsFilter(service));
        String operation = System.getProperty("operation");
        if (operation != null && !operation.isEmpty()) mf = mf.and(new OperationMetricsFilter(operation));
        String endpoint = System.getProperty("endpoint");
        if (endpoint != null && !endpoint.isEmpty()) mf = mf.and(new EndpointMetricsFilter(endpoint));
        String status = System.getProperty("status");
        if (status != null && !status.isEmpty()) mf = mf.and(new StatusMetricsFilter(status));
        String start = System.getProperty("startDate");
        String end = System.getProperty("endDate");
        if (start != null && !start.isEmpty()) {
            if (end != null && !end.isEmpty()) {
                mf = mf.and(new DateMetricsFilter(SDF.parse(start).getTime(), SDF.parse(end).getTime()));
            } else {
                mf = mf.and(new DateMetricsFilter(SDF.parse(start).getTime(), System.currentTimeMillis()));
            }
        } else if (end != null && !end.isEmpty()) {
            mf = mf.and(new DateMetricsFilter(0, SDF.parse(end).getTime()));
        }
        ServiceLogAnalyzer.analyze(logPath, mf);
    }

}
