-- V1__init_schema.sql

BEGIN;

------------------------------------------------
-- 1) Extension
------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;

------------------------------------------------
-- 2) USERS
------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NULL,
    email            VARCHAR(255) NOT NULL,
    email_status     VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED',
    hashed_password  VARCHAR(255) NOT NULL,
    role             VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ NULL,

    CONSTRAINT chk_users_email_status
        CHECK (email_status IN ('UNVERIFIED', 'VERIFIED', 'PENDING_VERIFICATION')),
    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'USER'))
);

-- unique email only for active users
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;

------------------------------------------------
-- 3) TASKS
------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
    task_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id         UUID NOT NULL,
    title            VARCHAR(255) NOT NULL,
    description      TEXT NULL,
    task_status      VARCHAR(50) NOT NULL DEFAULT 'TODO',
    task_priority    VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    deadline         TIMESTAMP NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ NULL,

    CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id),

    CONSTRAINT chk_tasks_status
        CHECK (task_status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_tasks_priority
        CHECK (task_priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

------------------------------------------------
-- 4) ASSIGNEE_TASK (many-to-many)
------------------------------------------------
CREATE TABLE IF NOT EXISTS assignee_task (
    task_id          UUID NOT NULL,
    assignee_id      UUID NOT NULL,

    CONSTRAINT pk_assignee_task
        PRIMARY KEY (task_id, assignee_id),

    CONSTRAINT fk_assignee_task_task
        FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,

    CONSTRAINT fk_assignee_task_user
        FOREIGN KEY (assignee_id) REFERENCES users(user_id)
);

------------------------------------------------
-- 5) COMMENTS
------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    comment_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL,
    task_id          UUID NOT NULL,
    content          TEXT NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ NULL,

    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_comments_task
        FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
);

------------------------------------------------
-- 6) LEAVE_REQUESTS
------------------------------------------------
CREATE TABLE IF NOT EXISTS leave_requests (
    leave_request_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id               UUID NOT NULL,
    user_id               UUID NOT NULL,
    reason                TEXT NULL,
    leave_request_status  VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ NULL,
    expires_at            TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_leave_requests_task
        FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,

    CONSTRAINT fk_leave_requests_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT chk_leave_requests_status
        CHECK (leave_request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'))
);

-- only one active pending leave request per (task_id, user_id)
CREATE UNIQUE INDEX IF NOT EXISTS uq_leave_requests_pending_per_task_user
    ON leave_requests (task_id, user_id)
    WHERE leave_request_status = 'PENDING' AND deleted_at IS NULL;

------------------------------------------------
-- 7) REFRESH TOKENS
------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_hash       VARCHAR(255) PRIMARY KEY,
    user_id          UUID NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at       TIMESTAMPTZ NOT NULL,
    revoked_at       TIMESTAMPTZ NULL,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

------------------------------------------------
-- 8) Indexes for common queries
------------------------------------------------

-- tasks
CREATE INDEX IF NOT EXISTS idx_tasks_owner_id
    ON tasks (owner_id);

CREATE INDEX IF NOT EXISTS idx_tasks_status
    ON tasks (task_status);

CREATE INDEX IF NOT EXISTS idx_tasks_priority
    ON tasks (task_priority);

CREATE INDEX IF NOT EXISTS idx_tasks_deadline
    ON tasks (deadline);

CREATE INDEX IF NOT EXISTS idx_tasks_owner_status
    ON tasks (owner_id, task_status);

-- comments
CREATE INDEX IF NOT EXISTS idx_comments_task_id
    ON comments (task_id);

CREATE INDEX IF NOT EXISTS idx_comments_user_id
    ON comments (user_id);

-- leave requests
CREATE INDEX IF NOT EXISTS idx_leave_requests_task_id
    ON leave_requests (task_id);

CREATE INDEX IF NOT EXISTS idx_leave_requests_user_id
    ON leave_requests (user_id);

CREATE INDEX IF NOT EXISTS idx_leave_requests_status
    ON leave_requests (leave_request_status);

CREATE INDEX IF NOT EXISTS idx_leave_requests_expires_at
    ON leave_requests (expires_at);

-- refresh tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked_at
    ON refresh_tokens (revoked_at);

COMMIT;