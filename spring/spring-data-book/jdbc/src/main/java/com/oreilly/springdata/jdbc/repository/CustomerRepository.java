package com.oreilly.springdata.jdbc.repository;

import com.oreilly.springdata.jdbc.domain.Customer;
import com.oreilly.springdata.jdbc.domain.EmailAddress;

import java.util.List;

/**
 * @author Thomas Risberg
 */
public interface CustomerRepository {

	Customer findById(Long id);

	List<Customer> findAll();

	void save(Customer customer);

	void delete(Customer customer);

	Customer findByEmailAddress(EmailAddress emailAddress);
}