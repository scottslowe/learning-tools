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
import com.oreilly.springdata.neo4j.core.Product;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
import org.springframework.util.Assert;

@RelationshipEntity(type = "ITEMS")
public class LineItem extends AbstractEntity {

	@StartNode
	private Order order;
    @Fetch
    @EndNode
	private Product product;

	private int amount;

	public LineItem(Order order, Product product) {
		this(order,product, 1);
	}

	public LineItem(Order order, Product product, int amount) {
        Assert.notNull(product);
        Assert.notNull(order);

        this.order = order;
		this.product = product;
		this.amount = amount;
	}

	public LineItem() {

	}

	public Product getProduct() {
		return product;
	}

    public Order getOrder() {
        return order;
    }

    public int getAmount() {
		return amount;
	}
}
