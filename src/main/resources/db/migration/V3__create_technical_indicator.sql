CREATE TABLE technical_indicator (
    id              BIGSERIAL PRIMARY KEY,
    stock_id        BIGINT       NOT NULL,
    trading_date    DATE         NOT NULL,
    rsi_14          NUMERIC(10,4),
    ema_20          NUMERIC(15,4),
    ema_50          NUMERIC(15,4),
    avg_volume_20   BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_indicator_stock FOREIGN KEY (stock_id) REFERENCES stock (id),
    CONSTRAINT uq_indicator_stock_date UNIQUE (stock_id, trading_date)
);

CREATE INDEX idx_indicator_stock_id ON technical_indicator (stock_id);
CREATE INDEX idx_indicator_trading_date ON technical_indicator (trading_date);
CREATE INDEX idx_indicator_stock_date ON technical_indicator (stock_id, trading_date DESC);
