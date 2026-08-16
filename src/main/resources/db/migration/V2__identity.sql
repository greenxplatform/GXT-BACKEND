CREATE TABLE users (
    id              UUID PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    email           VARCHAR(255) UNIQUE,
    password_hash   VARCHAR(255),
    google_id       VARCHAR(255) UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    role            VARCHAR(30) NOT NULL,
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE verification_challenges (
    id                  UUID PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verification_type   VARCHAR(30) NOT NULL,
    target              VARCHAR(255) NOT NULL,
    otp_hash            VARCHAR(255) NOT NULL,
    status              VARCHAR(30) NOT NULL,
    attempt_count       INT NOT NULL DEFAULT 0,
    expires_at          TIMESTAMPTZ NOT NULL,
    verified_at         TIMESTAMPTZ
);

CREATE INDEX idx_verification_challenges_user ON verification_challenges(user_id);

CREATE TABLE logged_out_tokens (
    id          UUID PRIMARY KEY,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
