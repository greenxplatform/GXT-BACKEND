CREATE TABLE strategies (
    id              VARCHAR(64) PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    instrument      VARCHAR(64),
    timeframe       VARCHAR(32),
    direction       VARCHAR(16),
    summary         TEXT,
    entry_text      TEXT,
    exit_text       TEXT,
    stop_loss_text  TEXT,
    preferred_mode  VARCHAR(32),
    spec_json       TEXT,
    backtest_id     VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_strategies_user_id ON strategies(user_id);

CREATE TABLE chat_messages (
    id              VARCHAR(64) PRIMARY KEY,
    strategy_id     VARCHAR(64) NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    role            VARCHAR(16) NOT NULL,
    content         TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_strategy_id ON chat_messages(strategy_id);

CREATE TABLE strategy_spec_snapshots (
    id              VARCHAR(64) PRIMARY KEY,
    strategy_id     VARCHAR(64) NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    spec_json       TEXT NOT NULL,
    revision        INT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_spec_snapshots_strategy_id ON strategy_spec_snapshots(strategy_id);

CREATE TABLE backtest_jobs (
    id              VARCHAR(64) PRIMARY KEY,
    strategy_id     VARCHAR(64) NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    snapshot_id     VARCHAR(64) NOT NULL REFERENCES strategy_spec_snapshots(id),
    status          VARCHAR(32) NOT NULL,
    error_message   TEXT,
    report_id       VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_backtest_jobs_strategy_id ON backtest_jobs(strategy_id);

CREATE TABLE backtest_reports (
    id              VARCHAR(64) PRIMARY KEY,
    strategy_id     VARCHAR(64) NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    instrument      VARCHAR(64) NOT NULL,
    period_from     DATE NOT NULL,
    period_to       DATE NOT NULL,
    metrics_json    TEXT NOT NULL,
    equity_json     TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE backtest_trades (
    id              VARCHAR(64) PRIMARY KEY,
    report_id       VARCHAR(64) NOT NULL REFERENCES backtest_reports(id) ON DELETE CASCADE,
    entry_date      DATE NOT NULL,
    exit_date       DATE NOT NULL,
    side            VARCHAR(16) NOT NULL,
    return_pct      DOUBLE PRECISION NOT NULL,
    result          VARCHAR(16) NOT NULL
);

CREATE INDEX idx_backtest_trades_report_id ON backtest_trades(report_id);
