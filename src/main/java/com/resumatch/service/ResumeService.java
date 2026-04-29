package com.resumatch.service;

import com.resumatch.entity.Resume;
import com.resumatch.entity.User;
import com.resumatch.exception.BadRequestException;
import com.resumatch.exception.ResourceNotFoundException;
import com.resumatch.repository.ResumeRepository;
import com.resumatch.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PdfTextExtractor pdfTextExtractor;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public Resume upload(User user, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are supported");
        }
        if (contentType != null && !contentType.equals("application/pdf")) {
            throw new BadRequestException("File must be a PDF (application/pdf)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must be under 5 MB");
        }

        // Extract text first — if parsing fails, we don't want to save the file
        String extractedText;
        try (var in = file.getInputStream()) {
            extractedText = pdfTextExtractor.extract(in);
        } catch (IOException e) {
            log.error("Failed to extract PDF text", e);
            throw new BadRequestException("Could not read the PDF file. Is it corrupted?");
        }

        if (extractedText == null || extractedText.trim().length() < 50) {
            throw new BadRequestException(
                "Extracted text is too short — the PDF may be scanned images. " +
                "Please upload a text-based PDF (not a scanned document)."
            );
        }

        // Save file to disk — use absolute path + Files.copy (more reliable than transferTo)
        String storedName = UUID.randomUUID() + ".pdf";
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = uploadPath.resolve(storedName);

        try {
            Files.createDirectories(uploadPath);
            log.info("Saving file to: {}", target);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("File saved successfully: {}", target);
        } catch (IOException e) {
            log.error("Failed to save file to {}", target, e);
            throw new RuntimeException("Could not save uploaded file: " + e.getMessage(), e);
        }

        Resume resume = Resume.builder()
                .user(user)
                .originalFilename(originalName)
                .storedFilename(storedName)
                .fileSize(file.getSize())
                .extractedText(extractedText)
                .build();

        return resumeRepository.save(resume);
    }

    public Page<Resume> listForUser(User user, Pageable pageable) {
        return resumeRepository.findByUser(user, pageable);
    }

    public Resume getOwnedResume(User user, Long id) {
        return resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
    }

    @Transactional
    public void delete(User user, Long id) {
        Resume resume = getOwnedResume(user, id);

        // Best-effort delete from disk
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.deleteIfExists(uploadPath.resolve(resume.getStoredFilename()));
        } catch (IOException e) {
            log.warn("Could not delete file from disk: {}", resume.getStoredFilename(), e);
        }

        resumeRepository.delete(resume);
    }
}
