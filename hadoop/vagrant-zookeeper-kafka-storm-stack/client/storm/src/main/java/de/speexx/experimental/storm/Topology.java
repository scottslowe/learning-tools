package de.speexx.experimental.storm;

import java.io.IOException;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.ByTopicRecordTranslator;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.storm.kafka.spout.KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff.TimeInterval;

/**
 *
 * @author sascha.kohlmann
 */
public class Topology {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static final String TOPIC_2_STREAM = "test_2_stream";
    
    public static void main(final String[] args) throws Exception {
        new Topology().runMain(args);
    }

    void runMain(final String[] args) throws Exception {
        final Config cfg = getConfig();
        final KafkaSpoutConfig kafkaConfig = getKafkaSpoutConfig(bootstrapServer());
        final StormTopology topology = getTopologyKafkaSpout(kafkaConfig);
        StormSubmitter.submitTopology("perfPrint", cfg, topology);
    }
    
    protected Config getConfig() {
        final Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 2048);
        config.put(Config.TOPOLOGY_BACKPRESSURE_ENABLE, false);
        config.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 16384);
        config.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 16384);
        return config;
    }

    StormTopology getTopologyKafkaSpout(final KafkaSpoutConfig<String, String> spoutConfig) {
        final TopologyBuilder tp = new TopologyBuilder();
        tp.setSpout("kafka_spout", new KafkaSpout<>(spoutConfig), 1);
        tp.setBolt("kafka_bolt", new SimpleBolt()).shuffleGrouping("kafka_spout", TOPIC_2_STREAM);
        return tp.createTopology();
    }

    KafkaSpoutConfig<String, String> getKafkaSpoutConfig(final String bootstrapServers) throws Exception {
        final ByTopicRecordTranslator<String, String> trans = new ByTopicRecordTranslator<>(
                (r) -> new Values(r.topic(), r.partition(), r.offset(), r.key(), r.value()),
                       new Fields("topic", "partition", "offset", "key", "value"), TOPIC_2_STREAM);
        return KafkaSpoutConfig.builder(bootstrapServers, kafkaTopicName())
            .setProp(ConsumerConfig.GROUP_ID_CONFIG, "kafkaSpoutGroup")
            .setRetry(getRetryService())
            .setRecordTranslator(trans)
            .setOffsetCommitPeriodMs(10_000)
            .setFirstPollOffsetStrategy(EARLIEST)
            .setMaxUncommittedOffsets(250)
            .build();
    }

    KafkaSpoutRetryService getRetryService() {
        return new KafkaSpoutRetryExponentialBackoff(TimeInterval.microSeconds(500),
            TimeInterval.milliSeconds(2), Integer.MAX_VALUE, TimeInterval.seconds(10));
    }


    static String kafkaTopicName() throws IOException {
        return fetchKafkaProperties().getProperty("kafka.topic");
    }
    
    static String kafkaServer() throws IOException {
        return fetchKafkaProperties().getProperty("kafka.server");
    }
    
    static String kafkaPort() throws IOException {
        return fetchKafkaProperties().getProperty("kafka.port");
    }
    
    static Properties fetchKafkaProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(Topology.class.getResourceAsStream("/META-INF/kafka.properties"));
        return properties;
    }
    
    static String bootstrapServer() throws IOException {
        return kafkaServer() + ":" + kafkaPort();
    }
}
