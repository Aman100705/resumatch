package com.resumatch.repository;

import com.resumatch.entity.Resume;
import com.resumatch.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByUser(User user, Pageable pageable);
    Optional<Resume> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}
