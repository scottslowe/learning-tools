package com.oreilly.springdata.hadoop.streaming;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomFileHandler {

	private static final Log log = LogFactory.getLog(CustomFileHandler.class);

	public File handleFile(File input) {
		log.info("Copying file: " + input.getAbsolutePath());
		return input;
	}
}
