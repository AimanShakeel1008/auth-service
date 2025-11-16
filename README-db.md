# Local PostgreSQL Setup for auth-service

This document explains how to run and verify the **local PostgreSQL database** used by the `auth-service`.

---

## 🚀 Prerequisites

- **Docker** installed
- **Docker Compose** installed
- (**Optional**) `psql` client for manual database inspection

---

## 📝 1. Create local environment file

Copy the example env file:

```bash
cp .env.example .env.local
```

(Optional) Edit `.env.local` and change the password to something you prefer for local dev.

---

## 🐘 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

This starts a PostgreSQL 15 container with:

- Database: `authsvc_db`
- User: `authsvc_user`
- Password: from `.env.local`
- Init script: `sql/init/01_create_user_and_db.sql`
- Healthcheck enabled

---

## 🔍 3. Check PostgreSQL status

### View container status
```bash
docker compose ps
```

### View logs (last 100 lines)
```bash
docker logs authsvc-postgres --tail 100
```

### Check readiness
```bash
docker exec -it authsvc-postgres   pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}
```

Expected output:
```
/var/run/postgresql:5432 - accepting connections
```

---

## 🛠️ 4. Connect using psql (optional)

If you have the `psql` client installed:

```bash
export PGPASSWORD=$(grep POSTGRES_PASSWORD .env.local | cut -d'=' -f2)

psql -h localhost      -p $(grep POSTGRES_PORT .env.local | cut -d'=' -f2)      -U $(grep POSTGRES_USER .env.local | cut -d'=' -f2)      -d $(grep POSTGRES_DB .env.local | cut -d'=' -f2)      -c '\l'
```

You should see `authsvc_db` listed.

---

## 🧹 5. Stop and clean (optional)

Stop the DB:
```bash
docker compose down
```

Stop and delete DB volume (removes all local data):
```bash
docker compose down -v
```

---

## 📌 Notes

- **Do not** commit `.env.local` to git. Use `.env.local.example` as the template.
- The init script under `sql/init/` runs only on first container initialization.
- For production, use a managed DB + secret management (Vault, AWS Secrets Manager, etc.).  
