-- 20140802061854-add-disable-and-delete-columns.down.sql

ALTER TABLE IF EXISTS user_account
DROP COLUMN IF EXISTS disabled;
