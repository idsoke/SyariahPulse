CREATE TABLE daily_price (
    id           BIGSERIAL PRIMARY KEY,
    stock_id     BIGINT       NOT NULL,
    trading_date DATE         NOT NULL,
    open         NUMERIC(15,2) NOT NULL,
    high         NUMERIC(15,2) NOT NULL,
    low          NUMERIC(15,2) NOT NULL,
    close        NUMERIC(15,2) NOT NULL,
    volume       BIGINT        NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_daily_price_stock FOREIGN KEY (stock_id) REFERENCES stock (id),
    CONSTRAINT uq_daily_price_stock_date UNIQUE (stock_id, trading_date)
);

CREATE INDEX idx_daily_price_stock_id ON daily_price (stock_id);
CREATE INDEX idx_daily_price_trading_date ON daily_price (trading_date);
CREATE INDEX idx_daily_price_stock_date ON daily_price (stock_id, trading_date DESC);
