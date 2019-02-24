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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.Region;
import org.springframework.util.Assert;


/**
 * A product.
 * 
 * @author Oliver Gierke
 * @author David Turanski
 */
@Region
public class Product extends AbstractPersistentEntity {

	private String name, description;
	private BigDecimal price;
	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * Creates a new {@link Product} with the given name.
	 * @param id a unique Id
	 * @param name must not be {@literal null} or empty.
	 * @param price must not be {@literal null} or less than or equal to zero.
	 */
	public Product(Long id, String name, BigDecimal price) {
		this(id, name, price, null);
	}

	/**
	 * Creates a new {@link Product} from the given name and description.
	 * @param id a unique Id
	 * @param name must not be {@literal null} or empty.
	 * @param price must not be {@literal null} or less than or equal to zero.
	 * @param description
	 */
	@PersistenceConstructor
	public Product(Long id, String name, BigDecimal price, String description) {
		super(id);
		Assert.hasText(name, "Name must not be null or empty!");
		Assert.isTrue(BigDecimal.ZERO.compareTo(price) < 0, "Price must be greater than zero!");

		this.name = name;
		this.price = price;
		this.description = description;
	}
	
	protected Product() {
	}

	/**
	 * Sets the attribute with the given name to the given value.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param value
	 */
	public void setAttribute(String name, String value) {

		Assert.hasText(name);

		if (value == null) {
			this.attributes.remove(value);
		} else {
			this.attributes.put(name, value);
		}
	}

	/**
	 * Returns the {@link Product}'s name.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the {@link Product}'s description.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns all the custom attributes of the {@link Product}.
	 * 
	 * @return
	 */
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	/**
	 * Returns the price of the {@link Product}.
	 * 
	 * @return
	 */
	public BigDecimal getPrice() {
		return price;
	}
}
