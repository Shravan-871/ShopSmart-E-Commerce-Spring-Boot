-- V4: order lifecycle tracking columns

ALTER TABLE orders ADD COLUMN confirmed_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN shipped_at   TIMESTAMP;
ALTER TABLE orders ADD COLUMN stock_restored BOOLEAN NOT NULL DEFAULT FALSE;
