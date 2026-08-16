CREATE TABLE notify_me_interests (
    id              UUID PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    status          VARCHAR(30) NOT NULL,
    invite_token    VARCHAR(64) UNIQUE,
    invited_at      TIMESTAMPTZ
);

CREATE INDEX idx_notify_me_interests_status ON notify_me_interests(status);
