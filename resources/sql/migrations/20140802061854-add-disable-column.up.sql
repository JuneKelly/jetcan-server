-- 20140802061854-add-disable-and-delete-columns.up.sql

ALTER TABLE IF EXISTS user_account
ADD COLUMN disabled boolean DEFAULT FALSE;
