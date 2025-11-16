-- ============================================================
-- V2__create_users_table.sql
-- Initial schema for auth-service users table
-- ============================================================

CREATE TABLE IF NOT EXISTS public.users (
    id UUID NOT NULL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Unique constraints for safe lookup
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
    ON public.users (LOWER(email));

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username
    ON public.users (LOWER(username));

-- Optional: indexing for email/username queries
CREATE INDEX IF NOT EXISTS idx_users_email
    ON public.users (email);

CREATE INDEX IF NOT EXISTS idx_users_username
    ON public.users (username);
