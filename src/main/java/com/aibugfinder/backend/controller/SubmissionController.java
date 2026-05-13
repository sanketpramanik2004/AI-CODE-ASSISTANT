package com.aibugfinder.backend.controller;

import com.aibugfinder.backend.dto.SubmissionResponse;
import com.aibugfinder.backend.dto.SubmitCodeRequest;
import com.aibugfinder.backend.service.SubmissionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/submit")
    public SubmissionResponse submit(@RequestBody SubmitCodeRequest request) {
        return submissionService.submit(request);
    }
}
