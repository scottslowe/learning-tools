package com.oreilly.springdata.hadoop.ftp;


import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.util.Assert;

public class Ftp {

	private static final Log log = LogFactory.getLog(Ftp.class);

	public static void main(String[] args) throws Exception {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/application-context.xml", Ftp.class);
		log.info("Ftp Application Running");
		context.registerShutdownHook();
		Scanner scanIn = new Scanner(System.in);
	    scanIn.nextLine();
		
	}
}
