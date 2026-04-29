package com.resumatch.controller;

import com.resumatch.dto.ResumeResponse;
import com.resumatch.entity.Resume;
import com.resumatch.entity.User;
import com.resumatch.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Resumes", description = "Upload and manage resume PDFs")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload a resume PDF",
               description = "Extracts text from the PDF automatically. Max 5 MB.")
    public ResponseEntity<ResumeResponse> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file
    ) {
        Resume resume = resumeService.upload(user, file);
        return ResponseEntity.ok(ResumeResponse.from(resume));
    }

    @GetMapping
    @Operation(summary = "List your uploaded resumes",
               description = "Paginated. Use ?page=0&size=10&sort=uploadedAt,desc")
    public ResponseEntity<Page<ResumeResponse>> list(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "uploadedAt") Pageable pageable
    ) {
        Page<ResumeResponse> page = resumeService.listForUser(user, pageable).map(ResumeResponse::from);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get metadata for a specific resume")
    public ResponseEntity<ResumeResponse> get(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ResumeResponse.from(resumeService.getOwnedResume(user, id)));
    }

    @GetMapping("/{id}/text")
    @Operation(summary = "Get the extracted plain text of a resume")
    public ResponseEntity<Map<String, String>> getText(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        Resume resume = resumeService.getOwnedResume(user, id);
        return ResponseEntity.ok(Map.of(
                "id", String.valueOf(resume.getId()),
                "filename", resume.getOriginalFilename(),
                "text", resume.getExtractedText()
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        resumeService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
