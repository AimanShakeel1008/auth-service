-- 01_create_user_and_db.sql
-- Idempotent creation of user and database for auth-service
-- This runs during container first-time init (docker-entrypoint-initdb.d)

-- create role if not exists
DO
$$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authsvc_user') THEN
      CREATE ROLE authsvc_user WITH LOGIN PASSWORD 'ChangeMeStrongP@ssw0rd';
   END IF;
END
$$;

-- create database if not exists
DO
$$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'authsvc_db') THEN
      CREATE DATABASE authsvc_db OWNER authsvc_user;
   END IF;
END
$$;
