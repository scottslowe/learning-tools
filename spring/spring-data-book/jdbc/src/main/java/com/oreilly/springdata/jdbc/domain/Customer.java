package com.oreilly.springdata.jdbc.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class Customer extends AbstractEntity {

	private String firstName;
	private String lastName;
	private EmailAddress emailAddress;
	private Set<Address> addresses = new HashSet<Address>();

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Set<Address> getAddresses() {
		return Collections.unmodifiableSet(addresses);
	}

	public void addAddress(Address address) {
		this.addresses.add(address);
	}

	public void clearAddresses() {
		this.addresses.clear();
	}

	@Override
	public String toString() {
		return "Customer: [" + getId() + "] " + firstName + " " + lastName + " " + emailAddress + " " + addresses;
	}
}
