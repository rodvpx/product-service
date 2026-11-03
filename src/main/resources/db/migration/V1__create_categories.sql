CREATE TABLE categories (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(120) NOT NULL,
    parent_id  UUID,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);