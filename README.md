<div align="center">

# ResuMatch — Backend

### AI-style resume → job description matcher with explainable scoring.

Spring Boot REST API · JWT authentication · PostgreSQL · Apache PDFBox · Jaccard similarity

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)

**[Live Demo](https://resumatch-ui-3yv7.vercel.app)**  ·  **[Frontend Repo](https://github.com/Aman100705/resumatch-ui)**

</div>

---

## What it does

ResuMatch lets a job-seeker upload a resume PDF, paste a job description, and get back a **transparent match score (0–100)** with the exact keywords they're missing.

It's the engine behind ATS-style filtering — but inverted to help the candidate, not screen them out.

The score is the weighted sum of three signals:

```
finalScore = 0.5 × keywordCoverage   (% of JD keywords found in resume)
           + 0.3 × skillCoverage     (% of JD tech skills found)
           + 0.2 × textSimilarity    (Jaccard token overlap)
```

Every score comes with the raw matched keywords, missing keywords, and a tailored recommendation — no black-box scoring.

---

## Architecture

```
┌────────────────┐    HTTPS+JWT    ┌────────────────────┐
│  Next.js UI    │ ──────────────► │  Spring Boot API   │
│  (Vercel)      │ ◄────────────── │  (REST + Security) │
└────────────────┘                 └─────────┬──────────┘
                                             │
                          ┌──────────────────┼─────────────────┐
                          ▼                  ▼                 ▼
                   ┌────────────┐   ┌────────────────┐  ┌────────────┐
                   │ PostgreSQL │   │ Apache PDFBox  │  │  Local FS  │
                   │  16 (JPA)  │   │ (text extract) │  │  (uploads) │
                   └────────────┘   └────────────────┘  └────────────┘
```

**5 entities** — User, Resume, JobDescription, MatchAnalysis, Role  
**4 controllers** — Auth, Resume, JobDescription, Match  
**16 endpoints** — full CRUD + analyze, all owner-scoped (multi-tenant safe)

---

## Tech stack

| Layer            | Technology                                |
| ---------------- | ----------------------------------------- |
| Language         | Java 21                                   |
| Framework        | Spring Boot 3.4 (Web · Data JPA · Security) |
| Auth             | JWT via `jjwt` 0.12 + BCrypt              |
| Database         | PostgreSQL 16 with Hibernate              |
| PDF Parsing      | Apache PDFBox 3.0                         |
| Text Similarity  | Apache Commons Text (Jaccard)             |
| API Docs         | springdoc-openapi (Swagger UI)            |
| Build            | Maven 3.9 + Lombok                        |

---

## Getting started

### Prerequisites
- JDK 21
- PostgreSQL 16 running locally
- Maven 3.9+

### Setup

```bash
# 1. Clone
git clone https://github.com/Aman100705/resumatch.git
cd resumatch

# 2. Create the database
psql postgres -c "CREATE USER resumatch_user WITH PASSWORD 'resumatch_pass';"
psql postgres -c "CREATE DATABASE resumatch_db OWNER resumatch_user;"

# 3. Run
mvn spring-boot:run
```

API runs on **http://localhost:8080**.  
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## API surface

### Auth
| Method | Endpoint              | Description                   |
| ------ | --------------------- | ----------------------------- |
| POST   | `/api/auth/register`  | Create account, receive JWT   |
| POST   | `/api/auth/login`     | Authenticate, receive JWT     |

### Resumes (JWT required)
| Method | Endpoint              | Description                          |
| ------ | --------------------- | ------------------------------------ |
| POST   | `/api/resumes/upload` | Upload PDF, extract text, persist    |
| GET    | `/api/resumes`        | Paginated list of owned resumes      |
| GET    | `/api/resumes/{id}`   | Single resume metadata               |
| DELETE | `/api/resumes/{id}`   | Delete resume + file                 |

### Job Descriptions (JWT required)
| Method | Endpoint            | Description                       |
| ------ | ------------------- | --------------------------------- |
| POST   | `/api/jobs`         | Create JD                         |
| GET    | `/api/jobs`         | Paginated list, optional search   |
| GET    | `/api/jobs/{id}`    | Single JD                         |
| PUT    | `/api/jobs/{id}`    | Update JD                         |
| DELETE | `/api/jobs/{id}`    | Delete JD                         |

### Match Analyses (JWT required)
| Method | Endpoint                | Description                                  |
| ------ | ----------------------- | -------------------------------------------- |
| POST   | `/api/matches/analyze`  | Run scoring on a (resume, JD) pair           |
| GET    | `/api/matches`          | Paginated history of past analyses           |
| GET    | `/api/matches/{id}`     | Full analysis result                         |

---

## Engineering decisions worth noting

- **Multi-tenant isolation by default.** Every repository query goes through `findByIdAndUser(...)` — no user can ever see another's data, even with a forged ID.
- **Lazy-loading via `JOIN FETCH`.** Match queries pre-load the resume and JD to avoid `LazyInitializationException` when serializing to JSON.
- **TEXT columns over CLOB.** Hibernate 6's CLOB handling on Postgres breaks under auto-commit; explicit `TEXT` columns sidestep the issue entirely.
- **Stateless JWT auth.** No session storage, scales horizontally.
- **Curated 179-skill dictionary.** Combined with Jaccard similarity for tokens outside the dictionary, this hits 80%+ keyword recall on real tech JDs while staying explainable.

---

## What's next

- [ ] Deploy backend to Railway / Render
- [ ] Add semantic similarity via embeddings (`text-embedding-3-small` or open-source)
- [ ] Resume tailoring suggestions (LLM-powered)
- [ ] Multi-page resume support beyond 5 MB
- [ ] Match history export (CSV / PDF)

---

## License

MIT — built by [Aman Patel](https://github.com/Aman100705) · 2026
