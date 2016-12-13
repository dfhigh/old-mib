package person.dufei.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class HttpGetDataProvider {

    private static final Charset UTF8 = Charset.forName("utf-8");

    public static String getData(String url) {
        try (CloseableHttpClient http = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = http.execute(get);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("http get for {} returned {}", url, response.getStatusLine());
                throw new RuntimeException("unable to get " + url);
            }
            return EntityUtils.toString(response.getEntity(), UTF8);
        } catch (IOException e) {
            log.error("caught exception when getting {}", url, e);
            throw new RuntimeException("unable to get " + url, e);
        }
    }

}
