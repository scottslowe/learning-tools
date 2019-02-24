/**
 * 
 */
package com.oreilly.springdata.batch.launch;

import org.h2.tools.Console;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author acogoluegnes
 *
 */
public class LaunchDatabaseAndConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext(
			"/META-INF/spring/batch-infrastructure-context.xml",
			"/META-INF/spring/initialize/initialize-database-context.xml"
		);
		Console.main(args);
	}

}
