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
package com.oreilly.springdata.rest.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreilly.springdata.rest.order.LineItem;

/**
 * Client side configuration setting up a basic {@link RestTemplate}.
 * 
 * @author Oliver Gierke
 */
@Configuration
public class ClientConfiguration {

	static final String BASE_URL = "http://localhost:8080";

	static final String ORDERS_REL = "order";
	static final String CUSTOMERS_REL = "customer";
	static final String PRODUCTS_REL = "product";

	// TODO: actually needs .Customer prepended
	static final String ORDER_CUSTOMER_REL = "order.Order.customer";

	/**
	 * Configures the Jackson {@link ObjectMapper} to ignore unknown properties on the client side. E.g.
	 * {@link LineItem#getTotal()} causes Jackson to consider {@code total} a property and fails to bind the object as
	 * there's no setter accepting a value.
	 * 
	 * @return
	 */
	@Bean
	public RestOperations restOperations() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(mapper);
		converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));

		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(converter);

		RestTemplate template = new RestTemplate();
		template.setMessageConverters(converters);

		return template;
	}
}
