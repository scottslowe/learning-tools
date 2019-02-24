INSERT INTO customer(id, first_name, last_name, email_address) VALUES(100, 'John', 'Doe', 'john@doe.com');
INSERT INTO customer(id, first_name, last_name, email_address) VALUES(101, 'Jane', 'Doe', 'jane@doe.com');
INSERT INTO customer(id, first_name, last_name, email_address) VALUES(102, 'Bob', 'Doe', 'bob@doe.com');
ALTER TABLE customer ALTER COLUMN id RESTART WITH 200;
INSERT INTO address(customer_id, street, city, country) VALUES(100, '6 Main St', 'Newtown', 'USA');
INSERT INTO address(customer_id, street, city, country) VALUES(100, '128 N. South St', 'Middletown', 'USA');
INSERT INTO address(customer_id, street, city, country) VALUES(102, '512 North St', 'London', 'UK');
