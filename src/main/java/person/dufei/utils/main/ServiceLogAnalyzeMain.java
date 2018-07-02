package person.dufei.utils.main;

import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.analysis.ServiceLogAnalyzer;
import person.dufei.utils.analysis.filter.DateMetricsFilter;
import person.dufei.utils.analysis.filter.EndpointMetricsFilter;
import person.dufei.utils.analysis.filter.MetricsFilter;
import person.dufei.utils.analysis.filter.OperationMetricsFilter;
import person.dufei.utils.analysis.filter.ServiceMetricsFilter;
import person.dufei.utils.analysis.filter.StatusMetricsFilter;

import java.text.SimpleDateFormat;

import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

/**
 * Created by dufei on 16/12/19.
 */
@Slf4j
public class ServiceLogAnalyzeMain {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    public static void main(String[] args) throws Exception {
        helpIntercept();
        String logPath = System.getProperty("filePath");
        validateStringNotBlank(logPath, "log file path");
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

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to analyze service logs produced by https://code.4paradigm.com/projects/PHT3/repos/online-app/browse/metrics/src/main/java/com/_4paradigm/prophet/metrics/publisher/MetricsLogger.java?at=refs%2Fheads%2Frelease%2F3.1.0, we support below configurations");
        log.info("");
        log.info("\t-h, print this message and exit");
        log.info("\t-f {file}, mandatory and must use absolute path of the log file");
        log.info("\t--service {service}, optional, filter metrics by specified service");
        log.info("\t--operation {operation}, optional, filter metrics by specified operation");
        log.info("\t--endpoint {endpoint}, optional, filter metrics by specified endpoint");
        log.info("\t--status {status}, optional, filter metrics by specified status");
        log.info("\t--start {start date}, optional, filter metrics produced before specified date, format should be yyyy-MM-dd_HH:mm:ss");
        log.info("\t--end {end date}, optional, filter metrics produced after specified date, format should be yyyy-MM-dd_HH:mm:ss");

        System.exit(0);
    }

}
