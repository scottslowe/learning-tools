package com.oreilly.springdata.redis;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
public class AtomicCountersTest {

  @Autowired RedisConnectionFactory connectionFactory;

  @Test public void testAtomicCounters() {
    RedisAtomicLong counter = new RedisAtomicLong("spring-data-book:counter-test:hits", connectionFactory, 0);
    Long l = counter.incrementAndGet();

    assertThat(l, is(greaterThan(0L)));
  }

}
