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
package com.oreilly.springdata.jpa.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.oreilly.springdata.jpa.AbstractIntegrationTest;
import com.oreilly.springdata.jpa.PlainJpaConfig;

/**
 * Integration test for the manual implementation ({@link JpaCustomerRepository}) of the {@link CustomerRepository}
 * interface.
 * 
 * @author Oliver Gierke
 */
@ContextConfiguration(classes = PlainJpaConfig.class)
public class JpaCustomerRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	CustomerRepository repository;

	@Test
	public void insertsNewCustomerCorrectly() {

		Customer customer = new Customer("Alicia", "Keys");
		customer = repository.save(customer);

		assertThat(customer.getId(), is(notNullValue()));
	}

	@Test
	public void updatesCustomerCorrectly() {

		Customer dave = repository.findByEmailAddress(new EmailAddress("dave@dmband.com"));
		assertThat(dave, is(notNullValue()));

		dave.setLastname("Miller");
		dave = repository.save(dave);

		Customer reference = repository.findByEmailAddress(dave.getEmailAddress());
		assertThat(reference.getLastname(), is(dave.getLastname()));
	}
}
