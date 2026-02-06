-- Add service_type column to customer_sessions
ALTER TABLE customer_sessions
    ADD COLUMN service_type VARCHAR(20) NOT NULL DEFAULT 'OTHER';

-- Create index for service_type queries
CREATE INDEX customer_sessions_service_type_status_idx 
    ON customer_sessions(service_type, status, started_at);