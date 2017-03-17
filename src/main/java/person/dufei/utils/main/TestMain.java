package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.io.file.FileReadUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestMain {

    private static final ObjectMapper OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectReader OR = OM.readerFor(PredictorRequest.class);
    private static final Charset UTF8 = Charset.forName("utf-8");
    private static final byte[] COLON = ":".getBytes(UTF8);
    private static final byte[] QUOTE = "\"".getBytes(UTF8);
    private static final byte[] COMMA = ",".getBytes(UTF8);
    private static final byte[] RIGHT_BRACE = "}".getBytes(UTF8);

    public static void main(String[] args) throws Exception {
        String json = FileReadUtils.getContent("/Users/dufei/Documents/1212/miaoche_1k");
        byte[] bytes = json.getBytes(UTF8);
        long[] ort = new long[1000], omt = new long[1000];
        for (int i = 0; i < 1000; i++) {
            Stopwatch sw1 = Stopwatch.createStarted();
            PredictorRequest req2 = OM.readValue(bytes, PredictorRequest.class);
            sw1.stop();
            Stopwatch sw2  =Stopwatch.createStarted();
            PredictorRequest req = OR.readValue(bytes);
            sw2.stop();
//            log.error("OR elapsed {} microseconds, OM elapsed {} microseconds", sw1.elapsed(TimeUnit.MICROSECONDS), sw2.elapsed(TimeUnit.MICROSECONDS));
            ort[i] = sw2.elapsed(TimeUnit.MICROSECONDS);
            omt[i] = sw1.elapsed(TimeUnit.MICROSECONDS);
        }
        Arrays.sort(ort);
        Arrays.sort(omt);
        log.error("tp50: or: {}, om: {}", ort[500], omt[500]);
        log.error("tp90: or: {}, om: {}", ort[900], omt[900]);
        log.error("tp99: or: {}, om: {}", ort[990], omt[990]);
    }

    private static String extractStringField(String rawJson, String fieldName) {
        int size = fieldName.length(), index = rawJson.indexOf(fieldName);
        if (index < 0) return null;
        index += size;
        while (rawJson.charAt(index) != ':') index++;
        while (rawJson.charAt(index) != '"') index++;
        index++;
        StringBuilder sb = new StringBuilder();
        while (rawJson.charAt(index) != '"') sb.append(rawJson.charAt(index++));
        return sb.toString();
    }

    private static String extractStringField(byte[] bytes, String fieldName) {
        byte[] target = fieldName.getBytes(UTF8);
        int index = Bytes.indexOf(bytes, target);
        if (index < 0) return null;
        index += target.length;
        while (!match(bytes, index, COLON)) index++;
        index += COLON.length;
        while (!match(bytes, index, QUOTE)) index++;
        index += QUOTE.length;
        int start = index;
        while (!match(bytes, index, QUOTE)) index++;
        int end = index;
        byte[] fieldValueBytes = Arrays.copyOfRange(bytes, start, end);
        return new String(fieldValueBytes, UTF8);
    }

    private static int extractIntField(byte[] bytes, String fieldName) {
        byte[] target = fieldName.getBytes(UTF8);
        int index = Bytes.indexOf(bytes, target);
        if (index < 0) throw new IllegalArgumentException("can't find " + fieldName + " in " + new String(bytes, UTF8));
        index += target.length;
        while (!match(bytes, index, COLON)) index++;
        index += COLON.length;
        int start = index;
        while (!match(bytes, index, COMMA) && !match(bytes, index, RIGHT_BRACE)) index++;
        int end = index;
        byte[] fieldValueBytes = Arrays.copyOfRange(bytes, start, end);
        return Integer.parseInt(new String(fieldValueBytes, UTF8).trim());
    }

    private static boolean match(byte[] father, int index, byte[] son) {
        for (int i = 0; i < son.length; i++) {
            if (father[i+index] != son[i]) return false;
        }
        return true;
    }

}
