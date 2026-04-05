package com.aibugfinder.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadedCodeExtractorTest {

    private final UploadedCodeExtractor extractor = new UploadedCodeExtractor();

    @Test
    void extractsSingleSourceFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "BinarySearch.java",
                "text/plain",
                "public class BinarySearch {}".getBytes(StandardCharsets.UTF_8));

        UploadedCodeExtractor.ExtractedCode extractedCode = extractor.extract(file, null);

        assertEquals("java", extractedCode.language());
        assertTrue(extractedCode.code().contains("BinarySearch"));
        assertTrue(extractedCode.description().contains("uploaded file"));
    }

    @Test
    void extractsSupportedFilesFromZipArchive() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "repo.zip",
                "application/zip",
                buildZip(
                        "src/Main.java", "class Main {}",
                        "README.md", "# notes",
                        "src/util.py", "print('ok')"));

        UploadedCodeExtractor.ExtractedCode extractedCode = extractor.extract(file, null);

        assertTrue(extractedCode.code().contains("File: src/Main.java"));
        assertTrue(extractedCode.code().contains("File: src/util.py"));
        assertTrue(extractedCode.description().contains("repository archive"));
    }

    private byte[] buildZip(String... nameContentPairs) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            for (int index = 0; index < nameContentPairs.length; index += 2) {
                zipOutputStream.putNextEntry(new ZipEntry(nameContentPairs[index]));
                zipOutputStream.write(nameContentPairs[index + 1].getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        }
        return outputStream.toByteArray();
    }
}
