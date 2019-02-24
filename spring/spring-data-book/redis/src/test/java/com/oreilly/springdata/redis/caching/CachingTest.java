package com.oreilly.springdata.redis.caching;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CachingConfig.class})
public class CachingTest {

  @Autowired CacheableTest cacheableTest;

  @Test public void testCaching() {
    String s1 = cacheableTest.getCacheableValue();
    String s2 = cacheableTest.getCacheableValue();

    assertThat( s1, is( equalTo( s2 ) ) );
  }
}
