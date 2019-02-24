package com.oreilly.springdata.roo.repository;

import com.oreilly.springdata.roo.domain.Address;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = Address.class)
public interface AddressRepository {
}
