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
package com.oreilly.springdata.gemfire;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.oreilly.springdata.gemfire.core.Address;
import com.oreilly.springdata.gemfire.core.Customer;
import com.oreilly.springdata.gemfire.core.CustomerRepository;
import com.oreilly.springdata.gemfire.core.EmailAddress;
import com.oreilly.springdata.gemfire.core.Product;
import com.oreilly.springdata.gemfire.core.ProductRepository;
import com.oreilly.springdata.gemfire.order.LineItem;
import com.oreilly.springdata.gemfire.order.Order;
import com.oreilly.springdata.gemfire.order.OrderRepository;

/**
 * Base class for integration tests adding some sample data through the vFabric GemFire Java driver.
 * 
 * @author Oliver Gierke
 * @author David Turanski
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public abstract class AbstractIntegrationTest {

	@Autowired
	protected CustomerRepository customerRepository;

	@Autowired
	protected OrderRepository orderRepository;

	@Autowired
	protected ProductRepository productRepository;

	@Before
	public void setUp() {

		// Customers

		Address address = new Address("Broadway", "New York", "United States");
		Customer dave = new Customer(1L, new EmailAddress("dave@dmband.com"), "Dave", "Matthews");
		dave.add(address);
		customerRepository.save(dave);

		// Products

		Product iPad = new Product(1L, "iPad", new BigDecimal(499.0), "Apple tablet device");
		Product macBook = new Product(2L, "MacBook Pro", new BigDecimal(1299.0), "Apple notebook");
		Product dock = new Product(3L, "Dock", new BigDecimal(49.0), "Dock for iPhone/iPad");
		dock.setAttribute("connector", "plug");

		productRepository.save(iPad);
		productRepository.save(macBook);
		productRepository.save(dock);

		// Orders

		Order order = new Order(1L, dave.getId(), address);
		order.add(new LineItem(iPad, 2));
		order.add(new LineItem(macBook));
		orderRepository.save(order);
	}
}
