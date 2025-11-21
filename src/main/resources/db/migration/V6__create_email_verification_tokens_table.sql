-- V6__create_email_verification_tokens_table.sql
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    token_hash varchar(128) NOT NULL,
    expires_at timestamptz NOT NULL,
    used boolean NOT NULL DEFAULT false,
    user_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_email_verification_token_hash ON email_verification_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_email_verification_token_user ON email_verification_tokens (user_id);

ALTER TABLE email_verification_tokens
  ADD CONSTRAINT fk_evt_user FOREIGN KEY (user_id)
  REFERENCES users (id) ON DELETE CASCADE;
