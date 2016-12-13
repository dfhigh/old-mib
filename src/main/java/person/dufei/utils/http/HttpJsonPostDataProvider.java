package person.dufei.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class HttpJsonPostDataProvider {

    private static final Charset UTF8 = Charset.forName("utf-8");
    private static final ContentType TP = ContentType.TEXT_PLAIN.withCharset(UTF8);
    private static final ContentType AJ = ContentType.APPLICATION_JSON.withCharset(UTF8);

    public static String getData(String url, String json) {
        try (CloseableHttpClient http = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(json, TP));
            CloseableHttpResponse response = http.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("got response {} when posting {} to {}", response.getStatusLine(), json, url);
                throw new RuntimeException("remote server returns " + response.getStatusLine());
            }
            return EntityUtils.toString(response.getEntity(), UTF8);
        } catch (IOException e) {
            log.error("caught exception when posting {} to {}", json, url, e);
            throw new RuntimeException("unable to post " + url, e);
        }
    }

}
