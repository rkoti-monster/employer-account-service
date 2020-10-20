DROP TABLE IF EXISTS USER;

CREATE TABLE USER (
    id INT,
    name VARCHAR(100)
);

DROP TABLE IF EXISTS ACCOUNT;

CREATE TABLE ACCOUNT (
    id VARCHAR(100),
    name VARCHAR(100),
    contact_id VARCHAR(100) NULL,
    zuora_id varchar(100) NULL,
    crm_id varchar(100) NULL,
    website varchar(100) NULL,
    parent_account_id varchar(100) NULL,
    address varchar(100) NULL
);