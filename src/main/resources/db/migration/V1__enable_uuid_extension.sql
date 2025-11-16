-- V0__enable_uuid_extension.sql
-- Enable uuid-ossp extension so uuid_generate_v4() is available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
