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

import com.oreilly.springdata.neo4j.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.Order;
import org.neo4j.cypherdsl.grammar.Execute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;

import static com.oreilly.springdata.neo4j.core.CoreMatchers.named;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.neo4j.cypherdsl.CypherQuery.lookup;
import static org.neo4j.cypherdsl.CypherQuery.start;
import static org.neo4j.cypherdsl.querydsl.CypherQueryDSL.*;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Integration tests for {@link ProductRepository}.
 * 
 * @author Oliver Gierke
 */
public class ProductRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	ProductRepository repository;

	@Test
	public void createProduct() {

		Product product = new Product("Camera bag");
		Product result = repository.save(product);

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is(product.getName()));
	}

	@Test
	public void findProductsInCustomerOrders() {

        QProduct product = QProduct.product;
        QCustomer customer = QCustomer.customer;
        final Identifier c = identifier(customer);
        final Identifier p = identifier(product);
        final Identifier item = identifier("item");
        final Identifier index = identifier(Customer.class.getSimpleName());
        Execute query = start(lookup(c, index, identifier(customer.emailAddress), param("email"))).
                match(node(c).in("customer").node().out("ITEMS").as(item).node(p)).
                where(toBooleanExpression(product.price.gt(400))).
                returns(p).
                orderBy(order(property(product.name), Order.ASCENDING));

        @SuppressWarnings("unchecked") final List<Product> result = repository.query(query, map("email", dave.getEmailAddress())).as(List.class);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getName(), is(mbp.getName()));
        assertThat(result.get(1).getName(), is(iPad.getName()));
    }

	@Test
	@SuppressWarnings("unchecked")
	public void lookupProductsByDescription() {

		Pageable pageable = new PageRequest(0, 1, Direction.DESC, "product.name"); // TODO JIRA
		Page<Product> page = repository.findByDescriptionLike("Apple", pageable); // TODO JIRA findByDescriptionContaining

		assertThat(page.getContent(), hasSize(1));
		assertThat(page, Matchers.<Product>hasItems(named("iPad")));
		assertThat(page.isFirstPage(), is(true));
		//assertThat(page.isLastPage(), is(false)); // TODO JIRA
		//assertThat(page.hasNextPage(), is(true));
	}
    
    @Test
    public void testListRanked() {
        template.save(new Rating(dave,mbp, 4, "Great Product"));
        template.save(new Rating(dave, iPad, 5, "Replaced MBP"));
/*
        final Page<Product> products = repository.listProductsRanked("description:Apple", new PageRequest(0, 10));
        assertEquals(2,products.getNumberOfElements());
        assertEquals(iPad,products.getContent().get(0));
        assertEquals(mbp,products.getContent().get(1));
*/
    }
}
