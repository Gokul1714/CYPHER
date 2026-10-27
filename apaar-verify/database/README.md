# CYPHER Database Setup (PostgreSQL)

This folder contains the complete PostgreSQL database schema and seed data for the **CYPHER AICTE Regulatory Officer Platform**.

---

## Quick Setup Options

### Option 1: Docker (Automatic)
If running via Docker Compose (`docker-compose up -d`), PostgreSQL automatically executes `database/init.sql` upon initial container launch.

* **Database Name**: `cypher_db`
* **Username**: `postgres`
* **Password**: `postgres`
* **Port**: `5432`

---

### Option 2: Local PostgreSQL (psql CLI)
If PostgreSQL is already installed on your local machine:

```bash
# 1. Connect to PostgreSQL
psql -U postgres

# 2. Create the database
CREATE DATABASE cypher_db;
\q

# 3. Import schema and seed records
psql -U postgres -d cypher_db -f database/init.sql
```

---

### Option 3: GUI Client (pgAdmin, DBeaver, TablePlus)
1. Connect to your local PostgreSQL server (`localhost:5432`).
2. Create a new database named `cypher_db`.
3. Open a SQL Query editor, paste the contents of `database/init.sql`, and run the script.

---

## Entity Relationships & Schema Summary
1. **`institutions`**: AICTE-approved colleges with AICTE codes, contact details, and accreditation.
2. **`users`**: Platform administrators and institutional officers with BCrypt passwords and verification approval states.
3. **`approved_intake`**: Sanctioned seat limits per college, course, and academic year (General, NRI, Supernumerary).
4. **`students`**: Centralized student census records with APAAR ID references and verification statuses.
5. **`enrolments`**: Active student admissions mapped to colleges and departments.
6. **`validation_results`**: Audit anomaly flags (Format failures, Missing APAAR, Cross-College Dual Enrolments).
7. **`verification_tokens`**: Tamper-proof, unpredictable cryptographically-signed tokens for public QR verification without PII exposure.
8. **`audit_logs`**: Immutable statutory compliance trail recording timestamps, actions, IP addresses, and user roles.
