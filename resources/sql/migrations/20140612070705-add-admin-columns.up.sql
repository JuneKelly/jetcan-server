-- 20140612070705-add-admin-columns.up.sql

ALTER TABLE IF EXISTS user_account
ADD COLUMN admin boolean DEFAULT FALSE;
