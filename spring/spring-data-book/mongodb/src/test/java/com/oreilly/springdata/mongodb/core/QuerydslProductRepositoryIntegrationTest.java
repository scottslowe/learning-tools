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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mysema.query.types.Predicate;
import com.oreilly.springdata.mongodb.AbstractIntegrationTest;

/**
 * Integration test showing the usage of Querydsl {@link Predicate} to query repositories implementing
 * {@link QueryDslPredicateExecutor}.
 * 
 * @author Oliver Gierke
 */
public class QuerydslProductRepositoryIntegrationTest extends AbstractIntegrationTest {

	static final QProduct product = QProduct.product;

	@Autowired
	ProductRepository repository;

	@Test
	public void findProductsByQuerydslPredicate() {

		Product iPad = repository.findOne(product.name.eq("iPad"));
		Predicate tablets = product.description.contains("tablet");

		Iterable<Product> result = repository.findAll(tablets);
		assertThat(result, is(Matchers.<Product> iterableWithSize(1)));
		assertThat(result, hasItem(iPad));
	}
}
