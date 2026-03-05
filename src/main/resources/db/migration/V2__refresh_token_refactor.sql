-- V2__refresh_token_refactor.sql
-- Refactor refresh_token:
-- - drop refresh_id (UUID PK)
-- - use token_hash as PRIMARY KEY (deterministic hash for lookup)
-- - replace is_revoked boolean with revoked_at timestamp
-- - rename create_at/expire_at/hash_token to created_at/expires_at/token_hash
-- - add helpful indexes

BEGIN;

-- 1) Add revoked_at first (so we can migrate is_revoked -> revoked_at)
ALTER TABLE refresh_token
  ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMPTZ NULL;

-- 2) Migrate existing revoked flags into revoked_at (best-effort)
-- If token was revoked in V1, we mark revoked_at as "now()".
-- (If you had historical revoked time, you'd store that instead.)
UPDATE refresh_token
SET revoked_at = now()
WHERE is_revoked = TRUE AND revoked_at IS NULL;

-- 3) Rename columns to clearer names
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'refresh_token' AND column_name = 'hash_token'
  ) THEN
    ALTER TABLE refresh_token RENAME COLUMN hash_token TO token_hash;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'refresh_token' AND column_name = 'create_at'
  ) THEN
    ALTER TABLE refresh_token RENAME COLUMN create_at TO created_at;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'refresh_token' AND column_name = 'expire_at'
  ) THEN
    ALTER TABLE refresh_token RENAME COLUMN expire_at TO expires_at;
  END IF;
END $$;

-- 4) Drop old indexes/constraints that no longer match the new model
-- (safe even if they don't exist)
DROP INDEX IF EXISTS uq_refresh_hash_token;
DROP INDEX IF EXISTS idx_refresh_user_id;

-- 5) Drop boolean flag (we now use revoked_at)
ALTER TABLE refresh_token
  DROP COLUMN IF EXISTS is_revoked;

-- 6) Switch primary key from refresh_id -> token_hash
-- Drop old PK constraint (name is usually refresh_token_pkey, but use IF EXISTS)
ALTER TABLE refresh_token
  DROP CONSTRAINT IF EXISTS refresh_token_pkey;

-- Remove refresh_id column (no longer needed)
ALTER TABLE refresh_token
  DROP COLUMN IF EXISTS refresh_id;

-- Ensure token_hash is NOT NULL (should already be NOT NULL in V1)
ALTER TABLE refresh_token
  ALTER COLUMN token_hash SET NOT NULL;

-- Set token_hash as PRIMARY KEY (fast lookup by hash)
ALTER TABLE refresh_token
  ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (token_hash);

-- 7) Recreate indexes for common operations
-- List tokens by user (optional feature, admin/debug)
CREATE INDEX IF NOT EXISTS idx_refresh_user_id ON refresh_token(user_id);

-- Cleanup / expiry scans
CREATE INDEX IF NOT EXISTS idx_refresh_expires_at ON refresh_token(expires_at);

-- Optional: fast query active tokens only (useful if you often check active/revoked)
CREATE INDEX IF NOT EXISTS idx_refresh_revoked_at ON refresh_token(revoked_at);


COMMIT;