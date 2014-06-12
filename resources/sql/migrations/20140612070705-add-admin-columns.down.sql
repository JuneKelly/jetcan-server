-- 20140612070705-add-admin-columns.down.sql

ALTER TABLE IF EXISTS user_account
DROP COLUMN IF EXISTS admin;
