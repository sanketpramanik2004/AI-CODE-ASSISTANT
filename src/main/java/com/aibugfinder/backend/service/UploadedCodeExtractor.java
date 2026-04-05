package com.aibugfinder.backend.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class UploadedCodeExtractor {

    private static final int MAX_REPO_FILES = 12;
    private static final int MAX_TOTAL_CHARACTERS = 40000;
    private static final int MAX_FILE_CHARACTERS = 8000;

    private static final Map<String, String> LANGUAGE_BY_EXTENSION = new LinkedHashMap<>();

    static {
        LANGUAGE_BY_EXTENSION.put("java", "java");
        LANGUAGE_BY_EXTENSION.put("py", "python");
        LANGUAGE_BY_EXTENSION.put("js", "javascript");
        LANGUAGE_BY_EXTENSION.put("ts", "typescript");
        LANGUAGE_BY_EXTENSION.put("jsx", "javascript");
        LANGUAGE_BY_EXTENSION.put("tsx", "typescript");
        LANGUAGE_BY_EXTENSION.put("cpp", "cpp");
        LANGUAGE_BY_EXTENSION.put("cc", "cpp");
        LANGUAGE_BY_EXTENSION.put("c", "c");
        LANGUAGE_BY_EXTENSION.put("cs", "csharp");
        LANGUAGE_BY_EXTENSION.put("go", "go");
        LANGUAGE_BY_EXTENSION.put("kt", "kotlin");
        LANGUAGE_BY_EXTENSION.put("rs", "rust");
        LANGUAGE_BY_EXTENSION.put("rb", "ruby");
        LANGUAGE_BY_EXTENSION.put("php", "php");
        LANGUAGE_BY_EXTENSION.put("swift", "swift");
        LANGUAGE_BY_EXTENSION.put("scala", "scala");
        LANGUAGE_BY_EXTENSION.put("sql", "sql");
        LANGUAGE_BY_EXTENSION.put("html", "html");
        LANGUAGE_BY_EXTENSION.put("css", "css");
        LANGUAGE_BY_EXTENSION.put("xml", "xml");
        LANGUAGE_BY_EXTENSION.put("json", "json");
        LANGUAGE_BY_EXTENSION.put("yml", "yaml");
        LANGUAGE_BY_EXTENSION.put("yaml", "yaml");
        LANGUAGE_BY_EXTENSION.put("md", "markdown");
    }

    public ExtractedCode extract(MultipartFile file, String requestedLanguage) throws IOException {
        String filename = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String normalizedLanguage = normalizeLanguage(requestedLanguage);

        if (isZipFile(filename, file.getContentType())) {
            return extractZipArchive(file, normalizedLanguage, filename);
        }

        String contents = readText(file.getInputStream());
        if (contents.isBlank()) {
            throw new IllegalArgumentException("The uploaded source file is empty.");
        }

        String detectedLanguage = normalizedLanguage != null ? normalizedLanguage : detectLanguage(filename);
        String trimmedContents = trimToLimit(contents, MAX_TOTAL_CHARACTERS);
        String description = "uploaded file '" + filename + "'";
        return new ExtractedCode(trimmedContents, detectedLanguage, description);
    }

    private ExtractedCode extractZipArchive(MultipartFile file, String requestedLanguage, String filename) throws IOException {
        StringBuilder builder = new StringBuilder();
        int includedFiles = 0;
        String detectedLanguage = requestedLanguage;

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory() || !isSupportedSourceFile(entry.getName())) {
                    continue;
                }

                String entryContents = readText(zipInputStream);
                if (entryContents.isBlank()) {
                    continue;
                }

                if (detectedLanguage == null) {
                    detectedLanguage = detectLanguage(entry.getName());
                }

                includedFiles++;
                builder.append("File: ").append(entry.getName()).append(System.lineSeparator());
                builder.append(trimToLimit(entryContents, MAX_FILE_CHARACTERS)).append(System.lineSeparator()).append(System.lineSeparator());

                if (includedFiles >= MAX_REPO_FILES || builder.length() >= MAX_TOTAL_CHARACTERS) {
                    break;
                }
            }
        }

        if (builder.length() == 0) {
            throw new IllegalArgumentException("The uploaded zip archive did not contain any supported source files.");
        }

        String description = "repository archive '" + filename + "' with " + includedFiles + " source file(s)";
        return new ExtractedCode(trimToLimit(builder.toString(), MAX_TOTAL_CHARACTERS), detectedLanguage, description);
    }

    private boolean isZipFile(String filename, String contentType) {
        return filename.toLowerCase().endsWith(".zip")
                || "application/zip".equalsIgnoreCase(contentType)
                || "application/x-zip-compressed".equalsIgnoreCase(contentType);
    }

    private boolean isSupportedSourceFile(String name) {
        return detectLanguage(name) != null;
    }

    private String detectLanguage(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return null;
        }

        return LANGUAGE_BY_EXTENSION.get(name.substring(dotIndex + 1).toLowerCase());
    }

    private String normalizeLanguage(String language) {
        return language == null || language.isBlank() ? null : language.trim();
    }

    private String readText(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private String trimToLimit(String text, int limit) {
        if (text.length() <= limit) {
            return text;
        }

        return text.substring(0, limit) + System.lineSeparator() + "// ... truncated for analysis";
    }

    public record ExtractedCode(String code, String language, String description) {
    }
}
