package com.resumatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    /** Overall match score from 0 to 100. */
    @Column(nullable = false)
    private Double matchScore;

    /** Keyword similarity subscore (0-100). */
    @Column(nullable = false)
    private Double keywordScore;

    /** Skills coverage subscore (0-100). */
    @Column(nullable = false)
    private Double skillsScore;

    /** Text similarity subscore (0-100). */
    @Column(nullable = false)
    private Double textScore;

    /** Keywords present in both documents. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_matched_keywords", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> matchedKeywords = new ArrayList<>();

    /** Keywords in the JD but missing from the resume (the "gap"). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_missing_keywords", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> missingKeywords = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
