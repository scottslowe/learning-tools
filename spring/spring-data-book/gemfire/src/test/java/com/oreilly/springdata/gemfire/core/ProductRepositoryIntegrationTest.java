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

import static com.oreilly.springdata.gemfire.core.CoreMatchers.named;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.oreilly.springdata.gemfire.AbstractIntegrationTest;

/**
 * Integration tests for {@link ProductRepository}.
 * 
 * @author Oliver Gierke
 * @author David Turanski
 */
public class ProductRepositoryIntegrationTest extends AbstractIntegrationTest {	 

	@Test
	public void createProduct() {

		Product product = new Product(4L, "Camera bag", new BigDecimal(49.99));
		product = productRepository.save(product);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void lookupProductsByDescription() {

		 
		List<Product> products = productRepository.findByDescriptionContaining("Apple");
		assertThat(products, hasSize(2));
		assertThat(products, Matchers.<Product> hasItems(named("iPad")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findsProductsByAttributes() {

		List<Product> products = productRepository.findByAttributes("connector", "plug");
		assertThat(products, hasSize(1));
		assertThat(products, Matchers.<Product> hasItems(named("Dock")));
	}
}
