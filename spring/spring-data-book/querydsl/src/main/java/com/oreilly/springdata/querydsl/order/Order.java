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
package com.oreilly.springdata.querydsl.order;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mysema.query.annotations.QueryEntity;
import com.oreilly.springdata.querydsl.core.AbstractEntity;
import com.oreilly.springdata.querydsl.core.Address;
import com.oreilly.springdata.querydsl.core.Customer;

/**
 * @author Oliver Gierke
 */
@QueryEntity
public class Order extends AbstractEntity {

	private Customer customer;
	private Address billingAddress;
	private Address shippingAddress;
	private Set<LineItem> lineItems = new HashSet<LineItem>();

	public Order(Customer customer) {
		this.customer = customer;
	}

	protected Order() {

	}

	public void add(LineItem lineItem) {
		this.lineItems.add(lineItem);
	}

	public Customer getCustomer() {
		return customer;
	}

	public Address getBillingAddress() {
		return billingAddress;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public Set<LineItem> getLineItems() {
		return Collections.unmodifiableSet(lineItems);
	}
}
