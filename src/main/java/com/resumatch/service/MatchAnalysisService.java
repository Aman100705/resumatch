package com.resumatch.service;

import com.resumatch.entity.JobDescription;
import com.resumatch.entity.MatchAnalysis;
import com.resumatch.entity.Resume;
import com.resumatch.entity.User;
import com.resumatch.exception.ResourceNotFoundException;
import com.resumatch.repository.MatchAnalysisRepository;
import com.resumatch.util.KeywordExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Core scoring service. Given a resume and a job description,
 * produces a match score with explanatory subscores and keyword breakdown.
 *
 * Scoring = 0.5 * keyword_score + 0.3 * skills_score + 0.2 * text_score
 *
 * Why this weighting: keywords (including non-skill terms) matter most because
 * ATS systems use them. Tech skill coverage is next. Raw text similarity is
 * a sanity check.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchAnalysisService {

    private final MatchAnalysisRepository repository;
    private final KeywordExtractor keywordExtractor;
    private final ResumeService resumeService;
    private final JobDescriptionService jobDescriptionService;

    private static final double KEYWORD_WEIGHT = 0.50;
    private static final double SKILLS_WEIGHT  = 0.30;
    private static final double TEXT_WEIGHT    = 0.20;

    private static final Pattern WORD_SPLIT = Pattern.compile("\\W+");

    @Transactional
    public MatchAnalysis analyze(User user, Long resumeId, Long jdId) {
        Resume resume = resumeService.getOwnedResume(user, resumeId);
        JobDescription jd = jobDescriptionService.getOwned(user, jdId);

        String resumeText = resume.getExtractedText() == null ? "" : resume.getExtractedText();
        String jdText = jd.getContent() == null ? "" : jd.getContent();

        // 1. Extract keywords
        Set<String> resumeKeywords = keywordExtractor.extract(resumeText);
        Set<String> jdKeywords = keywordExtractor.extractFromJd(jdText);

        // 2. Keyword overlap scoring
        Set<String> matched = new LinkedHashSet<>(resumeKeywords);
        matched.retainAll(jdKeywords);

        Set<String> missing = new LinkedHashSet<>(jdKeywords);
        missing.removeAll(resumeKeywords);

        double keywordScore = jdKeywords.isEmpty()
                ? 0.0
                : 100.0 * matched.size() / jdKeywords.size();

        // 3. Skills-specific scoring (only dictionary skills, weighted heavier)
        Set<String> resumeSkills = keywordExtractor.filterSkills(resumeKeywords);
        Set<String> jdSkills = keywordExtractor.filterSkills(jdKeywords);

        Set<String> matchedSkills = new HashSet<>(resumeSkills);
        matchedSkills.retainAll(jdSkills);

        double skillsScore = jdSkills.isEmpty()
                ? 100.0   // JD has no detectable skills; don't penalize
                : 100.0 * matchedSkills.size() / jdSkills.size();

        // 4. Text similarity (Jaccard over word sets) as a sanity check
        double textScore = computeTextSimilarity(resumeText, jdText) * 100.0;

        // 5. Weighted final score
        double finalScore = round1(
                keywordScore * KEYWORD_WEIGHT +
                skillsScore * SKILLS_WEIGHT +
                textScore * TEXT_WEIGHT
        );

        // Sort matched/missing lists for stable, readable output
        List<String> matchedSorted = new ArrayList<>(matched);
        Collections.sort(matchedSorted);
        List<String> missingSorted = missing.stream()
                .filter(k -> !isGenericWord(k))  // filter obvious junk
                .sorted()
                .limit(25)
                .toList();

        MatchAnalysis analysis = MatchAnalysis.builder()
                .user(user)
                .resume(resume)
                .jobDescription(jd)
                .matchScore(finalScore)
                .keywordScore(round1(keywordScore))
                .skillsScore(round1(skillsScore))
                .textScore(round1(textScore))
                .matchedKeywords(matchedSorted)
                .missingKeywords(new ArrayList<>(missingSorted))
                .build();

        MatchAnalysis saved = repository.save(analysis);
        log.info("Match for user={} resume={} jd={} score={}",
                user.getEmail(), resumeId, jdId, finalScore);
        return saved;
    }

    public Page<MatchAnalysis> listForUser(User user, Pageable pageable) {
        return repository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public MatchAnalysis getOwned(User user, Long id) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + id));
    }

    @Transactional
    public void delete(User user, Long id) {
        MatchAnalysis m = getOwned(user, id);
        repository.delete(m);
    }

    // --- Helpers ---

    /**
     * Jaccard similarity over the two documents' word sets:
     * |intersection| / |union|.
     *
     * Note: this is deliberately NOT Apache Commons Text's JaccardSimilarity.
     * That implementation compares sets of *characters*, so any two English
     * documents score ~0.85-0.95 regardless of content — a matching JD and an
     * unrelated one came out only ~10 points apart. Comparing word sets gives
     * the component real discriminatory power.
     */
    private double computeTextSimilarity(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) return 0.0;

        Set<String> wordsA = toWordSet(a);
        Set<String> wordsB = toWordSet(b);
        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);

        Set<String> union = new HashSet<>(wordsA);
        union.addAll(wordsB);

        return (double) intersection.size() / union.size();
    }

    private Set<String> toWordSet(String text) {
        Set<String> words = new HashSet<>();
        for (String token : WORD_SPLIT.split(text.toLowerCase())) {
            if (token.length() >= 2) words.add(token);
        }
        return words;
    }

    private boolean isGenericWord(String k) {
        // Extra filter for common non-actionable terms that slip through
        return k.length() < 3;
    }

    private double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
