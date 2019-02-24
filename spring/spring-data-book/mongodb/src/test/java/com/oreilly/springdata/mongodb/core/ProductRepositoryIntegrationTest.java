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
package com.oreilly.springdata.mongodb.core;

import static com.oreilly.springdata.mongodb.core.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.oreilly.springdata.mongodb.AbstractIntegrationTest;

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

		Product product = new Product("Camera bag", new BigDecimal(49.99));
		product = repository.save(product);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void lookupProductsByDescription() {

		Pageable pageable = new PageRequest(0, 1, Direction.DESC, "name");
		Page<Product> page = repository.findByDescriptionContaining("Apple", pageable);

		assertThat(page.getContent(), hasSize(1));
		assertThat(page, Matchers.<Product> hasItems(named("iPad")));
		assertThat(page.isFirstPage(), is(true));
		assertThat(page.isLastPage(), is(false));
		assertThat(page.hasNextPage(), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findsProductsByAttributes() {

		List<Product> products = repository.findByAttributes("attributes.connector", "plug");

		assertThat(products, Matchers.<Product> hasItems(named("Dock")));
	}
}
