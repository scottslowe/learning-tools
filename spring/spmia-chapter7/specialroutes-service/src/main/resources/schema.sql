DROP TABLE IF EXISTS abtesting;

CREATE TABLE abtesting (
  service_name      VARCHAR(100) PRIMARY KEY NOT NULL,
  active            VARCHAR(1) NOT NULL,
  endpoint          VARCHAR(100) NOT NULL,
  weight            INT);


INSERT INTO abtesting (service_name, active,  endpoint, weight) VALUES ('organizationservice', 'Y','http://orgservice-new:8087',5);
