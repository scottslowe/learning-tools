package com.oreilly.springdata.jdbc.repository;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.Tuple;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.MappingProjection;
import com.mysema.query.types.QBean;
import com.oreilly.springdata.jdbc.TestConfig;
import com.oreilly.springdata.jdbc.domain.Address;
import com.oreilly.springdata.jdbc.domain.Customer;
import com.oreilly.springdata.jdbc.domain.EmailAddress;
import com.oreilly.springdata.jdbc.domain.QAddress;
import com.oreilly.springdata.jdbc.domain.QCustomer;

/**
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@DirtiesContext
public class QueryDslCustomerRepositoryTest {

	@Autowired
	CustomerRepository repository;

	@Autowired
	DataSource dataSource;

	@Test
	public void testFindAll() {
		List<Customer> results = repository.findAll();
		assertThat(results, is(notNullValue()));
		assertThat(results, hasSize(3));
		assertThat(results.get(0), notNullValue());
		assertThat(results.get(1), notNullValue());
		assertThat(results.get(2), notNullValue());
	}

	@Test
	public void testFindById() {
		Customer result = repository.findById(100L);
		assertThat(result, is(notNullValue()));
		assertThat(result.getFirstName(), is("John"));
	}

	@Test
	public void testFindByEmail() {
		Customer result = repository.findByEmailAddress(new EmailAddress("bob@doe.com"));
		assertThat(result, is(notNullValue()));
		assertThat(result.getFirstName(), is("Bob"));
	}

	@Test
	public void saveNewCustomer() {
		Customer c = new Customer();
		c.setFirstName("Sven");
		c.setLastName("Svensson");
		c.setEmailAddress(new EmailAddress("sven@svensson.org"));
		Address a = new Address("Storgaten 6", "Trosa", "Sweden");
		c.addAddress(a);
		repository.save(c);
		System.out.println(repository.findAll());
		Customer result = repository.findById(c.getId());
		assertThat(result, is(notNullValue()));
		assertThat(result.getFirstName(), is("Sven"));
		assertThat(result.getEmailAddress().toString(), is(notNullValue()));
	}

	@Test
	public void saveNewCustomerWithoutEmail() {
		Customer c = new Customer();
		c.setFirstName("Sven");
		c.setLastName("Svensson");
		Address a = new Address("Storgaten 6", "Trosa", "Sweden");
		c.addAddress(a);
		repository.save(c);
		System.out.println(repository.findAll());
		Customer result = repository.findById(c.getId());
		assertThat(result, is(notNullValue()));
		assertThat(result.getFirstName(), is("Sven"));
		assertThat(result.getEmailAddress(), is(nullValue()));
	}

	@Test(expected = DuplicateKeyException.class)
	public void saveNewCustomerWithDuplicateEmail() {
		Customer c = new Customer();
		c.setFirstName("Bob");
		c.setLastName("Doe");
		c.setEmailAddress(new EmailAddress("bob@doe.com"));
		Address a = new Address("66 Main St", "Middletown", "USA");
		c.addAddress(a);
		repository.save(c);
	}

	@Test
	public void deleteCustomer() {
		Customer c = repository.findById(100L);
		repository.delete(c);
		Customer result = repository.findById(100L);
		assertThat(result, is(nullValue()));
	}

	@Test
	public void directQueryDslUseExtractingList() {

		Connection connection = DataSourceUtils.getConnection(dataSource);
		QAddress qAddress = QAddress.address;
		SQLTemplates dialect = new HSQLDBTemplates();
		SQLQuery query = new SQLQueryImpl(connection, dialect).from(qAddress).where(qAddress.city.eq("London"));
		List<Address> results = query.list(new QBean<Address>(Address.class, qAddress.street, qAddress.city,
				qAddress.country));
		DataSourceUtils.releaseConnection(connection, dataSource);
		assertThat(results, is(notNullValue()));
		assertThat(results, hasSize(1));
		assertThat(results.get(0), notNullValue());
		assertThat(results.get(0).getCity(), is("London"));
	}

	@Test
	public void directQueryDslUseExtractingResultSet() throws SQLException {

		Connection connection = DataSourceUtils.getConnection(dataSource);
		final QAddress qAddress = QAddress.address;
		SQLTemplates dialect = new HSQLDBTemplates();
		SQLQuery query = new SQLQueryImpl(connection, dialect);
		ResultSet rs = query.from(qAddress).where(qAddress.city.eq("London"))
				.getResults(qAddress.street, qAddress.city, qAddress.country);
		List<Address> results = new ArrayList<Address>();
		while (rs.next()) {
			results.add(new Address(rs.getString(qAddress.street.toString()), rs.getString(qAddress.city.toString()), rs
					.getString(qAddress.country.toString())));
		}
		DataSourceUtils.releaseConnection(connection, dataSource);
		assertThat(results, is(notNullValue()));
		assertThat(results, hasSize(1));
		assertThat(results.get(0), notNullValue());
		assertThat(results.get(0).getCity(), is("London"));
	}

	@Test
	public void templateWithMappingExample() {
		QueryDslJdbcTemplate qdslTemplate = new QueryDslJdbcTemplate(dataSource);
		final QAddress qAddress = QAddress.address;
		SQLQuery addressQuery = qdslTemplate.newSqlQuery().from(qAddress).where(qAddress.city.eq("London"));
		List<Address> results = qdslTemplate.query(addressQuery, BeanPropertyRowMapper.newInstance(Address.class),
				qAddress.street, qAddress.city, qAddress.country);
		assertThat(results, is(notNullValue()));
		assertThat(results, hasSize(1));
		assertThat(results.get(0), notNullValue());
		assertThat(results.get(0).getCity(), is("London"));
	}

	@Test
	public void templateWithMappingProjectionExample() {
		final QCustomer customer = new QCustomer("c");

		QueryDslJdbcTemplate template = new QueryDslJdbcTemplate(dataSource);
		List<Customer> results = template.query(template.newSqlQuery().from(customer).where(customer.id.eq(100L)),
				new MappingProjection<Customer>(Customer.class, customer.all()) {
					@Override
					protected Customer map(Tuple row) {
						Customer c = new Customer();
						c.setId(row.get(customer.id));
						c.setFirstName(row.get(customer.firstName));
						c.setLastName(row.get(customer.lastName));
						if (!row.get(customer.emailAddress).isEmpty()) {
							c.setEmailAddress(new EmailAddress(row.get(customer.emailAddress)));
						}
						return c;
					}
				});
		assertThat(results, is(notNullValue()));
		assertThat(results, hasSize(1));
		assertThat(results.get(0), notNullValue());
		assertThat(results.get(0).getFirstName(), is("John"));
		assertThat(results.get(0).getEmailAddress().toString(), is("john@doe.com"));
	}
}
