CREATE SCHEMA IF NOT EXISTS users;
USE users;
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
    address varchar(100) NULL);
DROP USER 'user'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'user';
GRANT ALL ON users.* TO 'user'@'%';