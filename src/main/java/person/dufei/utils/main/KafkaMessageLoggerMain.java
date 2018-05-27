package person.dufei.utils.main;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import person.dufei.utils.kafka.KafkaMessageConsumer;
import person.dufei.utils.kafka.StringKafkaMessageLogger;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class KafkaMessageLoggerMain {

    public static void main(String[] args) {
        String kafkaEndpoints = System.getProperty("kafkaEndpoints");
        String kafkaTopicsAgg = System.getProperty("kafkaTopics");
        int concurrency = Integer.parseInt(Optional.ofNullable(System.getProperty("concurrency")).orElse("1"));
        KafkaMessageConsumer<String, String> consumer = new StringKafkaMessageLogger(kafkaEndpoints,
                Arrays.asList(StringUtils.split(kafkaTopicsAgg, ",")));
        ExecutorService es = Executors.newFixedThreadPool(concurrency);
        for (int i = 0; i < concurrency; i++) {
            es.submit(consumer);
        }
    }
}
