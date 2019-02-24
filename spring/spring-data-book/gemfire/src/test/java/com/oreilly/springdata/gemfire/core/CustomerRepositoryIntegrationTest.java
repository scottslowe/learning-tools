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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.oreilly.springdata.gemfire.AbstractIntegrationTest;
import com.oreilly.springdata.gemfire.core.Customer;
import com.oreilly.springdata.gemfire.core.CustomerRepository;
import com.oreilly.springdata.gemfire.core.EmailAddress;

/**
 * Integration tests for {@link CustomerRepository}
 * 
 * @author Oliver Gierke
 * @author David Turanski
 */
public class CustomerRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void testFind() {

		Customer result = customerRepository.findByEmailAddress(new EmailAddress("dave@dmband.com"));
		assertThat(result, is(notNullValue()));
	}
}
