-- V3__normalize_timestamp_columns.sql

BEGIN;

------------------------------------------------
-- USERS
------------------------------------------------

ALTER TABLE users
  RENAME COLUMN create_at TO created_at;

ALTER TABLE users
  RENAME COLUMN update_at TO updated_at;

ALTER TABLE users
  RENAME COLUMN delete_at TO deleted_at;

------------------------------------------------
-- TASKS
------------------------------------------------

ALTER TABLE tasks
  RENAME COLUMN create_at TO created_at;

ALTER TABLE tasks
  RENAME COLUMN update_at TO updated_at;

ALTER TABLE tasks
  RENAME COLUMN delete_at TO deleted_at;

-- ensure deadline stays TIMESTAMP (LocalDateTime)
ALTER TABLE tasks
  ALTER COLUMN deadline TYPE TIMESTAMP;

------------------------------------------------
-- COMMENTS
------------------------------------------------

ALTER TABLE comments
  RENAME COLUMN create_at TO created_at;

ALTER TABLE comments
  RENAME COLUMN update_at TO updated_at;

ALTER TABLE comments
  RENAME COLUMN delete_at TO deleted_at;

------------------------------------------------
-- LEAVE_REQUEST
------------------------------------------------

ALTER TABLE leave_request
  RENAME COLUMN create_at TO created_at;

ALTER TABLE leave_request
  RENAME COLUMN update_at TO updated_at;

ALTER TABLE leave_request
  RENAME COLUMN delete_at TO deleted_at;

ALTER TABLE leave_request
  RENAME COLUMN expire_at TO expires_at;

------------------------------------------------
-- ensure timestamp types match Instant usage
------------------------------------------------

ALTER TABLE users
  ALTER COLUMN created_at TYPE TIMESTAMPTZ,
  ALTER COLUMN updated_at TYPE TIMESTAMPTZ,
  ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE tasks
  ALTER COLUMN created_at TYPE TIMESTAMPTZ,
  ALTER COLUMN updated_at TYPE TIMESTAMPTZ,
  ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE comments
  ALTER COLUMN created_at TYPE TIMESTAMPTZ,
  ALTER COLUMN updated_at TYPE TIMESTAMPTZ,
  ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE leave_request
  ALTER COLUMN created_at TYPE TIMESTAMPTZ,
  ALTER COLUMN updated_at TYPE TIMESTAMPTZ,
  ALTER COLUMN deleted_at TYPE TIMESTAMPTZ,
  ALTER COLUMN expires_at TYPE TIMESTAMPTZ;

ALTER TABLE refresh_token
  ALTER COLUMN created_at TYPE TIMESTAMPTZ,
  ALTER COLUMN expires_at TYPE TIMESTAMPTZ,
  ALTER COLUMN revoked_at TYPE TIMESTAMPTZ;

COMMIT;