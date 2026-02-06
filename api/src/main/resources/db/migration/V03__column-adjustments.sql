ALTER TABLE customers
    ALTER COLUMN email TYPE VARCHAR(255) USING (email::VARCHAR(255));

ALTER TABLE customers
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE customer_sessions
    ALTER COLUMN service_type TYPE VARCHAR(255) USING (service_type::VARCHAR(255));

ALTER TABLE customer_sessions
    ALTER COLUMN status TYPE VARCHAR(255) USING (status::VARCHAR(255));

ALTER TABLE customers
    ALTER COLUMN version DROP NOT NULL;