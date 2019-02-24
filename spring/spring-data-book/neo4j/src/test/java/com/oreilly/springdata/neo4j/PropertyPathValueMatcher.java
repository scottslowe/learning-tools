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
package com.oreilly.springdata.neo4j;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class PropertyPathValueMatcher extends TypeSafeMatcher<Object> {

	private final String propertyPath;
	private final Object expected;

	public PropertyPathValueMatcher(String propertyPath, Object value) {
		this.propertyPath = propertyPath;
		this.expected = value;
	}

	/* (non-Javadoc)
	 * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
	 */
	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(Object object) {

		BeanWrapper wrapper = new BeanWrapperImpl(object);
		Object value = wrapper.getPropertyValue(propertyPath);

		return expected == null ? value == null : expected.equals(value);
	}

	public static Matcher<? extends Object> withProperty(String name, Object value) {
		return new PropertyPathValueMatcher(name, value);
	}
}
