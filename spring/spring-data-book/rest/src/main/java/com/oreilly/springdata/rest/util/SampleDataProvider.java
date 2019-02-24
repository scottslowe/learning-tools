/*
 * Copyright 2012 the original author or authors.
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
package com.oreilly.springdata.rest.util;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Spring component to populate the configured {@link DataSource} with the sample data SQL file (see
 * {@code src/main/resources/data.sql}) if the application is started with the {@code with-data} profile being active.
 * 
 * @author Oliver Gierke
 */
@Component
@Profile("with-data")
class SampleDataProvider implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(SampleDataProvider.class);
	private static final String SQL_FILE = "data.sql";

	private final DataSource dataSource;
	private ApplicationContext applicationContext;

	/**
	 * Creates a new {@link SampleDataProvider} to populate the given {@link DataSource}.
	 * 
	 * @param dataSource must not be {@literal null}.
	 */
	@Autowired
	public SampleDataProvider(DataSource dataSource) {

		LOG.info("SampleDataProvider activated!");

		Assert.notNull(dataSource, "DataSource must not be null!");
		this.dataSource = dataSource;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (!event.getApplicationContext().equals(applicationContext)) {
			return;
		}

		LOG.info("Populating datasource using SQL file {}!", SQL_FILE);

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setScripts(new Resource[] { new ClassPathResource(SQL_FILE) });
		DatabasePopulatorUtils.execute(populator, dataSource);
	}
}
