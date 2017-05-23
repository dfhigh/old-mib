package person.dufei.utils.profiler.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by dufei on 17/3/4.
 */
@Data
public class ProfileConfig {

    private int concurrency = 1;
    private int batchSize = 1;
    private String url;
    private String accessToken = "";
    private long sleep = 0;
    private String outputPath = "/tmp/score";
    private String arch = "http";

    private ProfileConfig() {}

    public static ProfileConfig fromEnv() {
        ProfileConfig pc = new ProfileConfig();
        String concurrency = System.getProperty("concurrency");
        if (StringUtils.isNumeric(concurrency)) pc.setConcurrency(Integer.parseInt(concurrency));
        String batchSize = System.getProperty("batchSize");
        if (StringUtils.isNumeric(batchSize)) pc.setBatchSize(Integer.parseInt(batchSize));
        String url = System.getProperty("endpoint");
        if (StringUtils.isBlank(url)) throw new IllegalArgumentException("url can't be blank");
        pc.setUrl(url);
        String accessToken = System.getProperty("accessToken");
        if (!StringUtils.isBlank(accessToken)) pc.setAccessToken(accessToken);
        String sleep = System.getProperty("sleep");
        if (StringUtils.isNumeric(sleep)) pc.setSleep(Long.parseLong(sleep));
        String outputPath = System.getProperty("outputPath");
        if (StringUtils.isNotBlank(outputPath)) pc.setOutputPath(outputPath);
        String arch = System.getProperty("arch");
        if (StringUtils.isNotBlank(arch)) pc.setArch(arch);
        return pc;
    }

}
