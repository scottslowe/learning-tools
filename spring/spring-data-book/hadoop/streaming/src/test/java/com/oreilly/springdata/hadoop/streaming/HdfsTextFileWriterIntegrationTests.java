package com.oreilly.springdata.hadoop.streaming;

import org.apache.hadoop.fs.FileSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/hadoop-context.xml" })
public class HdfsTextFileWriterIntegrationTests {

	@Autowired
	private FileSystem hadoopFs;
	
	@Test
	public void test() throws Exception {
		HdfsTextFileWriter fileWriter = new HdfsTextFileWriter(hadoopFs);
		Message<?> message = MessageBuilder.withPayload("Hello World!").build();
		fileWriter.write(message);		
	}

}
