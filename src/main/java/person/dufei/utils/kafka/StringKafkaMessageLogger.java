package person.dufei.utils.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collection;

@Slf4j
public class StringKafkaMessageLogger extends KafkaMessageConsumer<String, String> {

    private static final Deserializer<String> STRING_DESERIALIZER = new StringDeserializer();

    public StringKafkaMessageLogger(String kafkaEndpoints, Collection<String> topics) {
        super(kafkaEndpoints, topics, STRING_DESERIALIZER, STRING_DESERIALIZER);
    }

    @Override
    protected void consume(ConsumerRecord<String, String> record) {
        if (record == null) return;
        log.info("at {} received message from topic {} in partition {} at offset {} with key {} and value {}",
                record.timestamp(), record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
