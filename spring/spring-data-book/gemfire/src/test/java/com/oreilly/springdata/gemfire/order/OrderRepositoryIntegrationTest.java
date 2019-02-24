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
package com.oreilly.springdata.gemfire.order;

import static com.oreilly.springdata.gemfire.core.CoreMatchers.*;
import static com.oreilly.springdata.gemfire.order.OrderMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.oreilly.springdata.gemfire.AbstractIntegrationTest;
import com.oreilly.springdata.gemfire.core.Customer;
import com.oreilly.springdata.gemfire.core.EmailAddress;
import com.oreilly.springdata.gemfire.core.Product;

/**
 * Integration tests for {@link OrderRepository}.
 * 
 * @author Oliver Gierke
 * @author David Turanski
 */
public class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void createOrder() {

		Customer dave = customerRepository.findByEmailAddress(new EmailAddress("dave@dmband.com"));
		Product iPad = productRepository.findByName("iPad").get(0);

		Order order = new Order(2L, dave.getId(), dave.getAddresses().iterator().next());
		order.add(new LineItem(iPad));

		order = orderRepository.save(order);
		assertThat(order.getId(), is(notNullValue()));
	}

	@Test
	public void readOrder() {

		Product iPad = productRepository.findByName("iPad").get(0);
		Customer dave = customerRepository.findByEmailAddress(new EmailAddress("dave@dmband.com"));
		List<Order> orders = orderRepository.findByCustomerId(dave.getId());

		Matcher<Iterable<? super Order>> hasOrderForiPad = containsOrder(with(LineItem(with(ProductId(equalTo(iPad.getId()))))));

		assertThat(orders, hasOrderForiPad);
	}
}
