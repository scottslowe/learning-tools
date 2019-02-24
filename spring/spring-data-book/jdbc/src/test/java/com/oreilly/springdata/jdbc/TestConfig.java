package com.oreilly.springdata.jdbc;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Additional configuration for integration tests. Overriding the bean definition for the {@link DataSource}.
 * 
 * @author Thomas Risberg
 * @author Oliver Gierke
 */
@Configuration
@EnableTransactionManagement
public class TestConfig extends ApplicationConfig {

	/*
	 * (non-Javadoc)
	 * @see com.oreilly.springdata.jdbc.ApplicationConfig#dataSource()
	 */
	@Bean
	@Override
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).addScript("classpath:sql/schema.sql")
				.addScript("classpath:sql/test-data.sql").build();
	}
}
