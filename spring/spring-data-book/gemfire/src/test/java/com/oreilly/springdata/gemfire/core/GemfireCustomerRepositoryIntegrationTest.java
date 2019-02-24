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
package com.oreilly.springdata.gemfire.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
/**
 * 
 * @author David Turanski
 * @author Oliver Gierke
 *
 */
public class GemfireCustomerRepositoryIntegrationTest {

	@Autowired
	CustomerRepository customerRepository;

	@Test
	public void basicRepositoryOperations() {

		Address address = new Address("Broadway", "New York", "United States");
		Customer dave = new Customer(1L, new EmailAddress("dave@dmband.com"), "Dave", "Matthews");
		dave.add(address);

		customerRepository.save(dave);
		assertEquals(dave, customerRepository.findByLastname("Matthews").get(0));

		assertEquals(1, customerRepository.findAll().size());

		customerRepository.delete(dave);
		assertEquals(0, customerRepository.findAll().size());
	}
}
