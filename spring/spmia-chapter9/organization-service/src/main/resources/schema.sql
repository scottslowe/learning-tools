DROP TABLE IF EXISTS organizations;

CREATE TABLE organizations (
  organization_id        VARCHAR(100) PRIMARY KEY NOT NULL,
  name                   TEXT NOT NULL,
  contact_name           TEXT NOT NULL,
  contact_email          TEXT NOT NULL,
  contact_phone          TEXT   NOT NULL);


INSERT INTO organizations (organization_id, name, contact_name, contact_email, contact_phone)
VALUES ('e254f8c-c442-4ebe-a82a-e2fc1d1ff78a', 'customer-crm-co', 'Mark Balster', 'mark.balster@custcrmco.com', '823-555-1212');

INSERT INTO organizations (organization_id, name, contact_name, contact_email, contact_phone)
VALUES ('442adb6e-fa58-47f3-9ca2-ed1fecdfe86c', 'HR-PowerSuite', 'Doug Drewry','doug.drewry@hr.com', '920-555-1212');
