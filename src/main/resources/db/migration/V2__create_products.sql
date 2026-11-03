CREATE TABLE products (
    id          UUID          PRIMARY KEY,
    sku         VARCHAR(50)   NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    description TEXT,
    price       NUMERIC(12,2) NOT NULL,
    category_id UUID          NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by  UUID          NOT NULL,
    version     INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_products_price CHECK (price > 0)
);

CREATE INDEX idx_products_category_active ON products (category_id, active);