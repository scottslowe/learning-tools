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
package com.oreilly.springdata.rest.client;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.client.RestOperations;

import com.oreilly.springdata.rest.client.OrderClient.Order;
import com.oreilly.springdata.rest.client.OrderClient.Orders;

/**
 * Sample client to access {@link Orders} exposed by the REST exporter.
 * 
 * @author Oliver Gierke
 */
class OrderClient {

	/**
	 * A sample DTO to have a {@link Resource} object typed to {@link com.oreilly.springdata.rest.order.Order}.
	 * 
	 * @author Oliver Gierke
	 */
	static class Order extends Resource<com.oreilly.springdata.rest.order.Order> {

	}

	/**
	 * DTO to bind a paged collection resource of {@link Order}s.
	 * 
	 * @author Oliver Gierke
	 */
	static class Orders extends PagedResources<Order> {

	}

	public static void main(String[] args) {

		// Bootstrap RestOperations instance using Spring
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ClientConfiguration.class);
		context.registerShutdownHook();
		RestOperations operations = context.getBean(RestOperations.class);

		// Access root resource
		ResourceSupport root = operations.getForEntity(ClientConfiguration.BASE_URL, Resource.class).getBody();
		Link orderLink = root.getLink(ClientConfiguration.ORDERS_REL);

		// Follow link to access orders
		Orders orders = operations.getForObject(orderLink.getHref(), Orders.class);

		for (Order order : orders) {

			// Follow link to access customer of the order
			Link link = order.getLink(ClientConfiguration.ORDER_CUSTOMER_REL);
			CustomerClient.Customer customer = operations.getForObject(link.getHref(), CustomerClient.Customer.class);
			com.oreilly.springdata.rest.core.Customer domainObject = customer.getContent();
			System.out.println("Order for customer: " + domainObject.getFirstname() + " " + domainObject.getLastname());
		}
	}
}
