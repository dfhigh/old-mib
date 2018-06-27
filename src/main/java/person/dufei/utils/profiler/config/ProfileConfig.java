package person.dufei.utils.profiler.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static com._4paradigm.prophet.rest.utils.Serdes.deserializeFromJson;
import static person.dufei.utils.io.file.FileReadUtils.getContent;

/**
 * Created by dufei on 17/3/4.
 */
@Data
public class ProfileConfig {

    private static final Pattern JSON_PATTERN = Pattern.compile("^\\s*[\\[|{].*[]|}]\\s*$");

    private int concurrency = 1;
    private int batchSize = 1;
    private String url;
    private String accessToken = "";
    private long sleep = 0;
    private String outputPath = "/tmp/score";
    private String arch = "http";
    private boolean firstLineSchema = false;
    private Schema[] schemas;
    private boolean forever = false;
    private boolean async = false;
    private String delimiter = "\t";

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
        String fls = System.getProperty("firstLineSchema");
        if (StringUtils.isNotBlank(fls)) pc.setFirstLineSchema(Boolean.parseBoolean(fls));
        String schemaJson = System.getProperty("schemaJson");
        if (StringUtils.isNotBlank(schemaJson)) {
            if (!JSON_PATTERN.matcher(schemaJson).matches()) {
                schemaJson = getContent(schemaJson);
            }
            pc.setSchemas(deserializeFromJson(schemaJson, Schema[].class));
        }
        String delimiter = System.getProperty("delimiter");
        if (StringUtils.isNotBlank(delimiter)) pc.setDelimiter(delimiter);
        String forever = System.getProperty("forever");
        if (StringUtils.isNotBlank(forever)) pc.forever = Boolean.parseBoolean(forever);
        String async = System.getProperty("async");
        if (StringUtils.isNotBlank(async)) pc.async = Boolean.parseBoolean(async);
        return pc;
    }

}
