package com.oreilly.springdata.redis.caching;

import org.springframework.cache.annotation.Cacheable;

/**
 * @author Jon Brisbin
 */
public class CacheableTest {

  @Cacheable(value = "greetings")
  public String getCacheableValue() {
    long now = System.currentTimeMillis();
    return "Hello World (@ " + now + ")!";
  }

}
