DROP EXTENSION pg_trgm;
CREATE EXTENSION pg_trgm;
select set_limit(0.95);

DROP TABLE IF EXISTS plt_vulnerability_cnvd;
CREATE TABLE plt_vulnerability_cnvd(
	id VARCHAR(32) PRIMARY KEY,
	cnvd_id VARCHAR(255) NOT NULL UNIQUE,
	cve_id VARCHAR(255),
	severity VARCHAR(255),
	title TEXT,
	type VARCHAR(255),
	submit_time VARCHAR(255),
	open_time VARCHAR(255),
	discoverer TEXT,
	reference TEXT,
	suggestion TEXT,
	description TEXT,
	patch_name TEXT,
	patch_description TEXT
);
CREATE INDEX cnvd_cnvd_id_index ON plt_vulnerability_cnvd(cnvd_id);
CREATE INDEX cnvd_cve_id_index ON plt_vulnerability_cnvd(cve_id);

DROP TABLE IF EXISTS plt_vulnerability_cnvd_product;
CREATE TABLE plt_vulnerability_cnvd_product(
	id VARCHAR(32) PRIMARY KEY,
	cnvd_id VARCHAR(255) NOT NULL,
	product TEXT NOT NULL
);
CREATE INDEX cp_cnvd_id_index ON plt_vulnerability_cnvd_product(cnvd_id);
CREATE INDEX cp_product_index ON plt_vulnerability_cnvd_product(product);

DROP TABLE IF EXISTS plt_vulnerability_cwe;
CREATE TABLE plt_vulnerability_cwe(
	id VARCHAR(32) PRIMARY KEY,
	cwe_id VARCHAR(255) NOT NULL UNIQUE,
	name TEXT,
	weakness_abstraction VARCHAR(255),
	status VARCHAR(255),
	description TEXT,
	extended_description TEXT
);

DROP TABLE IF EXISTS plt_vulnerability_cve;
CREATE TABLE plt_vulnerability_cve(
	id VARCHAR(32) PRIMARY KEY,
	cve_id VARCHAR(255) NOT NULL UNIQUE,
	cve_assigner VARCHAR(255),
	problem_type VARCHAR[],
	ref JSONB,
	descriptions TEXT[],
	cvss3 JSONB,
	cvss2 JSONB,
	published_date VARCHAR(255),
	last_modified_date VARCHAR(255)
);

DROP TABLE IF EXISTS plt_vulnerability_cve_cpe CASCADE;
CREATE TABLE plt_vulnerability_cve_cpe(
	id VARCHAR(32) PRIMARY KEY,
	cve_id VARCHAR(255) NOT NULL,
	uri VARCHAR NOT NULL,
	vulnerable BOOLEAN,
	version_start VARCHAR(255),
	version_end VARCHAR(255),
	start_include BOOLEAN,
	end_include BOOLEAN
) PARTITION BY HASH(id);

do language plpgsql $$    
declare    
begin    
  for i in 0..63    
  loop    
    execute format('drop table if exists plt_vulnerability_cve_cpe%s', i);
    execute format('create table plt_vulnerability_cve_cpe%s partition of plt_vulnerability_cve_cpe for values with (modulus 64,remainder %s)', i,i);             
  end loop;    
end;    
$$;
CREATE INDEX cve_id_index ON plt_vulnerability_cve_cpe(cve_id);
CREATE INDEX vp_uri_index ON plt_vulnerability_cve_cpe(uri);
CREATE INDEX cc_gin_uri_index ON plt_vulnerability_cve_cpe USING gin(uri gin_trgm_ops);

DROP TABLE IF EXISTS plt_vulnerability_cpe_match;
CREATE TABLE plt_vulnerability_cpe_match(
	id VARCHAR(32) PRIMARY KEY,
	uri VARCHAR NOT NULL,
	version_start VARCHAR(255),
	version_end VARCHAR(255),
	start_include BOOLEAN,
	end_include BOOLEAN,
	names VARCHAR[]
);
CREATE INDEX uri_index ON plt_vulnerability_cpe_match(uri);

