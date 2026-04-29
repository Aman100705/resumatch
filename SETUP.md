# 🚀 SETUP.md — First-time Setup Guide

> You downloaded this project? Great. Follow this file step-by-step. By the end (15 min) you'll have a running Spring Boot API.

---

## Prerequisites checklist

Before anything, confirm these are installed on your Mac:

```bash
java -version   # Should say 21.x or higher
mvn -version    # Should say 3.9+
psql --version  # Should say 16.x
```

**If any of those fail**, install them first via Homebrew:

```bash
brew install --cask temurin@21
brew install maven postgresql@16
brew services start postgresql@16
```

Then add postgres to your PATH:
```bash
echo 'export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

---

## Step 1: Set up the database

Open a new terminal window and run:

```bash
psql postgres
```

You should see a prompt: `postgres=#`

Paste these 4 lines one at a time:

```sql
CREATE USER resumatch_user WITH PASSWORD 'resumatch_pass';
CREATE DATABASE resumatch_db OWNER resumatch_user;
GRANT ALL PRIVILEGES ON DATABASE resumatch_db TO resumatch_user;
\q
```

**Verify** the database works:
```bash
psql -U resumatch_user -d resumatch_db
# When prompted for password, type: resumatch_pass
```
You should see `resumatch_db=>`. Type `\q` and hit Enter to exit.

---

## Step 2: Open the project in IntelliJ

1. Launch **IntelliJ IDEA Community Edition**
2. On the welcome screen → **Open**
3. Navigate to the unzipped `resumatch` folder → click **Open**
4. IntelliJ detects it's a Maven project → click **"Load Maven Project"** or **"Trust Project"**
5. Wait 1–3 minutes — IntelliJ downloads all dependencies (~200 MB). You'll see a progress bar at the bottom.

### While you wait, install 2 IntelliJ plugins

`IntelliJ IDEA → Settings → Plugins`

Search and install:
1. **Lombok** (critical — project won't compile without it)
2. **Spring Boot** (comes pre-installed; check it's enabled)

After install → restart IntelliJ.

### Enable annotation processing (required for Lombok)

`Settings → Build, Execution, Deployment → Compiler → Annotation Processors` → check **"Enable annotation processing"** → Apply.

---

## Step 3: Run the app

Two ways:

### Option A — IntelliJ UI (recommended)
1. In the left sidebar, open `src/main/java/com/resumatch/ResuMatchApplication.java`
2. Click the green **▶ play button** next to the `main` method
3. Watch the console at the bottom

### Option B — terminal
```bash
./mvnw spring-boot:run
```

### ✅ Success looks like:

```
...
Tomcat started on port 8080 (http) with context path '/'
Started ResuMatchApplication in 3.245 seconds (process running for 3.812)
```

---

## Step 4: Verify it works

Open in your browser:

**http://localhost:8080/swagger-ui.html**

You should see a clean Swagger UI with all your endpoints grouped:
- Authentication
- Resumes
- Job Descriptions
- Matches

If you see this — **your Spring Boot API is LIVE.** 🎉

---

## Step 5: Import the Postman collection

1. Open **Postman**
2. Click **Import** (top left)
3. Drag the `ResuMatch.postman_collection.json` file into the window
4. Collection imported

---

## Step 6: Test the full flow (takes 2 minutes)

### 6a. Register
Click **1. Register** → hit **Send**. You'll get a response with a `token` — a long string starting with `eyJ...`.

### 6b. Set the token as a variable
1. In Postman, click the **ResuMatch API** collection (top of left sidebar)
2. Click the **Variables** tab
3. Find `token` → paste your JWT into the **Current Value** column
4. Click **Save** (top right)

Now every authenticated request will automatically use your token.

### 6c. Upload a resume
1. Click **3. Upload Resume**
2. Go to the **Body** tab
3. Next to `file`, click **Select Files** → pick your resume PDF
4. Click **Send**

Response gives you a resume ID. Say it's `1`.

### 6d. Create a job description
Click **5. Create Job Description** → Send. It uses a pre-filled Spring Boot JD. Response gives a JD ID, usually `1`.

### 6e. The magic — analyze the match

Click **7. Analyze Match** → Send.

You'll get back something like:
```json
{
  "matchScore": 72.4,
  "keywordScore": 68.0,
  "skillsScore": 85.0,
  "textScore": 45.2,
  "verdict": "Strong match",
  "matchedKeywords": ["java", "spring boot", "jwt", "postgresql", "docker"],
  "missingKeywords": ["redis", "microservices", "kubernetes"],
  "recommendation": "Consider adding these keywords to your resume..."
}
```

**You just built a working ATS resume scorer. 🎯**

---

## 🧯 If something breaks

### "Port 8080 already in use"
Something else is on that port. Kill it:
```bash
lsof -ti:8080 | xargs kill -9
```
Or change the port in `src/main/resources/application.yml` → `server.port: 8081`.

### "FATAL: password authentication failed for user 'resumatch_user'"
You typed the wrong DB password. Either recreate the user:
```bash
psql postgres -c "ALTER USER resumatch_user WITH PASSWORD 'resumatch_pass';"
```
Or update the password in `application.yml` under `spring.datasource.password`.

### "java.lang.UnsupportedClassVersionError"
You're running on Java < 21. Install JDK 21:
```bash
brew install --cask temurin@21
```
Then in IntelliJ: `File → Project Structure → SDK` → select JDK 21.

### Lombok errors — "cannot find symbol: getEmail()"
Your IDE didn't pick up Lombok. Either:
- Install the Lombok plugin (see Step 2)
- Enable annotation processing (see Step 2)
- Restart IntelliJ

### "extracted text is too short"
Your uploaded PDF is a scanned image, not a real text PDF. Use an export of a real resume from Word or Google Docs, not a scanned one.

### Any other error
Paste the full stacktrace from the IntelliJ console to your mentor (Claude).

---

## 📣 What to do after it's running

1. ⭐ **Commit everything to GitHub** — this project is resume-worthy:
   ```bash
   git init
   git add .
   git commit -m "initial commit: ResuMatch backend"
   git remote add origin https://github.com/YOUR_USERNAME/resumatch.git
   git push -u origin main
   ```

2. 🚀 **Deploy it** — Railway is easiest: connect GitHub → auto-detects Spring Boot → deploys. Add the same env vars in Railway's dashboard.

3. 📸 **Record a demo video** — screen-record yourself hitting the `/api/matches/analyze` endpoint in Postman and getting a match score back. Post on LinkedIn.

4. 🔥 **Build the frontend** — React UI that consumes this API. Upload PDF → see animated score gauge → keyword pills → recommendations. This is what turns an API into a full product.

Happy shipping. 🚢
