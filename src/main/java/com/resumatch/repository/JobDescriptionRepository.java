package com.resumatch.repository;

import com.resumatch.entity.JobDescription;
import com.resumatch.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    Page<JobDescription> findByUser(User user, Pageable pageable);
    Page<JobDescription> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);
    Optional<JobDescription> findByIdAndUser(Long id, User user);
}
