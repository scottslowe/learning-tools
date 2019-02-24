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
package com.oreilly.springdata.mongodb;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.Mongo;

/**
 * Base class for integration tests adding some sample data through the MongoDB Java driver.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public abstract class AbstractIntegrationTest {

	@Autowired
	Mongo mongo;

	@Before
	public void setUp() {

		DB database = mongo.getDB("e-store");

		// Customers

		DBCollection customers = database.getCollection("customer");
		customers.remove(new BasicDBObject());

		BasicDBObject address = new BasicDBObject();
		address.put("city", "New York");
		address.put("street", "Broadway");
		address.put("country", "United States");

		BasicDBList addresses = new BasicDBList();
		addresses.add(address);

		DBObject dave = new BasicDBObject("firstname", "Dave");
		dave.put("lastname", "Matthews");
		dave.put("email", "dave@dmband.com");
		dave.put("addresses", addresses);

		customers.insert(dave);

		// Products

		DBCollection products = database.getCollection("product");
		products.drop();

		DBObject iPad = new BasicDBObject("name", "iPad");
		iPad.put("description", "Apple tablet device");
		iPad.put("price", 499.0);
		iPad.put("attributes", new BasicDBObject("connector", "plug"));

		DBObject macBook = new BasicDBObject("name", "MacBook Pro");
		macBook.put("description", "Apple notebook");
		macBook.put("price", 1299.0);

		BasicDBObject dock = new BasicDBObject("name", "Dock");
		dock.put("description", "Dock for iPhone/iPad");
		dock.put("price", 49.0);
		dock.put("attributes", new BasicDBObject("connector", "plug"));

		products.insert(iPad, macBook, dock);

		// Orders

		DBCollection orders = database.getCollection("order");
		orders.drop();

		// Line items

		DBObject iPadLineItem = new BasicDBObject("product", iPad);
		iPadLineItem.put("amount", 2);

		DBObject macBookLineItem = new BasicDBObject("product", macBook);
		macBookLineItem.put("amount", 1);

		BasicDBList lineItems = new BasicDBList();
		lineItems.add(iPadLineItem);
		lineItems.add(macBookLineItem);

		DBObject order = new BasicDBObject("customer", new DBRef(database, "customer", dave.get("_id")));
		order.put("lineItems", lineItems);
		order.put("shippingAddress", address);

		orders.insert(order);
	}
}
