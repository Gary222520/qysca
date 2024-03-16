DROP TABLE IF EXISTS bu;
CREATE TABLE bu (
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

DROP TABLE IF EXISTS employee;
CREATE TABLE employee(
	id VARCHAR(32) PRIMARY KEY,
	uid VARCHAR(255) UNIQUE NOT NULL,
	name VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	phone VARCHAR(255) NOT NULL,
    bu_id VARCHAR(32) NOT NULL,
    foreign key (bu_id) references bu(id)
);

DROP TABLE IF EXISTS application;
CREATE TABLE application(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(2000) DEFAULT NULL,
	language VARCHAR(255) DEFAULT NULL,
	type VARCHAR(255) NOT NULL,
	builder VARCHAR(255) DEFAULT NULL,
	scanner VARCHAR(255) DEFAULT NULL,
	state VARCHAR(255) NOT NULL,
	time VARCHAR(255) NOT NULL,
    lock Boolean NOT NULL,
	release Boolean NOT NULL,
    root Boolean NOT NULL,
    creator VARCHAR(32) NOT NULL,
    bu_id VARCHAR(32) NOT NULL,
    child_application text[],
    child_component text[],
	UNIQUE(group_id,artifact_id,version),
    foreign key (bu_id) references bu(id)
);

DROP TABLE IF EXISTS application_member;
create table application_member(
	id VARCHAR(32) PRIMARY KEY,
    application_id VARCHAR(32) NOT NULL,
    employee_id VARCHAR(32) NOT NULL,
    role VARCHAR(255) NOT NULL,
    foreign key (application_id) references application(id),
    foreign key (employee_id) references employee(uid)
);

DROP TABLE IF EXISTS component;
CREATE TABLE component(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255),
	language VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	description VARCHAR(2000),
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
    creator VARCHAR(32) DEFAULT NULL,
	developers JSONB,
	licenses JSONB,
	hashes JSONB,
	UNIQUE(group_id,artifact_id,version)
);

DROP TABLE IF EXISTS dependency_tree;
CREATE TABLE dependency_tree(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	tree JSONB,
	UNIQUE(group_id,artifact_id,version)
);

DROP TABLE IF EXISTS dependency_table;
CREATE TABLE dependency_table(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	c_group_id VARCHAR(255) NOT NULL,
	c_artifact_id VARCHAR(255) NOT NULL,
	c_version VARCHAR(255) NOT NULL,
	scope VARCHAR(255),
	depth INTEGER NOT NULL,
	direct BOOLEAN NOT NULL,
	type VARCHAR(255) NOT NULL,
	language VARCHAR(255) NOT NULL
);
