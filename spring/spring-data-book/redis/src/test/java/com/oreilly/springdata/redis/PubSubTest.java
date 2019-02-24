package com.oreilly.springdata.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PubSubConfig.class})
public class PubSubTest {

  @Autowired RedisConnectionFactory connectionFactory;

  @Test public void testPubSubWithoutConversion() {
    RedisConnection redis = connectionFactory.getConnection();
    try {
      redis.publish( PubSubConfig.DUMP_CHANNEL.getBytes(), "Hello World!".getBytes() );
    } finally {
      redis.close();
    }
  }

  @Test public void testPubSubWithConversion() {
    RedisConnection redis = connectionFactory.getConnection();

    RedisMessageListenerContainer listeners = new RedisMessageListenerContainer();
    listeners.setConnectionFactory( connectionFactory );

    MessageListenerAdapter listener = new MessageListenerAdapter( new BeanMessageListener() );
    listener.setSerializer( new BeanMessageSerializer() );
    listener.afterPropertiesSet();

    listeners.addMessageListener( listener, new ChannelTopic( PubSubConfig.DUMP_CHANNEL ) );
    listeners.afterPropertiesSet();
    listeners.start();

    try {
      redis.publish( PubSubConfig.DUMP_CHANNEL.getBytes(), "Hello World!".getBytes() );
    } finally {
      redis.close();
      listeners.stop();
    }
  }

  private class BeanMessage {
    String message;

    private BeanMessage( String message ) {
      this.message = message;
    }
  }

  private class BeanMessageListener {
    public void handleMessage( BeanMessage msg ) {
      System.out.println( "msg: " + msg.message );
    }
  }

  private class BeanMessageSerializer implements RedisSerializer<BeanMessage> {
    @Override public byte[] serialize( BeanMessage beanMessage ) throws SerializationException {
      return (null != beanMessage.message ? beanMessage.message.getBytes() : new byte[0]);
    }

    @Override public BeanMessage deserialize( byte[] bytes ) throws SerializationException {
      return new BeanMessage( new String( bytes ) );
    }
  }
}
