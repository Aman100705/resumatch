package com.resumatch.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class PdfTextExtractor {

    /**
     * Extracts plain text from a PDF input stream.
     * @throws IOException if the PDF is malformed or unreadable
     */
    public String extract(InputStream pdfStream) throws IOException {
        byte[] bytes = pdfStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.debug("Extracted {} chars from PDF", text.length());
            return text;
        }
    }
}
