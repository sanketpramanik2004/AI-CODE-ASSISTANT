package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.aibugfinder.backend.dto.CodeRequest;
import com.aibugfinder.backend.dto.SubmissionResponse;
import com.aibugfinder.backend.dto.SubmitCodeRequest;
import com.aibugfinder.backend.dto.TestCaseResultResponse;
import com.aibugfinder.backend.entity.Problem;
import com.aibugfinder.backend.entity.Submission;
import com.aibugfinder.backend.entity.SubmissionStatus;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.entity.UserProblemProgress;
import com.aibugfinder.backend.exception.BadRequestException;
import com.aibugfinder.backend.exception.ResourceNotFoundException;
import com.aibugfinder.backend.repository.ProblemRepository;
import com.aibugfinder.backend.repository.SubmissionRepository;
import com.aibugfinder.backend.repository.UserProblemProgressRepository;
import com.aibugfinder.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionService {
    private final AuthenticatedUserService authenticatedUserService;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final UserProblemProgressRepository progressRepository;
    private final CodeEvaluationService codeEvaluationService;
    private final GamificationService gamificationService;
    private final AnalysisService analysisService;

    public SubmissionService(AuthenticatedUserService authenticatedUserService,
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            UserProblemProgressRepository progressRepository,
            CodeEvaluationService codeEvaluationService,
            GamificationService gamificationService,
            AnalysisService analysisService) {
        this.authenticatedUserService = authenticatedUserService;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.codeEvaluationService = codeEvaluationService;
        this.gamificationService = gamificationService;
        this.analysisService = analysisService;
    }

    @Transactional
    public SubmissionResponse submit(SubmitCodeRequest request) {
        validate(request);
        User user = authenticatedUserService.currentUser();
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found."));

        int attemptNumber = submissionRepository.countByUserIdAndProblemId(user.getId(), problem.getId()) + 1;
        EvaluationResult evaluation = codeEvaluationService.evaluate(problem, request.getCode(), request.getLanguage());
        SubmissionStatus status = evaluation.allPassed() ? SubmissionStatus.CORRECT : SubmissionStatus.WRONG;
        boolean alreadySolved = submissionRepository.existsByUserIdAndProblemIdAndStatus(
                user.getId(),
                problem.getId(),
                SubmissionStatus.CORRECT);

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setCode(request.getCode());
        submission.setStatus(status);
        submission.setAttemptNumber(attemptNumber);
        submissionRepository.save(submission);

        int xpGained = 0;
        if (status == SubmissionStatus.CORRECT && !alreadySolved) {
            xpGained = gamificationService.awardForSolvedProblem(user, problem.getDifficulty());
            userRepository.save(user);
            markSolved(user, problem);
        }

        AnalysisResponse feedback = buildAiFeedback(request, problem, status, attemptNumber);
        SubmissionResponse response = new SubmissionResponse();
        response.setStatus(status);
        response.setMessage(status == SubmissionStatus.CORRECT ? "All test cases passed." : "Some test cases failed.");
        response.setAttemptNumber(attemptNumber);
        response.setHintLevel(status == SubmissionStatus.WRONG ? Math.min(attemptNumber, 3) : 0);
        response.setHint(status == SubmissionStatus.WRONG ? hintFor(problem, attemptNumber) : "");
        response.setXpGained(xpGained);
        response.setTotalXp(user.getXp());
        response.setLevel(user.getLevel());
        response.setStreak(user.getStreak());
        response.setTestCases(toResponse(evaluation.testCases()));
        response.setAiFeedback(feedback);
        return response;
    }

    private void markSolved(User user, Problem problem) {
        UserProblemProgress progress = progressRepository.findByUserIdAndProblemId(user.getId(), problem.getId())
                .orElseGet(UserProblemProgress::new);
        progress.setUser(user);
        progress.setProblem(problem);
        progress.setSolved(true);
        progress.setSolvedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private AnalysisResponse buildAiFeedback(SubmitCodeRequest request, Problem problem, SubmissionStatus status, int attemptNumber) {
        CodeRequest codeRequest = new CodeRequest();
        codeRequest.setCode(request.getCode());
        codeRequest.setLanguage(request.getLanguage());
        AnalysisResponse feedback = analysisService.analyzeCode(codeRequest);

        if (status == SubmissionStatus.CORRECT) {
            feedback.setBugs(List.of("Submission accepted. Review the optimization notes for improvement ideas."));
            return feedback;
        }

        if ((feedback.getFixedCode() == null || feedback.getFixedCode().isBlank()
                || feedback.getFixedCode().equals(request.getCode())) && problem.getSolutionCode() != null) {
            feedback.setFixedCode(problem.getSolutionCode());
        }

        return feedback;
    }

    private String hintFor(Problem problem, int attemptNumber) {
        return switch (Math.min(attemptNumber, 3)) {
            case 1 -> "Think about the boundary cases before changing the whole approach.";
            case 2 -> "Trace the code against the first failing test and check how indexes or state change step by step.";
            default -> "Compare your solution with this expected approach: " + problem.getSolutionCode();
        };
    }

    private List<TestCaseResultResponse> toResponse(List<TestCaseExecutionResult> testCases) {
        return testCases.stream()
                .map(testCase -> new TestCaseResultResponse(
                        testCase.input(),
                        testCase.expectedOutput(),
                        testCase.actualOutput(),
                        testCase.passed()))
                .toList();
    }

    private void validate(SubmitCodeRequest request) {
        if (request == null || request.getProblemId() == null || request.getCode() == null || request.getCode().isBlank()) {
            throw new BadRequestException("Problem id and code are required.");
        }
    }
}
