package com.oreilly.springdata.hadoop.hive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;
import org.springframework.util.Assert;

@Repository
public class JdbcPasswordRepository implements PasswordRepository, ResourceLoaderAware {

	private @Autowired JdbcOperations jdbcOperations;

	private @Value("${hive.table}")	String tableName;
	
	private ResourceLoader resourceLoader;

	/*
	@Autowired
	public JdbcPasswordRepository(JdbcOperations jdbcOperations) {
		Assert.notNull(jdbcOperations);
		this.jdbcOperations = jdbcOperations;
	}*/

	@Override
	public Long count() {
		return jdbcOperations.queryForLong("select count(*) from " + tableName);
	}

	@Override
	public void processPasswordFile(String inputFile) {
		/*
		SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(jdbcOperations),
				resourceLoader.getResource(inputFile),
				true);
		*/
		//TODO need to pass in a variable to the script... probab need to 
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
