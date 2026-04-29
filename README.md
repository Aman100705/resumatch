# ResuMatch 🎯

> **AI-powered Resume ↔ Job Description matcher.** Upload a resume PDF, paste a job description, get a data-driven match score with keyword gap analysis.

Built with **Spring Boot 3.4 · PostgreSQL · JWT · Apache PDFBox · OpenAPI**.

---

## 🎯 What it does

ResuMatch solves a real problem: you apply to a job, you don't hear back, and you wonder *why*. Recruiters use Applicant Tracking Systems (ATS) that filter resumes by keyword match. If your resume doesn't hit the right keywords, it never reaches a human.

ResuMatch is a REST API that does exactly what ATS does — but *for you*. You get:

- A **match score from 0–100** with 3 explained subscores (keywords, skills, text similarity)
- The **keywords you're already matching** (validate your current resume)
- The **keywords you're missing** (the gap analysis — what to add)
- **Recommendations** for how to improve your resume for this specific JD

---

## 🏗️ Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Framework | **Spring Boot 3.4** | Production-standard Java framework |
| Language | **Java 21** (LTS) | Modern Java with records, switch expressions, pattern matching |
| Database | **PostgreSQL 16** | Rock-solid relational DB with JSONB if we need it |
| ORM | **Spring Data JPA + Hibernate** | Repository pattern, no SQL boilerplate |
| Security | **Spring Security + JWT (jjwt 0.12)** | Stateless auth, role-based access |
| PDF parsing | **Apache PDFBox 3.x** | Industry standard for extracting text from PDFs |
| Text similarity | **Apache Commons Text (Jaccard)** | Lightweight, no ML dependencies needed |
| API docs | **springdoc-openapi (Swagger UI)** | Self-documenting API at `/swagger-ui.html` |
| Build | **Maven** | Simpler than Gradle for first project |

---

## 🚀 Quick Start

### Prerequisites

You'll need these installed (all free):

- **Java 21** (JDK). Verify: `java -version`
- **Maven 3.9+**. Verify: `mvn -version`
- **PostgreSQL 16**. Verify: `psql --version`
- **IntelliJ IDEA** (Community Edition is fine). Or VS Code with Java extensions.

### 1. Clone and open

```bash
git clone https://github.com/YOUR_USERNAME/resumatch.git
cd resumatch
```

Open the folder in **IntelliJ** → *"Open"* → select the `resumatch/` folder → IntelliJ auto-detects Maven → wait ~60 seconds for dependencies to download.

### 2. Set up the database

Make sure Postgres is running (`brew services start postgresql@16` on Mac), then:

```bash
psql postgres
```

```sql
CREATE USER resumatch_user WITH PASSWORD 'resumatch_pass';
CREATE DATABASE resumatch_db OWNER resumatch_user;
GRANT ALL PRIVILEGES ON DATABASE resumatch_db TO resumatch_user;
\q
```

The app will auto-create all tables via Hibernate on first boot.

### 3. Set environment variables (optional for local dev)

Default values are in `application.yml`. For production, override:

```bash
export DB_USERNAME=resumatch_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=$(openssl rand -base64 48)
export UPLOAD_DIR=/var/data/resumatch/uploads
```

### 4. Run it

From the project root:

```bash
./mvnw spring-boot:run
```

Or from IntelliJ: right-click `ResuMatchApplication.java` → **Run**.

You should see:

```
Started ResuMatchApplication in 3.2 seconds
Tomcat started on port 8080
```

### 5. Open Swagger UI

Visit **http://localhost:8080/swagger-ui.html** — every endpoint is documented and testable directly from the browser. 🎉

---

## 📡 API Endpoints

### 🔐 Auth (public)
| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account, get JWT |
| POST | `/api/auth/login` | Log in, get JWT |

### 📄 Resumes (JWT required)
| Method | Path | Description |
|---|---|---|
| POST | `/api/resumes/upload` | Upload PDF (multipart form, max 5MB) |
| GET | `/api/resumes` | List your resumes (paginated) |
| GET | `/api/resumes/{id}` | Get resume metadata |
| GET | `/api/resumes/{id}/text` | Get extracted text |
| DELETE | `/api/resumes/{id}` | Delete a resume |

### 💼 Job Descriptions (JWT required)
| Method | Path | Description |
|---|---|---|
| POST | `/api/job-descriptions` | Create a JD |
| GET | `/api/job-descriptions` | List JDs (with optional `?search=`) |
| GET | `/api/job-descriptions/{id}` | Get full JD |
| PUT | `/api/job-descriptions/{id}` | Update a JD |
| DELETE | `/api/job-descriptions/{id}` | Delete a JD |

### 🎯 Matches (JWT required)
| Method | Path | Description |
|---|---|---|
| POST | `/api/matches/analyze` | **The core endpoint.** Analyze resume vs JD |
| GET | `/api/matches` | List past match analyses |
| GET | `/api/matches/{id}` | Get a specific match |
| DELETE | `/api/matches/{id}` | Delete a match |

---

## 🧪 End-to-End Walkthrough (via Postman)

### Step 1: Register

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "Test User",
  "email": "test@example.com",
  "password": "password123"
}
```

Response includes a JWT token. **Copy it.**

### Step 2: Set Authorization header

For every request below, add:
```
Authorization: Bearer <paste-token-here>
```

### Step 3: Upload a resume

```
POST http://localhost:8080/api/resumes/upload
Body: form-data
  Key: file   Type: File   Value: <your-resume.pdf>