DROP TABLE IF EXISTS plt_go_component;
CREATE TABLE plt_go_component(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	language VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	description TEXT,
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
    creator VARCHAR(32),
	licenses TEXT[],
    vulnerabilities TEXT[],
	state VARCHAR(32) NOT NULL,
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_go_dependency_tree;
CREATE TABLE plt_go_dependency_tree(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	tree JSONB,
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_go_dependency_table;
CREATE TABLE plt_go_dependency_table(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	c_name VARCHAR(255) NOT NULL,
	c_version VARCHAR(255) NOT NULL,
	depth INTEGER NOT NULL,
	direct BOOLEAN NOT NULL,
	type VARCHAR(255) NOT NULL,
	language VARCHAR(255) NOT NULL,
    licenses VARCHAR(255) NOT NULL,
    vulnerabilities VARCHAR(255) NOT NULL
);

DROP TABLE IF EXISTS plt_python_component;
CREATE TABLE plt_python_component(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    description TEXT,
    url VARCHAR(255),
    download_url VARCHAR(255),
    source_url VARCHAR(255),
    p_url VARCHAR(255),
    author TEXT,
    author_email TEXT,
    licenses TEXT[],
	vulnerabilities TEXT[],
    creator VARCHAR(32),
    state VARCHAR(32) NOT NULL,
    UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_python_dependency_tree;
CREATE TABLE plt_python_dependency_tree(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    tree JSONB,
    UNIQUE(name,version)
);


DROP TABLE IF EXISTS plt_python_dependency_table;
CREATE TABLE plt_python_dependency_table(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    c_name VARCHAR(255) NOT NULL,
    c_version VARCHAR(255) NOT NULL,
    depth INTEGER NOT NULL,
    direct BOOLEAN NOT NULL,
    type VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
	licenses VARCHAR(255) NOT NULL,
    vulnerabilities VARCHAR(255) NOT NULL
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
	description TEXT,
	language VARCHAR(255),
	type VARCHAR(255) NOT NULL,
	builder VARCHAR(255),
	scanner VARCHAR(255),
	state VARCHAR(255) NOT NULL,
    licenses TEXT[],
	vulnerabilities TEXT[],
	time VARCHAR(255) NOT NULL,
    lock Boolean NOT NULL,
	release Boolean NOT NULL,
    creator VARCHAR(32) NOT NULL,
    child_application text[],
    child_component JSONB,
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_java_component;
CREATE TABLE plt_java_component(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	j_name VARCHAR(255),
	language VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	description TEXT,
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
    creator VARCHAR(32),
	developers JSONB,
	licenses TEXT[],
    vulnerabilities TEXT[],
	hashes JSONB,
	state VARCHAR(32) NOT NULL,
	UNIQUE(name,version)
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

DROP TABLE IF EXISTS plt_java_dependency_tree;
CREATE TABLE plt_java_dependency_tree(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	tree JSONB,
	UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_java_dependency_table;
CREATE TABLE plt_java_dependency_table(
	id VARCHAR(32) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	c_name VARCHAR(255) NOT NULL,
	c_version VARCHAR(255) NOT NULL,
	scope VARCHAR(255),
	depth INTEGER NOT NULL,
	direct BOOLEAN NOT NULL,
	type VARCHAR(255) NOT NULL,
	language VARCHAR(255) NOT NULL,
	licenses VARCHAR(255) NOT NULL,
    vulnerabilities TEXT NOT NULL
);

DROP TABLE IF EXISTS plt_license;
CREATE TABLE plt_license(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(255),
    is_osi_approved BOOLEAN,
    is_fsf_approved BOOLEAN,
    is_spdx_approved BOOLEAN,
    risk_level VARCHAR(255),
    risk_disclosure TEXT,
    gpl_compatibility BOOLEAN,
    gpl_compatibility_description TEXT,
    obligations_required JSONB,
    obligations_not_required JSONB,
    rights_allowed JSONB,
    rights_prohibited JSONB,
    text TEXT
);

DROP TABLE IF EXISTS plt_js_component;
create table plt_js_component (
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    description TEXT,
    url VARCHAR(255),
    source_url VARCHAR(255),
    copyright_statements TEXT[],
    p_url VARCHAR(255),
    licenses TEXT[],
    vulnerabilities TEXT[],
    download_url VARCHAR(255),
    language VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    creator VARCHAR(32),
    state VARCHAR(32) NOT NULL,
    UNIQUE(name,version)
);

DROP TABLE IF EXISTS plt_js_dependency_tree;
CREATE TABLE plt_js_dependency_tree (
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    tree JSONB,
    UNIQUE(name,version)
);
DROP TABLE IF EXISTS plt_js_dependency_table;
CREATE TABLE plt_js_dependency_table (
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    c_name VARCHAR(255) NOT NULL,
    c_version VARCHAR(255) NOT NULL,
    depth INTEGER NOT NULL,
    direct BOOLEAN NOT NULL,
    type VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
	licenses VARCHAR(255) NOT NULL,
    vulnerabilities VARCHAR(255) NOT NULL
);

DROP TABLE IF EXISTS plt_app_component;
CREATE TABLE plt_app_component(
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    language TEXT[],
    type VARCHAR(255) NOT NULL,
    description TEXT,
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
    developers JSONB,
    licenses TEXT[],
    vulnerabilities TEXT[],
    creator VARCHAR(32),
    UNIQUE(name, version)
);

DROP TABLE IF EXISTS plt_app_dependency_tree;
CREATE TABLE plt_app_dependency_tree(
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    tree JSONB,
    UNIQUE(name, version)
);

DROP TABLE IF EXISTS plt_app_dependency_table;
CREATE TABLE plt_app_dependency_table(
	id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    c_name VARCHAR(255) NOT NULL,
    c_version VARCHAR(255) NOT NULL,
    depth INTEGER NOT NULL,
    direct BOOLEAN NOT NULL,
    type VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
	licenses VARCHAR(255) NOT NULL,
    vulnerabilities TEXT NOT NULL
);

    
CREATE TABLE IF NOT EXISTS plt_visited_maven_urls(
    id VARCHAR(32) PRIMARY KEY,
    url VARCHAR(255) NOT NULL UNIQUE,
    is_success BOOLEAN NOT NULL,
    is_last_level BOOLEAN NOT NULL,
    is_dependencies_crawled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS plt_visited_python_packages(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    visited BOOLEAN NOT NULL,
    is_success BOOLEAN NOT NULL,
    UNIQUE(name,version)
);

CREATE TABLE IF NOT EXISTS plt_visited_npm_packages(
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    visited BOOLEAN NOT NULL,
    is_success BOOLEAN NOT NULL,
    UNIQUE(name,version)
);

