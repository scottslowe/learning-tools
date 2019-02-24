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

import java.util.Collection;

public class CollectionPathMatcher extends TypeSafeMatcher<Collection<Object>> {

	private String collectionPath;
	private Matcher<?> propertyMatcher;

	public CollectionPathMatcher(String collectionPath, Matcher<?> delegate) {
		this.collectionPath = collectionPath;
		this.propertyMatcher = delegate;
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
	protected boolean matchesSafely(Collection<Object> item) {

		for (Object outer : item) {

			BeanWrapper wrapper = new BeanWrapperImpl(outer);
			Object property = wrapper.getPropertyValue(collectionPath);

			if (propertyMatcher.matches(property)) {
				return true;
			}
		}

		return false;
	}

	public static Matcher<Collection<Object>> hasElement(String path, Matcher<?> matcher) {
		return new CollectionPathMatcher(path, matcher);
	}
}
