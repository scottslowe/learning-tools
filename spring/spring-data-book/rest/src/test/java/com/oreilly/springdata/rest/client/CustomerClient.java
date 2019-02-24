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

/**
 * Sample client to access {@link Customers} exposed by the REST exporter.
 * 
 * @author Oliver Gierke
 */
class CustomerClient {

	/**
	 * A sample DTO to have a {@link Resource} object typed to {@link com.oreilly.springdata.rest.core.Customer}.
	 * 
	 * @author Oliver Gierke
	 */
	static class Customer extends Resource<com.oreilly.springdata.rest.core.Customer> {

	}

	/**
	 * DTO to bind a paged collection resource of {@link Customer}s.
	 * 
	 * @author Oliver Gierke
	 */
	static class Customers extends PagedResources<Customer> {

	}

	public static void main(String[] args) {

		// Setup RestTemplate though Spring
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ClientConfiguration.class);
		context.registerShutdownHook();
		RestOperations restOperations = context.getBean(RestOperations.class);

		// Access root resource
		ResourceSupport result = restOperations.getForObject(ClientConfiguration.BASE_URL, Resource.class);

		Link link = result.getLink(ClientConfiguration.CUSTOMERS_REL);
		System.out.println("Following: " + link.getHref());

		// Follow link relation for customers to access those
		Customers customers = restOperations.getForObject(link.getHref(), Customers.class);

		for (Customer dto : customers) {
			com.oreilly.springdata.rest.core.Customer customer = dto.getContent();
			System.out.println(customer.getFirstname() + " " + customer.getLastname());
		}
	}
}
