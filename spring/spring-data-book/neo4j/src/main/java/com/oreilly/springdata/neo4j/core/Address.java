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

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Address extends AbstractEntity {

	private String street, city;

    private Country country;

	public Address(String street, String city, Country country) {
		this.street = street;
		this.city = city;
        this.country = country;
    }

	public Address() {

	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public Country getCountry() {
		return country;
	}
}
