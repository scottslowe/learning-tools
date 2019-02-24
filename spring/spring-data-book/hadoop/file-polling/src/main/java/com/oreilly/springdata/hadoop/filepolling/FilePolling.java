package com.oreilly.springdata.hadoop.filepolling;

import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FilePolling {

	private static final Log log = LogFactory.getLog(FilePolling.class);

	public static void main(String[] args) throws Exception {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/application-context.xml", FilePolling.class);
		log.info("File Polling Application Running");
		context.registerShutdownHook();
		Scanner scanIn = new Scanner(System.in);
	    scanIn.nextLine();
	}
}
