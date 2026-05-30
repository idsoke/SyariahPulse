CREATE TABLE stock (
    id          BIGSERIAL PRIMARY KEY,
    symbol      VARCHAR(20)  NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    sector      VARCHAR(100),
    is_syariah  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stock_symbol UNIQUE (symbol)
);

CREATE INDEX idx_stock_symbol ON stock (symbol);
CREATE INDEX idx_stock_is_syariah ON stock (is_syariah);
