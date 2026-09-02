CREATE TABLE IF NOT EXISTS sync_log (
    type       VARCHAR(10) NOT NULL PRIMARY KEY,
    last_download_time  VARCHAR(20)
);

INSERT OR IGNORE INTO sync_log (type, last_download_time) VALUES ('up', '2024-01-01 00:00:00');
INSERT OR IGNORE INTO sync_log (type, last_download_time) VALUES ('down', '2024-01-01 00:00:00');