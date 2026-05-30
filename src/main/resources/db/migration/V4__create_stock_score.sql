CREATE TABLE stock_score (
    id              BIGSERIAL PRIMARY KEY,
    stock_id        BIGINT    NOT NULL,
    scoring_date    DATE      NOT NULL,
    score           INT       NOT NULL DEFAULT 0,
    price_score     INT       NOT NULL DEFAULT 0,
    volume_score    INT       NOT NULL DEFAULT 0,
    rsi_score       INT       NOT NULL DEFAULT 0,
    ema20_score     INT       NOT NULL DEFAULT 0,
    trend_score     INT       NOT NULL DEFAULT 0,
    rank_position   INT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_score_stock FOREIGN KEY (stock_id) REFERENCES stock (id),
    CONSTRAINT uq_score_stock_date UNIQUE (stock_id, scoring_date)
);

CREATE INDEX idx_score_stock_id ON stock_score (stock_id);
CREATE INDEX idx_score_scoring_date ON stock_score (scoring_date);
CREATE INDEX idx_score_rank ON stock_score (scoring_date, score DESC, rank_position);
