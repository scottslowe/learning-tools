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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.gemstone.gemfire.cache.query.SelectResults;

/**
 * A Data access object using a native Gemfire interfaces for data access. NOTE: This implementation demonstrates the
 * use of {@link GemfireTemplate} however it's essentially identical to what Gemfire Repository support gives you out of
 * the box. In other words it is not really required.
 * 
 * @author David Turanski
 * @author Oliver Gierke
 */
@Repository
class GemfireCustomerRepository implements CustomerRepository {

	private final GemfireTemplate template;

	/**
	 * Creates a new {@link GemfireCustomerRepository} using the given {@link GemfireTemplate}.
	 * 
	 * @param template must not be {@literal null}.
	 */
	@Autowired
	public GemfireCustomerRepository(GemfireTemplate template) {

		Assert.notNull(template);
		this.template = template;
	}

	/**
	 * Returns all objects in the region. Not advisable for very large data sets.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Customer> findAll() {
		return new ArrayList<Customer>((Collection<? extends Customer>) template.getRegion().values());
	}

	/* 
	 * (non-Javadoc)
	 * @see com.oreilly.springdata.gemfire.customer.CustomerRepository#save(com.oreilly.springdata.gemfire.customer.Customer)
	 */
	@Override
	public Customer save(Customer customer) {
		template.put(customer.getId(), customer);
		return customer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.oreilly.springdata.gemfire.customer.CustomerRepository#findByLastname(java.lang.String)
	 */
	public List<Customer> findByLastname(String lastname) {

		String queryString = "lastname = '" + lastname + "'";
		SelectResults<Customer> results = template.query(queryString);
		return results.asList();
	}

	/* (non-Javadoc)
	 * @see com.oreilly.springdata.gemfire.customer.CustomerRepository#findByEmailAddress(com.oreilly.springdata.gemfire.core.EmailAddress)
	 */
	@Override
	public Customer findByEmailAddress(EmailAddress emailAddress) {

		String queryString = "emailAddress = ?1";
		return template.findUnique(queryString, emailAddress);
	}

	/*
	 * (non-Javadoc)
	 * @see com.oreilly.springdata.gemfire.customer.CustomerRepository#delete(com.oreilly.springdata.gemfire.customer.Customer)
	 */
	public void delete(Customer customer) {
		template.remove(customer.getId());
	}
}
