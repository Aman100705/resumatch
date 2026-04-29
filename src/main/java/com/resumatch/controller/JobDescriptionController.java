package com.resumatch.controller;

import com.resumatch.dto.JobDescriptionDtos;
import com.resumatch.entity.JobDescription;
import com.resumatch.entity.User;
import com.resumatch.service.JobDescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-descriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Job Descriptions", description = "Create and manage job descriptions to match against")
public class JobDescriptionController {

    private final JobDescriptionService service;

    @PostMapping
    @Operation(summary = "Create a new job description")
    public ResponseEntity<JobDescriptionDtos.Response> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody JobDescriptionDtos.CreateRequest req
    ) {
        JobDescription jd = service.create(user, req);
        return ResponseEntity.ok(JobDescriptionDtos.Response.from(jd));
    }

    @GetMapping
    @Operation(summary = "List your job descriptions",
               description = "Paginated. Optional ?search= filters by title.")
    public ResponseEntity<Page<JobDescriptionDtos.Summary>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<JobDescriptionDtos.Summary> page = service.listForUser(user, search, pageable)
                .map(JobDescriptionDtos.Summary::from);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific job description")
    public ResponseEntity<JobDescriptionDtos.Response> get(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(JobDescriptionDtos.Response.from(service.getOwned(user, id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job description")
    public ResponseEntity<JobDescriptionDtos.Response> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody JobDescriptionDtos.CreateRequest req
    ) {
        JobDescription jd = service.update(user, id, req);
        return ResponseEntity.ok(JobDescriptionDtos.Response.from(jd));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job description")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
