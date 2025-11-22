-- V8__create_outbox_table.sql
CREATE TABLE IF NOT EXISTS outbox (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type text NOT NULL,
    aggregate_id uuid NOT NULL,
    type text NOT NULL,
    payload jsonb NOT NULL,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    processed boolean NOT NULL DEFAULT false,
    processed_at timestamptz,
    retry_count integer NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_processed_occurred ON outbox (processed, occurred_at);
