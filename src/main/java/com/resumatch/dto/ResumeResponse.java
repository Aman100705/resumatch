package com.resumatch.dto;

import com.resumatch.entity.Resume;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private Integer extractedCharCount;
    private Instant uploadedAt;

    public static ResumeResponse from(Resume r) {
        return ResumeResponse.builder()
                .id(r.getId())
                .originalFilename(r.getOriginalFilename())
                .fileSize(r.getFileSize())
                .extractedCharCount(r.getExtractedText() == null ? 0 : r.getExtractedText().length())
                .uploadedAt(r.getUploadedAt())
                .build();
    }
}
