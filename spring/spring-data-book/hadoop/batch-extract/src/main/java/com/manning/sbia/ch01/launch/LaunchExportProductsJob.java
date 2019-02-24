/**
 * 
 */
package com.manning.sbia.ch01.launch;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Launches the import invoices job.
 * 
 * @author acogoluegnes
 * 
 */
public class LaunchExportProductsJob {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:/META-INF/spring/*.xml");

		JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
		Job job = ctx.getBean(Job.class);

		jobLauncher.run(job,
				new JobParametersBuilder()
						.addString("hdfsSourceDirectory", "/data/analysis/results/part-*")
						.addDate("date", new Date()).toJobParameters());
	}

}
