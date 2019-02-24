package org.springframework.data.hadoop.samples;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/launch-context.xml")
public class WordCountWorkflowTests {

    @Autowired
    private ApplicationContext ctx;

	@Test
	public void testWorkflowNS() throws Exception {
		startJobs(ctx);
	}
	
	public void startJobs(ApplicationContext ctx) {
		JobLauncher launcher = ctx.getBean(JobLauncher.class);
		Map<String, Job> jobs = ctx.getBeansOfType(Job.class);

		for (Map.Entry<String, Job> entry : jobs.entrySet()) {
			System.out.println("Executing job " + entry.getKey());
			try {
				if (launcher.run(entry.getValue(), new JobParameters()).getStatus().equals(BatchStatus.FAILED)){
					throw new BeanInitializationException("Failed executing job " + entry.getKey());
				}
			} catch (Exception ex) {
				throw new BeanInitializationException("Cannot execute job " + entry.getKey(), ex);
			}
		}
	}
}