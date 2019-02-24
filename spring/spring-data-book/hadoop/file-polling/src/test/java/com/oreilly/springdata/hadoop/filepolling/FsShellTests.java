package com.oreilly.springdata.hadoop.filepolling;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/hadoop-context.xml" })
public class FsShellTests {

	private static final Log log = LogFactory.getLog(FsShellTests.class);

	@Autowired
	private Configuration configuration;

	private FsShell fsShell;

	@Test
	public void shellOps() {
		assertNotNull(configuration);
		log.info(configuration);
		fsShell = new FsShell(configuration);
		Collection<FileStatus> coll = fsShell.ls("/");
		log.info(coll);

	}
}
