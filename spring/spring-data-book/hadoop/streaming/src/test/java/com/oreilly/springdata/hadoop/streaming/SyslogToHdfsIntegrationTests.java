package com.oreilly.springdata.hadoop.streaming;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/application-context.xml" })
public class SyslogToHdfsIntegrationTests {

	@Test
	public void test() {

	}

}
