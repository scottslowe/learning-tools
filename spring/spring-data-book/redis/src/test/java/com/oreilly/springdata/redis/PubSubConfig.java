package com.oreilly.springdata.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author Jon Brisbin
 */
@Configuration
public class PubSubConfig extends ApplicationConfig {

  public static final String DUMP_CHANNEL = "spring-data-book:pubsub-test:dump";

  @Bean RedisMessageListenerContainer container() {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory());
    container.addMessageListener(dumpToConsoleListener(), new ChannelTopic(DUMP_CHANNEL));
    return container;
  }

  @Bean MessageListener dumpToConsoleListener() {
    return new MessageListener() {
      @Override public void onMessage(Message message, byte[] pattern) {
        System.out.println("FROM MESSAGE: " + new String(message.getBody()));
      }
    };
  }

}
