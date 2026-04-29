package com.resumatch.controller;

import com.resumatch.dto.MatchDtos;
import com.resumatch.entity.MatchAnalysis;
import com.resumatch.entity.User;
import com.resumatch.service.MatchAnalysisService;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Matches", description = "Run and view resume ↔ JD match analyses")
public class MatchController {

    private final MatchAnalysisService service;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a resume against a job description",
               description = "Returns match score, keyword breakdown, and recommendations.")
    public ResponseEntity<MatchDtos.AnalyzeResponse> analyze(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MatchDtos.AnalyzeRequest req
    ) {
        MatchAnalysis match = service.analyze(user, req.getResumeId(), req.getJobDescriptionId());
        return ResponseEntity.ok(MatchDtos.AnalyzeResponse.from(match));
    }

    @GetMapping
    @Operation(summary = "List all match analyses you've run")
    public ResponseEntity<Page<MatchDtos.AnalyzeResponse>> list(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<MatchDtos.AnalyzeResponse> page = service.listForUser(user, pageable)
                .map(MatchDtos.AnalyzeResponse::from);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific match analysis")
    public ResponseEntity<MatchDtos.AnalyzeResponse> get(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(MatchDtos.AnalyzeResponse.from(service.getOwned(user, id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a match analysis")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
