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
package com.oreilly.springdata.neo4j.core;

import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.PrefixedDynamicProperties;
import org.springframework.data.neo4j.support.index.IndexType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Product extends AbstractEntity {
    @Indexed(unique = true)
	private String name;
    @Indexed(indexType = IndexType.FULLTEXT,indexName = "product_search")
	private String description;
    @GraphProperty(propertyType = double.class)
	private BigDecimal price;

    @RelatedTo
	private Set<Tag> tags = new HashSet<Tag> ();

    private DynamicProperties attributes=new PrefixedDynamicProperties("attributes");

	public Product(String name) {
		this(name, null);
	}

	public Product(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Product() {

	}

	public void setAttribute(String key, String value) {
        this.attributes.setProperty(key, value);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

    public DynamicProperties getAttributes() {
		return attributes;
	}

	public BigDecimal getPrice() {
		return price;
	}

    public Product withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
