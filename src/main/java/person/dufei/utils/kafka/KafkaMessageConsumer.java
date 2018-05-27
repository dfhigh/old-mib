package person.dufei.utils.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Collection;
import java.util.Properties;

import static com._4paradigm.prophet.rest.utils.Validator.validateCollectionNotEmptyContainsNoNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateObjectNotNull;
import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

public abstract class KafkaMessageConsumer<K, V> implements Runnable {

    private final String kafkaEndpoints;
    private final Collection<String> topics;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;

    public KafkaMessageConsumer(final String kafkaEndpoints, final Collection<String> topics, final Deserializer<K> keyDeserializer,
                                final Deserializer<V> valueDeserializer) {
        validateStringNotBlank(kafkaEndpoints, "kafka endpoints");
        validateCollectionNotEmptyContainsNoNull(topics, "kafka topics");
        validateObjectNotNull(keyDeserializer, "key deserializer");
        validateObjectNotNull(valueDeserializer, "value deserializer");
        this.kafkaEndpoints = kafkaEndpoints;
        this.topics = topics;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    @Override
    public void run() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEndpoints);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "mib");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (KafkaConsumer<K, V> consumer = new KafkaConsumer<>(properties, keyDeserializer, valueDeserializer)) {
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<K, V> records = consumer.poll(0);
                records.forEach(this::consume);
            }
        }
    }

    protected abstract void consume(ConsumerRecord<K, V> record);
}
