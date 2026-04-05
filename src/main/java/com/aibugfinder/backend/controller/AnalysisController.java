package com.aibugfinder.backend.controller;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.aibugfinder.backend.dto.CodeRequest;
import com.aibugfinder.backend.service.AnalysisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public AnalysisResponse analyze(@RequestBody CodeRequest request) {
        return service.analyzeCode(request);
    }

    @PostMapping(value = "/analyze/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeUpload(@RequestPart("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) {
        return service.analyzeUploadedFile(file, language);
    }
}
