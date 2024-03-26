DROP TABLE IF EXISTS plt_go_component;
CREATE TABLE plt_go_component(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	language VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	description VARCHAR(2000),
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
    creator VARCHAR(32),
	licenses JSONB,
	state VARCHAR(32) NOT NULL,
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_bu;
CREATE TABLE plt_bu (
	id VARCHAR(32) PRIMARY KEY,
	bid VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS plt_bu_app;
CREATE TABLE plt_bu_app(
	id VARCHAR(32) PRIMARY KEY,
	bid VARCHAR(32) NOT NULL,
	aid VARCHAR(32) NOT NULL,
	UNIQUE(bid,aid)
);

DROP TABLE IF EXISTS plt_application;
CREATE TABLE plt_application(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	description VARCHAR(2000),
	language VARCHAR(255),
	type VARCHAR(255) NOT NULL,
	builder VARCHAR(255),
	scanner VARCHAR(255),
	state VARCHAR(255) NOT NULL,
	time VARCHAR(255) NOT NULL,
    lock Boolean NOT NULL,
	release Boolean NOT NULL,
    creator VARCHAR(32) NOT NULL,
    child_application text[],
    child_component text[],
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_component;
CREATE TABLE plt_component(
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
    creator VARCHAR(32),
	developers JSONB,
	licenses JSONB,
	hashes JSONB,
	state VARCHAR(32) NOT NULL,
	UNIQUE(group_id,artifact_id,version)
);

DROP TABLE IF EXISTS plt_user;
CREATE TABLE plt_user(
	id VARCHAR(32) PRIMARY KEY,
	uid VARCHAR(255) UNIQUE NOT NULL,
	name VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	phone VARCHAR(255) NOT NULL,
	login BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS plt_user_role;
CREATE TABLE plt_user_role(
	id VARCHAR(32) PRIMARY KEY,
	uid VARCHAR(32) NOT NULL,
	rid VARCHAR(32) NOT NULL,
	bid VARCHAR(32) NOT NULL,
	aid VARCHAR(32) NOT NULL,
	UNIQUE(uid,rid,bid,aid)
);

DROP TABLE IF EXISTS plt_role;
CREATE TABLE plt_role(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS plt_role_permission;
CREATE TABLE plt_role_permission(
	id VARCHAR(32) PRIMARY KEY,
	rid VARCHAR(32) NOT NULL,
	pid VARCHAR(32) NOT NULL,
	UNIQUE(rid,pid)
);

DROP TABLE IF EXISTS plt_permission;
CREATE TABLE plt_permission(
	id VARCHAR(32) PRIMARY KEY,
	url VARCHAR(255) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS plt_dependency_tree;
CREATE TABLE plt_dependency_tree(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	tree JSONB,
	UNIQUE(group_id,artifact_id,version)
);

DROP TABLE IF EXISTS plt_dependency_table;
CREATE TABLE plt_dependency_table(
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