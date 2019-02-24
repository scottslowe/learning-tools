/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oreilly.springdata.hadoop.pig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.hadoop.fs.FsShell;

public class PigAppWithRepository {

	private static final Log log = LogFactory.getLog(PigAppWithRepository.class);

	public static void main(String[] args) throws Exception {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/pig-context-password-repository.xml", PigAppWithRepository.class);
		log.info("Pig Application Running");
		context.registerShutdownHook();	

		String outputDir = "/data/password-repo/output";		
		FsShell fsShell = context.getBean(FsShell.class);
		if (fsShell.test(outputDir)) {
			fsShell.rmr(outputDir);
		}
		
		PasswordRepository repo = context.getBean(PigPasswordRepository.class);
		repo.processPasswordFile("/data/passwd/input");
		
		/*
		Collection<String> files = new ArrayList<String>();
		files.add("/data/passwd/input");
		files.add("/data/passwd/input2");
		repo.processPasswordFiles(files);
		*/


	    
	}
}
