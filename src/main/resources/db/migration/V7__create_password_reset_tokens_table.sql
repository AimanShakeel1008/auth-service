-- V7__create_password_reset_tokens_table.sql
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    token_hash varchar(128) NOT NULL,
    expires_at timestamptz NOT NULL,
    used boolean NOT NULL DEFAULT false,
    user_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user ON password_reset_tokens (user_id);

ALTER TABLE password_reset_tokens
  ADD CONSTRAINT fk_prt_user FOREIGN KEY (user_id)
  REFERENCES users (id) ON DELETE CASCADE;
