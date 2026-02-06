CREATE TABLE customers(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_sessions(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE customer_sessions
    ADD CONSTRAINT fk_customer_sessions_on_customers FOREIGN KEY (customer_id) REFERENCES customers (id);

CREATE INDEX customer_services_status_idx ON customer_sessions(status);
CREATE INDEX customer_services_started_at_idx ON customer_sessions(started_at);
CREATE INDEX customer_services_finished_at_idx ON customer_sessions(finished_at);
