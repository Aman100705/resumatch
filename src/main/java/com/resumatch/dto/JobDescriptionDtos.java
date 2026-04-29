package com.resumatch.dto;

import com.resumatch.entity.JobDescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public class JobDescriptionDtos {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;

        @Size(max = 200)
        private String company;

        @NotBlank(message = "Content is required")
        @Size(min = 50, max = 20000, message = "Content must be 50–20,000 characters")
        private String content;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String company;
        private String content;
        private Instant createdAt;

        public static Response from(JobDescription jd) {
            return Response.builder()
                    .id(jd.getId())
                    .title(jd.getTitle())
                    .company(jd.getCompany())
                    .content(jd.getContent())
                    .createdAt(jd.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Summary {
        private Long id;
        private String title;
        private String company;
        private Instant createdAt;

        public static Summary from(JobDescription jd) {
            return Summary.builder()
                    .id(jd.getId())
                    .title(jd.getTitle())
                    .company(jd.getCompany())
                    .createdAt(jd.getCreatedAt())
                    .build();
        }
    }
}
