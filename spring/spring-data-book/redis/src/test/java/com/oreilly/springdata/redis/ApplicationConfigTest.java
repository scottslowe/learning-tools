package com.oreilly.springdata.redis;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Jon Brisbin
 */
public class ApplicationConfigTest {

  @Test public void boostrapFromJavaConfig() {
    ApplicationContext appCtx = new AnnotationConfigApplicationContext(ApplicationConfig.class);

    assertThat(appCtx, is(notNullValue()));
  }

}
