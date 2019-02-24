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
package com.oreilly.springdata.mongodb.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.oreilly.springdata.mongodb.AbstractIntegrationTest;

/**
 * Integration tests for {@link CustomerRepository}.
 * 
 * @author Oliver Gierke
 */
public class CustomerRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	CustomerRepository repository;

	@Test
	public void savesCustomerCorrectly() {

		EmailAddress email = new EmailAddress("alicia@keys.com");

		Customer dave = new Customer("Alicia", "Keys");
		dave.setEmailAddress(email);
		dave.add(new Address("27 Broadway", "New York", "United States"));

		Customer result = repository.save(dave);
		assertThat(result.getId(), is(notNullValue()));
	}

	@Test
	public void readsCustomerByEmail() {

		EmailAddress email = new EmailAddress("alicia@keys.com");
		Customer alicia = new Customer("Alicia", "Keys");
		alicia.setEmailAddress(email);

		repository.save(alicia);

		Customer result = repository.findByEmailAddress(email);
		assertThat(result, is(alicia));
	}

	@Test(expected = DuplicateKeyException.class)
	public void preventsDuplicateEmail() {

		Customer dave = repository.findByEmailAddress(new EmailAddress("dave@dmband.com"));

		Customer anotherDave = new Customer("Dave", "Matthews");
		anotherDave.setEmailAddress(dave.getEmailAddress());

		repository.save(anotherDave);
	}
}
