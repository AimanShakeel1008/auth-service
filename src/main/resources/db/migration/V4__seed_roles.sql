-- ============================================================
-- V3__seed_roles.sql
-- Seed initial roles (idempotent)
-- ============================================================

-- Using fixed UUIDs to allow stable references across environments.
-- Change UUIDs only if you understand the implications for downstream references.

INSERT INTO public.roles (id, name, description)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'ROLE_USER', 'Default role for normal users')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.roles (id, name, description)
VALUES
  ('00000000-0000-0000-0000-000000000002', 'ROLE_ADMIN', 'Administrative role with elevated privileges')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.roles (id, name, description)
VALUES
  ('00000000-0000-0000-0000-000000000003', 'ROLE_SERVICE', 'Service-to-service role for backend components')
ON CONFLICT (id) DO NOTHING;

-- Ensure name uniqueness if someone seeded by name instead of id
INSERT INTO public.roles (id, name, description)
SELECT uuid_generate_v4(), r.name, r.description
FROM (VALUES
        ('ROLE_USER', 'Default role for normal users'),
        ('ROLE_ADMIN', 'Administrative role with elevated privileges'),
        ('ROLE_SERVICE', 'Service-to-service role for backend components')
     ) AS r(name, description)
WHERE NOT EXISTS (
  SELECT 1 FROM public.roles pr WHERE lower(pr.name) = lower(r.name)
);
