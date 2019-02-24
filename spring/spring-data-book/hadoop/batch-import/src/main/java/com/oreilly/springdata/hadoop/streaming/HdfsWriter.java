package com.oreilly.springdata.hadoop.streaming;

import java.io.IOException;

import org.springframework.integration.Message;

public interface HdfsWriter {

	void write(Message<?> message) throws IOException;
	
	void close();
}
