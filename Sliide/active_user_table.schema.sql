CREATE TABLE IF NOT EXISTS active_user_table (
    date              DATE    PRIMARY KEY,
    active_user_count INTEGER NOT NULL
) WITHOUT ROWID;