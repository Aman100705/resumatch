package com.resumatch.dto;

import com.resumatch.entity.MatchAnalysis;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

public class MatchDtos {

    @Data
    public static class AnalyzeRequest {
        @NotNull(message = "resumeId is required")
        private Long resumeId;

        @NotNull(message = "jobDescriptionId is required")
        private Long jobDescriptionId;
    }

    @Data
    @Builder
    public static class AnalyzeResponse {
        private Long id;
        private Long resumeId;
        private String resumeName;
        private Long jobDescriptionId;
        private String jobTitle;
        private String company;

        private Double matchScore;
        private Double keywordScore;
        private Double skillsScore;
        private Double textScore;

        private String verdict;

        private List<String> matchedKeywords;
        private List<String> missingKeywords;

        private String recommendation;
        private Instant createdAt;

        public static AnalyzeResponse from(MatchAnalysis m) {
            return AnalyzeResponse.builder()
                    .id(m.getId())
                    .resumeId(m.getResume().getId())
                    .resumeName(m.getResume().getOriginalFilename())
                    .jobDescriptionId(m.getJobDescription().getId())
                    .jobTitle(m.getJobDescription().getTitle())
                    .company(m.getJobDescription().getCompany())
                    .matchScore(m.getMatchScore())
                    .keywordScore(m.getKeywordScore())
                    .skillsScore(m.getSkillsScore())
                    .textScore(m.getTextScore())
                    .verdict(computeVerdict(m.getMatchScore()))
                    .matchedKeywords(m.getMatchedKeywords())
                    .missingKeywords(m.getMissingKeywords())
                    .recommendation(buildRecommendation(m))
                    .createdAt(m.getCreatedAt())
                    .build();
        }

        private static String computeVerdict(Double score) {
            if (score == null) return "Unknown";
            if (score >= 80) return "Excellent match";
            if (score >= 65) return "Strong match";
            if (score >= 50) return "Decent match";
            if (score >= 35) return "Weak match";
            return "Poor match";
        }

        private static String buildRecommendation(MatchAnalysis m) {
            List<String> missing = m.getMissingKeywords();
            if (missing == null || missing.isEmpty()) {
                return "Your resume already covers the major keywords from this JD. Focus on tailoring impact statements to the role.";
            }
            int showCount = Math.min(5, missing.size());
            return String.format(
                "Consider adding these keywords to your resume (if you have the experience): %s. " +
                "Also tailor your impact bullets to match the JD's language.",
                String.join(", ", missing.subList(0, showCount))
            );
        }
    }
}
