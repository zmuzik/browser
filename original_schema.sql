CREATE TABLE _sync_state (
	_id INTEGER PRIMARY KEY,
	account_name TEXT NOT NULL,
	account_type TEXT NOT NULL,
	data TEXT,
	UNIQUE(account_name, account_type)
);

CREATE TABLE _sync_state_metadata (
	version INTEGER
);

CREATE TABLE android_metadata (
	locale TEXT
);

CREATE TABLE bookmarks(
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT,
	url TEXT,
	folder INTEGER NOT NULL DEFAULT 0,
	parent INTEGER,
	position INTEGER NOT NULL,
	insert_after INTEGER,
	deleted INTEGER NOT NULL DEFAULT 0,
	account_name TEXT,
	account_type TEXT,
	sourceid TEXT,
	version INTEGER NOT NULL DEFAULT 1,
	created INTEGER,
	modified INTEGER,
	dirty INTEGER NOT NULL DEFAULT 0,
	sync1 TEXT,
	sync2 TEXT,
	sync3 TEXT,
	sync4 TEXT,
	sync5 TEXT
);

CREATE TABLE history(
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT,
	url TEXT NOT NULL,
	created INTEGER,
	date INTEGER,
	visits INTEGER NOT NULL DEFAULT 0,
	user_entered INTEGER
);

CREATE TABLE images (
	url_key TEXT UNIQUE NOT NULL,
	favicon BLOB,
	thumbnail BLOB,
	touch_icon BLOB
);

CREATE TABLE searches (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	search TEXT,date LONG
);

CREATE TABLE settings (
	key TEXT PRIMARY KEY,
	value TEXT NOT NULL
);

CREATE TABLE thumbnails (
	_id INTEGER PRIMARY KEY,
	thumbnail BLOB NOT NULL
);

CREATE VIEW v_accounts AS 
SELECT NULL AS account_name, NULL AS account_type, 1 AS root_id 
UNION ALL 
SELECT account_name, account_type, _id AS root_id FROM bookmarks WHERE sync3 = "bookmark_bar" AND deleted = 0;

CREATE VIEW v_omnibox_suggestions  AS   
SELECT _id, url, title, 1 AS bookmark, 0 AS visits, 0 AS date  FROM bookmarks   WHERE deleted = 0 AND folder = 0   
UNION ALL   
SELECT _id, url, title, 0 AS bookmark, visits, date   FROM history   WHERE url NOT IN (SELECT url FROM bookmarks    WHERE deleted = 0 AND folder = 0)   ORDER BY bookmark DESC, visits DESC, date DESC;

CREATE INDEX imagesUrlIndex ON images(url_key);

