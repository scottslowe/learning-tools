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
package com.oreilly.springdata.querydsl.core;

import static com.mysema.query.collections.MiniApi.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests to show the usage of Querydsl predicates to filter collections.
 * 
 * @author Oliver Gierke
 */
public class ProductUnitTest {

	private static final QProduct $ = QProduct.product;

	Product macBook, iPad, iPod, turntable;

	List<Product> products;

	@Before
	public void setUp() {

		macBook = new Product("MacBook Pro", "Apple laptop");
		iPad = new Product("iPad", "Apple tablet");
		iPod = new Product("iPod", "Apple MP3 player");
		turntable = new Product("Turntable", "Vinyl player");

		products = Arrays.asList(macBook, iPad, iPod, turntable);
	}

	@Test
	public void findsAllAppleProducts() {

		List<Product> result = from($, products).where($.description.contains("Apple")).list($);

		assertThat(result, hasSize(3));
		assertThat(result, hasItems(macBook, iPad, iPod));
	}

	@Test
	public void findsAllAppleProductNames() {

		List<String> result = from($, products).where($.description.contains("Apple")).list($.name);

		assertThat(result, hasSize(3));
		assertThat(result, hasItems(macBook.getName(), iPad.getName(), iPod.getName()));
	}

	@Test
	public void findsPlayers() {

		List<Product> result = from($, products).where($.description.contains("player")).list($);

		assertThat(result, hasSize(2));
		assertThat(result, hasItems(iPod, turntable));
	}
}
