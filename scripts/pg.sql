DROP TABLE IF EXISTS components;
CREATE TABLE components(
	id VARCHAR(32) PRIMARY KEY,
	group_id VARCHAR(255) NOT NULL,
	artifact_id VARCHAR(255) NOT NULL,
	version VARCHAR(255) NOT NULL,
	name VARCHAR(255),
	language VARCHAR(255),
	description VARCHAR(2000),
	url VARCHAR(255),
	download_url VARCHAR(255),
	source_url VARCHAR(255),
	developers JSONB,
	licenses JSONB,
	hashes JSONB,
	UNIQUE(group_id,artifact_id,version)
);

INSERT INTO components
VALUES (
    '1',
	'g1',
	'a1',
	'v1',
	'g1a1v1',
	'java',
	'ok',
	'ftp',
	'http',
	'tcp',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO components
VALUES (
    '2',
	'g1',
	'a1',
	'v2',
	'g1a1v1',
	'java',
	'ok',
	'ftp',
	'http',
	'tcp',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO components
VALUES (
    '3',
	'g2',
	'a1',
	'v2',
	'g1a1v1',
	'java',
	'ok',
	'ftp',
	'http',
	'tcp',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);

INSERT INTO components
VALUES (
    '4',
	'g2',
	'a1',
	'v88',
	'g1a1v1',
	'java',
	'ok',
	'ftp',
	'http',
	'tcp',
    '[{"id":"AA","name":"AAA","email":"AAAA"},{"id":"BB","name":"BBBB","email":"BBBBB"}]',
    '[{"name":"WE","url":"sssaaa"},{"name":"fwewr","url":"wwww"}]',
    '[{"alg":"md5","content":"sfwefewewfwefwfwwewfxx"},{"alg":"sha1","content":"sdfiwfkjfjwofifnn"}]'
);