```

Response gives you a resume ID — e.g., `"id": 1`.

### Step 4: Create a job description

```
POST http://localhost:8080/api/job-descriptions
Content-Type: application/json

{
  "title": "Java Backend Intern",
  "company": "Cool Startup",
  "content": "We're looking for a Spring Boot engineer with JWT, PostgreSQL, Redis, and Docker experience. Must know microservices and CI/CD..."
}
```

Response gives you a JD ID — e.g., `"id": 1`.

### Step 5: Run the match

```
POST http://localhost:8080/api/matches/analyze
Content-Type: application/json

{
  "resumeId": 1,
  "jobDescriptionId": 1
}
```

Response:
```json
{
  "id": 1,
  "resumeId": 1,
  "jobDescriptionId": 1,
  "matchScore": 72.4,
  "keywordScore": 68.0,
  "skillsScore": 85.0,
  "textScore": 45.2,
  "verdict": "Strong match",
  "matchedKeywords": ["java", "spring boot", "jwt", "postgresql", "docker", "rest"],
  "missingKeywords": ["redis", "microservices", "ci/cd"],
  "recommendation": "Consider adding these keywords to your resume: redis, microservices, ci/cd..."
}
```

**That's the magic.** 🎯

---

## 🧠 How the scoring works

```
Final Score = 0.5 × Keyword Score  +  0.3 × Skills Score  +  0.2 × Text Score
```

| Subscore | What it measures | How |
|---|---|---|
| **Keyword Score** | Overlap between all JD keywords and resume text | `matched_count / jd_keyword_count × 100` |
| **Skills Score** | Coverage of tech skills specifically (weighted heavier) | Filtered to curated skill dictionary |
| **Text Score** | Overall lexical similarity | Jaccard similarity on lowercased word sets |

### Why this weighting?

ATS systems care about keywords first. Tech skills are most important (they're searched for explicitly by recruiters). Raw text similarity is a sanity check that catches edge cases.

### Why not embeddings / GPT?

This project intentionally uses classical NLP to keep it:
1. **Free** — no API costs or rate limits
2. **Fast** — scores in under 100ms
3. **Explainable** — you can see exactly *why* a score is what it is
4. **Deterministic** — same inputs → same outputs

Future versions (v2+) could add optional semantic embeddings for synonym detection (e.g., "Node.js" ≈ "JavaScript runtime").

---

## 📁 Project Structure

```
resumatch/
├── pom.xml
├── src/main/
│   ├── java/com/resumatch/
│   │   ├── ResuMatchApplication.java      # Entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java        # JWT + CORS + route rules
│   │   ├── controller/                    # REST endpoints
│   │   │   ├── AuthController.java
│   │   │   ├── ResumeController.java
│   │   │   ├── JobDescriptionController.java
│   │   │   └── MatchController.java
│   │   ├── dto/                           # Request/response contracts
│   │   ├── entity/                        # JPA entities (tables)
│   │   ├── exception/                     # Custom exceptions + global handler
│   │   ├── repository/                    # Spring Data JPA interfaces
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java      # Generates + validates JWTs
│   │   │   ├── JwtAuthFilter.java         # Intercepts every request
│   │   │   └── CustomUserDetailsService.java
│   │   ├── service/                       # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── ResumeService.java
│   │   │   ├── JobDescriptionService.java
│   │   │   └── MatchAnalysisService.java  # The scoring brain
│   │   └── util/
│   │       ├── PdfTextExtractor.java      # Apache PDFBox wrapper
│   │       └── KeywordExtractor.java      # Tech skill dictionary + NLP
│   └── resources/
│       └── application.yml                # Config
└── uploads/                               # Uploaded PDFs (gitignored)
```

---

## 🚀 Deployment

This API is deployment-ready for any platform that runs Java:

- **Railway** (easiest, free tier): connect GitHub → auto-deploys on push
- **Render**: free PostgreSQL + Java support
- **AWS Elastic Beanstalk**: enterprise-grade
- **Google Cloud Run**: containerize with Docker, pay per request

Don't forget to set environment variables (`JWT_SECRET`, `DB_USERNAME`, etc.) in whichever platform you choose.

---

## 🛣️ Roadmap (things to add for v2+)

- [ ] **Resume upload via URL** (for LinkedIn/GDrive links)
- [ ] **Semantic similarity** with sentence-transformers for synonym detection
- [ ] **Async analysis** with Spring's `@Async` + a progress endpoint (for very large resumes)
- [ ] **Batch matching** — compare one resume against N job descriptions in one call
- [ ] **Export match report as PDF**
- [ ] **Admin dashboard** with usage stats
- [ ] **Email verification flow** with `spring-boot-starter-mail`
- [ ] **Refresh tokens** + token blacklist for logout
- [ ] **Docker + docker-compose** setup
- [ ] **GitHub Actions CI** — lint, test, build on every push

---

## 📄 License

MIT — fork it, build on it, make it yours.

---

**Built by Aman Patel** · [Portfolio](https://aman-portfolio-lilac.vercel.app) · [Email](mailto:ap3668@srmist.edu.in)
