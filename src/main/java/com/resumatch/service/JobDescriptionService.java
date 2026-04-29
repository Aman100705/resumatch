package com.resumatch.service;

import com.resumatch.dto.JobDescriptionDtos;
import com.resumatch.entity.JobDescription;
import com.resumatch.entity.User;
import com.resumatch.exception.ResourceNotFoundException;
import com.resumatch.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionService {

    private final JobDescriptionRepository repository;

    @Transactional
    public JobDescription create(User user, JobDescriptionDtos.CreateRequest req) {
        JobDescription jd = JobDescription.builder()
                .user(user)
                .title(req.getTitle().trim())
                .company(req.getCompany() == null ? null : req.getCompany().trim())
                .content(req.getContent().trim())
                .build();
        JobDescription saved = repository.save(jd);
        log.info("User {} created JD id={}", user.getEmail(), saved.getId());
        return saved;
    }

    public Page<JobDescription> listForUser(User user, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return repository.findByUserAndTitleContainingIgnoreCase(user, search.trim(), pageable);
        }
        return repository.findByUser(user, pageable);
    }

    public JobDescription getOwned(User user, Long id) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found: " + id));
    }

    @Transactional
    public JobDescription update(User user, Long id, JobDescriptionDtos.CreateRequest req) {
        JobDescription existing = getOwned(user, id);
        existing.setTitle(req.getTitle().trim());
        existing.setCompany(req.getCompany() == null ? null : req.getCompany().trim());
        existing.setContent(req.getContent().trim());
        return repository.save(existing);
    }

    @Transactional
    public void delete(User user, Long id) {
        JobDescription jd = getOwned(user, id);
        repository.delete(jd);
    }
}
