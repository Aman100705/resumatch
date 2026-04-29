package com.resumatch.repository;

import com.resumatch.entity.MatchAnalysis;
import com.resumatch.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchAnalysisRepository extends JpaRepository<MatchAnalysis, Long> {

    /**
     * Returns matches for a user with resume + JD eagerly fetched
     * so we can build the response DTO without a LazyInitializationException.
     */
    @Query(
        value = """
            SELECT m FROM MatchAnalysis m
            LEFT JOIN FETCH m.resume
            LEFT JOIN FETCH m.jobDescription
            WHERE m.user = :user
            ORDER BY m.createdAt DESC
        """,
        countQuery = "SELECT COUNT(m) FROM MatchAnalysis m WHERE m.user = :user"
    )
    Page<MatchAnalysis> findByUserOrderByCreatedAtDesc(
        @Param("user") User user,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM MatchAnalysis m
        LEFT JOIN FETCH m.resume
        LEFT JOIN FETCH m.jobDescription
        WHERE m.id = :id AND m.user = :user
    """)
    Optional<MatchAnalysis> findByIdAndUser(
        @Param("id") Long id,
        @Param("user") User user
    );
}
