-- 1. Убираем ключ идемпотентности из notifications
ALTER TABLE notifications DROP COLUMN idempotency_key;

-- 2. Идемпотентность отдельно
CREATE TABLE idempotency_keys (
    key               TEXT         PRIMARY KEY,             -- значение из заголовка Idempotency-Key
    notification_id   UUID         NOT NULL REFERENCES notifications(id),
    request_body_hash TEXT         NOT NULL,                -- sha256 тела
    response_status   INT          NOT NULL,
    response_body     JSONB        NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at        TIMESTAMPTZ  NOT NULL DEFAULT (now() + interval '7 days')
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys (expires_at);