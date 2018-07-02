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
        helpIntercept();
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

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to print kafka messages to console, we support below configurations");
        log.info("");
        log.info("\t-h, print this message and exit");
        log.info("\t--kafka-endpoints {endpoints}, mandatory, kafka endpoints, comma separated list");
        log.info("\t--kafka-topics {topics}, mandatory, kafka topics, comma separated list");
        log.info("\t-c {concurrency}, optional, thread count to consume the messages, default value is 1");

        System.exit(0);
    }
}
