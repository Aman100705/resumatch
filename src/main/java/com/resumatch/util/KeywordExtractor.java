package com.resumatch.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts meaningful keywords (primarily tech skills and domain terms)
 * from resume/JD text. Uses a curated skill dictionary + frequency analysis
 * for tokens not in the dictionary.
 */
@Component
public class KeywordExtractor {

    /**
     * Curated tech/skills dictionary. Multi-word phrases are matched as-is.
     * Uses HashSet to silently ignore any accidental duplicates.
     */
    private static final Set<String> TECH_SKILLS = buildTechSkills();

    private static Set<String> buildTechSkills() {
        Set<String> set = new HashSet<>();
        // Languages
        set.addAll(List.of(
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "golang", "rust",
            "kotlin", "swift", "php", "ruby", "scala", "r", "dart", "perl"
        ));
        // Backend frameworks
        set.addAll(List.of(
            "spring boot", "spring", "spring mvc", "spring security", "spring cloud",
            "hibernate", "jpa", "node.js", "nodejs", "express", "express.js",
            "django", "flask", "fastapi", "nestjs", "laravel", "ruby on rails",
            ".net", "asp.net"
        ));
        // Frontend
        set.addAll(List.of(
            "react", "react.js", "reactjs", "angular", "vue", "vue.js", "svelte",
            "next.js", "nextjs", "nuxt", "redux", "tailwind", "tailwindcss", "bootstrap",
            "html", "html5", "css", "css3", "sass", "scss", "material ui", "mui", "chakra ui"
        ));
        // Databases
        set.addAll(List.of(
            "mysql", "postgresql", "postgres", "mongodb", "redis", "firebase", "firestore",
            "cassandra", "dynamodb", "sqlite", "oracle", "sql server", "mariadb",
            "elasticsearch", "neo4j"
        ));
        // Cloud & DevOps
        set.addAll(List.of(
            "aws", "amazon web services", "ec2", "s3", "lambda", "rds",
            "azure", "google cloud", "gcp",
            "docker", "kubernetes", "k8s", "terraform", "ansible", "jenkins",
            "github actions", "gitlab ci", "circleci",
            "ci/cd", "cicd", "nginx", "apache", "linux", "bash"
        ));
        // AI/ML
        set.addAll(List.of(
            "machine learning", "deep learning", "tensorflow", "pytorch", "keras",
            "scikit-learn", "sklearn", "pandas", "numpy", "opencv", "nlp",
            "llm", "openai", "gpt", "gemini", "langchain", "huggingface",
            "prompt engineering", "rag", "embeddings", "vector database"
        ));
        // Mobile
        set.addAll(List.of("android", "ios", "react native", "flutter"));
        // Tools & concepts
        set.addAll(List.of(
            "git", "github", "gitlab", "bitbucket", "jira", "confluence",
            "rest", "restful", "rest api", "graphql", "grpc", "websocket", "websockets",
            "microservices", "monolith", "serverless", "oauth", "jwt", "saml",
            "agile", "scrum", "kanban", "tdd", "bdd",
            "oop", "functional programming", "design patterns", "solid principles",
            "unit testing", "integration testing", "junit", "mockito", "jest",
            "postman", "swagger", "openapi"
        ));
        // Data & Analytics
        set.addAll(List.of(
            "sql", "nosql", "etl", "kafka", "rabbitmq", "spark", "hadoop",
            "tableau", "power bi", "data analysis", "data visualization"
        ));
        // Soft / misc
        set.addAll(List.of(
            "data structures", "algorithms", "dsa", "system design",
            "problem solving", "communication", "leadership", "teamwork"
        ));
        return Collections.unmodifiableSet(set);
    }

    private static final Set<String> STOP_WORDS = buildStopWords();

    private static Set<String> buildStopWords() {
        Set<String> set = new HashSet<>();
        set.addAll(List.of(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "must", "can", "this", "that", "these", "those",
            "i", "you", "he", "she", "it", "we", "they", "my", "your", "his", "her",
            "its", "our", "their", "me", "him", "us", "them",
            "to", "of", "in", "on", "at", "by", "for", "with", "about", "against",
            "between", "into", "through", "during", "before", "after", "above", "below",
            "from", "up", "down", "out", "off", "over", "under", "again", "further",
            "then", "once", "here", "there", "when", "where", "why", "how",
            "all", "any", "both", "each", "few", "more", "most", "other", "some", "such",
            "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
            "s", "t", "just", "don", "now",
            "work", "experience", "role", "team", "company", "job", "position", "looking",
            "strong", "good", "great", "excellent", "responsible", "including", "using",
            "use", "used", "ability", "knowledge", "skills", "required", "preferred",
            "years", "year", "day", "plus", "nice", "bonus", "etc"
        ));
        return Collections.unmodifiableSet(set);
    }

    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z][a-zA-Z0-9.+#]*");

    /**
     * Extracts a normalized, deduplicated set of keywords from text.
     */
    public Set<String> extract(String text) {
        if (text == null || text.isBlank()) return Set.of();
        String lower = text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();

        // 1. Multi-word skill detection from dictionary (these have priority)
        for (String skill : TECH_SKILLS) {
            if (skill.contains(" ") && lower.contains(skill)) {
                found.add(skill);
            }
        }

        // 2. Single-word tokens — keep single-word skills from dictionary,
        //    drop stop words, keep everything else that looks technical.
        var matcher = TOKEN.matcher(lower);
        Map<String, Integer> frequency = new HashMap<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() < 2 || token.length() > 30) continue;
            if (STOP_WORDS.contains(token)) continue;
            frequency.merge(token, 1, Integer::sum);
        }

        // Add all single-word dictionary skills that appear
        for (String skill : TECH_SKILLS) {
            if (!skill.contains(" ") && frequency.containsKey(skill)) {
                found.add(skill);
            }
        }

        return found;
    }

    /**
     * Extracts keywords specifically from a job description —
     * includes both dictionary skills and high-frequency non-stopword tokens.
     */
    public Set<String> extractFromJd(String text) {
        Set<String> result = new LinkedHashSet<>(extract(text));

        if (text == null || text.isBlank()) return result;
        String lower = text.toLowerCase();
        Map<String, Integer> frequency = new HashMap<>();

        var matcher = TOKEN.matcher(lower);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() < 3 || token.length() > 30) continue;
            if (STOP_WORDS.contains(token)) continue;
            frequency.merge(token, 1, Integer::sum);
        }

        // Add tokens that appear at least 2 times
        frequency.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30)
                .map(Map.Entry::getKey)
                .forEach(result::add);

        return result;
    }

    public boolean isSkill(String keyword) {
        return TECH_SKILLS.contains(keyword.toLowerCase());
    }

    public Set<String> filterSkills(Set<String> keywords) {
        return keywords.stream()
                .filter(this::isSkill)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
