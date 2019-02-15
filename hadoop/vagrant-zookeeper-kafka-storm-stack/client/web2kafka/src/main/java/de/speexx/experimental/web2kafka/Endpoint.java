package de.speexx.experimental.web2kafka;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sascha.kohlmann
 */
@Path("event")
public class Endpoint {
 
    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @POST
    @Path("add")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response addPerformancData(final String json) throws IOException {
        
        LOG.info(json);
        
        final Properties props = producerConfiguration();
        try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
            final ProducerRecord record = new ProducerRecord<>(topicName(), "time-" + System.currentTimeMillis(), json);
            final Future<RecordMetadata> future = producer.send(record);
            System.out.format("Check future.%n", record);
            final RecordMetadata meta = future.get(2, TimeUnit.SECONDS);
            return Response.status(Response.Status.CREATED).build();
        } catch (final IOException | InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Unable to handle input.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    Properties producerConfiguration() throws IOException {
        
        final Properties properties = fetchKafkaProperties();
        
        final Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.server") + ":" + properties.getProperty("kafka.port"));
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return kafkaProperties;
    }

    String topicName() throws IOException {
        return fetchKafkaProperties().getProperty("kafka.topic");
    }
    
    Properties fetchKafkaProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(Endpoint.class.getResourceAsStream("/META-INF/kafka.properties"));
        return properties;
    }
}
