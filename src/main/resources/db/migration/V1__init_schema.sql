-- V1__init_schema.sql

-- 1) Extensions (UUID generator)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2) ENUM types
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'email_status') THEN
    CREATE TYPE email_status AS ENUM ('UNVERIFIED', 'VERIFIED', 'PENDING_VERIFICATION');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
    CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
    CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'leave_request_status') THEN
    CREATE TYPE leave_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED');
  END IF;
END$$;

-- 3) Tables

-- USER
CREATE TABLE IF NOT EXISTS users (
  user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(255) NULL,
  email           VARCHAR(255) NOT NULL,
  email_status    email_status NOT NULL DEFAULT 'UNVERIFIED',
  hashed_password VARCHAR(255) NOT NULL,
  role            user_role NOT NULL DEFAULT 'USER',

  create_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  update_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  delete_at       TIMESTAMPTZ NULL,

  CONSTRAINT uq_users_email UNIQUE (email)
);

-- TASK
CREATE TABLE IF NOT EXISTS tasks (
  task_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id      UUID NOT NULL,
  title         VARCHAR(255) NOT NULL,
  description   TEXT NULL,
  task_status   task_status NOT NULL DEFAULT 'TODO',
  task_priority task_priority NOT NULL DEFAULT 'MEDIUM',
  deadline      TIMESTAMPTZ NULL,

  create_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  update_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  delete_at     TIMESTAMPTZ NULL,

  CONSTRAINT fk_tasks_owner FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

-- ASSIGNEE_TASK (many-to-many)
CREATE TABLE IF NOT EXISTS assignee_task (
  task_id     UUID NOT NULL,
  assignee_id UUID NOT NULL,

  CONSTRAINT pk_assignee_task PRIMARY KEY (task_id, assignee_id),
  CONSTRAINT fk_assignee_task_task FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
  CONSTRAINT fk_assignee_task_user FOREIGN KEY (assignee_id) REFERENCES users(user_id)
);

-- COMMENT
CREATE TABLE IF NOT EXISTS comments (
  comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID NOT NULL,
  task_id    UUID NOT NULL,
  content    TEXT NOT NULL,

  create_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  update_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  delete_at  TIMESTAMPTZ NULL,

  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
);

-- LEAVE_REQUEST
CREATE TABLE IF NOT EXISTS leave_request (
  leave_request_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  task_id              UUID NOT NULL,
  user_id              UUID NOT NULL,
  reason               TEXT NULL,
  leave_request_status leave_request_status NOT NULL DEFAULT 'PENDING',

  create_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  update_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  delete_at            TIMESTAMPTZ NULL,
  expire_at            TIMESTAMPTZ NOT NULL,

  CONSTRAINT fk_leave_task FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
  CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- REFRESH TOKEN
CREATE TABLE IF NOT EXISTS refresh_token (
  refresh_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID NOT NULL,
  hash_token VARCHAR(255) NOT NULL,
  is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
  create_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  expire_at  TIMESTAMPTZ NOT NULL,

  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4) Indexes (list/filter/pagination/search cơ bản)

-- tasks listing
CREATE INDEX IF NOT EXISTS idx_tasks_owner_id ON tasks(owner_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(task_status);
CREATE INDEX IF NOT EXISTS idx_tasks_deadline ON tasks(deadline);

-- comments listing
CREATE INDEX IF NOT EXISTS idx_comments_task_id ON comments(task_id);

-- leave_request lookup
CREATE INDEX IF NOT EXISTS idx_leave_task_id ON leave_request(task_id);
CREATE INDEX IF NOT EXISTS idx_leave_user_id ON leave_request(user_id);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_request(leave_request_status);

-- refresh token lookup
CREATE INDEX IF NOT EXISTS idx_refresh_user_id ON refresh_token(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_hash_token ON refresh_token(hash_token);

-- Optional: chỉ cho phép 1 request PENDING cho mỗi (task_id, user_id) và chưa soft-delete
CREATE UNIQUE INDEX IF NOT EXISTS uq_leave_pending_per_task_user
ON leave_request(task_id, user_id)
WHERE leave_request_status = 'PENDING' AND delete_at IS NULL;