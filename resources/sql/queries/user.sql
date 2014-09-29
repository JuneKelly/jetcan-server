-- name: -create-user!
-- Make a new user
INSERT INTO user_account (id, password, name, admin, created)
VALUES (
  :email,
  :password,
  :name,
  :admin,
  :created
);


-- name: -get-user-profile
-- Gets profile fields for a single user by email id
SELECT id, name, created, admin, disabled FROM user_account
WHERE id = :email;


-- name: -get-user-credentials
-- gets credentials for a single user
SELECT id AS email, password FROM user_account
WHERE id = :email


-- name: -update-user!
-- save changes to user record
UPDATE user_account
SET name = :name
WHERE id = :email


-- name: -user-exists?
SELECT exists(
  SELECT 1 from user_account
  WHERE id = :email
);


-- name: -get-user-list
SELECT id, name, created, admin, disabled
FROM user_account


--name: -update-user-disabled-status!
UPDATE user_account
SET disabled = :disabled
WHERE id = :id
