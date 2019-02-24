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
package com.oreilly.springdata.neo4j.order;

import com.oreilly.springdata.neo4j.core.AbstractEntity;
import com.oreilly.springdata.neo4j.core.Address;
import com.oreilly.springdata.neo4j.core.Customer;
import com.oreilly.springdata.neo4j.core.Product;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Order extends AbstractEntity {

	@RelatedTo
	private Customer customer;
	@RelatedTo
	private Address billingAddress;
	@RelatedTo
	private Address shippingAddress;

	@RelatedToVia(type = "ITEMS")
    @Fetch
	private Set<LineItem> lineItems = new HashSet<LineItem>();

	public Order(Customer customer) {
		this.customer = customer;
	}

	protected Order() {

	}

	public void add(LineItem lineItem) {
		this.lineItems.add(lineItem);
	}
    // TODO JIRA setter was used when hydrating object from storage, don't use BeanWrapper
    public Order withBillingAddress(Address billingAddress) {
        Assert.state(customer.hasAddress(billingAddress),"valid customer address for "+customer);
        this.billingAddress = billingAddress;
        return this;
    }

    public Order withShippingAddress(Address shippingAddress) {
        Assert.state(customer.hasAddress(shippingAddress),"valid customer address for "+customer);
        this.shippingAddress = shippingAddress;
        return this;
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

    public void add(Product product, int amount) {
        add(new LineItem(this,product,amount));
    }
}
