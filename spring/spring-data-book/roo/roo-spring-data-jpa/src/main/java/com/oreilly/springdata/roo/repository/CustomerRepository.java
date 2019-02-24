package com.oreilly.springdata.roo.repository;

import com.oreilly.springdata.roo.domain.Customer;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = Customer.class)
public interface CustomerRepository {
}
