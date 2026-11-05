-- 1. Уведомление
CREATE TABLE notifications (
                               id           UUID         PRIMARY KEY DEFAULT uuidv7(),
                               idempotency_key TEXT UNIQUE,
                               user_id      TEXT         NOT NULL,
                               channel      TEXT         NOT NULL CHECK (channel IN ('EMAIL','TELEGRAM','SMS','PUSH')),
                               priority     TEXT         NOT NULL CHECK (priority IN ('P0','P1')),
                               body         TEXT         NOT NULL,
                               status       TEXT         NOT NULL CHECK (status IN ('ACCEPTED','QUEUED','SENDING','SENT','FAILED','DEAD'))
                                                                     DEFAULT 'ACCEPTED',
                               sent_at      TIMESTAMPTZ,
                               created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
                               updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 2. Outbox
CREATE TABLE outbox (
                        id              UUID         PRIMARY KEY DEFAULT uuidv7(),
                        notification_id UUID         NOT NULL REFERENCES notifications(id),
                        payload         JSONB        NOT NULL,                  -- snapshot notification
                        status          TEXT         NOT NULL CHECK (status IN ('PENDING','SENT')) DEFAULT 'PENDING',
                        attempts        INT          NOT NULL DEFAULT 0,
                        created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                        sent_at         TIMESTAMPTZ
);

CREATE INDEX idx_outbox_pending
    ON outbox (created_at)
    WHERE status = 'PENDING';

-- Автообновление updated_at на notifications
CREATE OR REPLACE FUNCTION touch_updated_at()
RETURNS TRIGGER AS $$ BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
 $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION touch_updated_at();