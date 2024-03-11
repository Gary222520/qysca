DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role(
	id VARCHAR(32) PRIMARY KEY,
	uid VARCHAR(255) UNIQUE NOT NULL,
	name VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	phone VARCHAR(255) NOT NULL,
	role VARCHAR(255) NOT NULL
);

INSERT INTO user_role
VALUES(
	'00000000000000000000000000000000',
	'000000000',
	'admin',
	'admin',
	'1319091298@qq.com',
	'13861982308',
	'Admin'
);


DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS application;

CREATE TABLE application(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255),
	description VARCHAR(2000),
	UNIQUE(group_id,artifact_id,version)
);

CREATE TABLE project(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255),
	description VARCHAR(2000),
	language VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	builder VARCHAR(255) NOT NULL,
	scanner VARCHAR(255) NOT NULL,
	state VARCHAR(255) NOT NULL,
	time VARCHAR(255) NOT NULL,
	a_p_id VARCHAR(32) NOT NULL,
	FOREIGN KEY(a_p_id) REFERENCES application(id),
	UNIQUE(group_id,artifact_id,version)
);

DROP TABLE IF EXISTS component;
CREATE TABLE component(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255),
	language VARCHAR(255) NOT NULL,
	opensource BOOLEAN NOT NULL,
	description VARCHAR(2000),
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	p_url VARCHAR(255),
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
	opensource BOOLEAN NOT NULL,
	language VARCHAR(255) NOT NULL
);


INSERT INTO application
VALUES(
	'1',
	'g1',
	'a1',
	'v1',
	'a',
	'aaaaaa'
);

INSERT INTO project
VALUES(
	'1',
	'g1',
	'a1',
	'v1',
	'a',
	'aaaaaa',
	'ss',
	'sss',
	'ssfwrew',
	'jojr',
	'fwerw',
	'weirojw',
	'1'
);

INSERT INTO dependency_table
VALUES(
	'1',
	'g1',
	'a1',
	'v1',
	'cg1',
	'ca1',
	'cv1',
	'compile',
	1,
	true,
	true,
	'java'
);

INSERT INTO dependency_table
VALUES(
	'2',
	'g1',
	'a1',
	'v1',
	'cg2',
	'ca2',
	'cv2',
	'compile',
	2,
	true,
	false,
	'java'
);

INSERT INTO dependency_tree
VALUES(
	'1',
	'g1',
	'a1',
	'v1',
	'{"groupId":"g0","artifactId":"a0","version":"v0","depth":0,"opensource":true,"dependencies":[{"groupId":"g1","artifactId":"a1","opensource":true,"version":"v1","depth":1,"dependencies":{}},{"groupId":"g2","artifactId":"a2","version":"v2","opensource":true,"depth":1,"dependencies":{}}]}'
);


INSERT INTO component
VALUES (
    '1',
	'g1',
	'a1',
	'v1',
	'g1a1v1',
	'java',
	true,
	'ok',
	'ftp',
	'http',
	'tcp',
	'xxx',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO component
VALUES (
    '2',
	'g1',
	'a1',
	'v2',
	'g1a1v1',
	'java',
	true,
	'ok',
	'ftp',
	'http',
	'tcp',
	'xxx',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO component
VALUES (
    '3',
	'g2',
	'a1',
	'v2',
	'g1a1v1',
	'java',
	true,
	'ok',
	'ftp',
	'http',
	'tcp',
	'xxx',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO component
VALUES (
    '4',
	'g2',
	'a1',
	'v88',
	'g1a1v1',
	'java',
	true,
	'ok',
	'ftp',
	'http',
	'tcp',
	'xxx',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);
