package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.aibugfinder.backend.dto.SubmissionResponse;
import com.aibugfinder.backend.dto.SubmitCodeRequest;
import com.aibugfinder.backend.entity.Difficulty;
import com.aibugfinder.backend.entity.Problem;
import com.aibugfinder.backend.entity.SubmissionStatus;
import com.aibugfinder.backend.entity.Track;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.repository.ProblemRepository;
import com.aibugfinder.backend.repository.SubmissionRepository;
import com.aibugfinder.backend.repository.UserProblemProgressRepository;
import com.aibugfinder.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubmissionServiceTest {

    @Test
    void correctEasySubmissionAwardsXpAndUpdatesLevel() {
        TestFixture fixture = new TestFixture();
        when(fixture.evaluationService.evaluate(any(), any(), any())).thenReturn(new EvaluationResult(
                true,
                List.of(new TestCaseExecutionResult("input", "output", "output", true))));

        SubmissionResponse response = fixture.service.submit(request());

        assertEquals(SubmissionStatus.CORRECT, response.getStatus());
        assertEquals(10, response.getXpGained());
        assertEquals(10, response.getTotalXp());
        assertEquals(1, response.getLevel());
        assertEquals(1, response.getStreak());
    }

    @Test
    void wrongAttemptsEscalateHintsAndShowSuggestedCode() {
        TestFixture fixture = new TestFixture();
        when(fixture.submissionRepository.countByUserIdAndProblemId(1L, 10L)).thenReturn(1);
        when(fixture.evaluationService.evaluate(any(), any(), any())).thenReturn(new EvaluationResult(
                false,
                List.of(new TestCaseExecutionResult("input", "expected", "actual", false))));

        SubmissionResponse response = fixture.service.submit(request());

        assertEquals(SubmissionStatus.WRONG, response.getStatus());
        assertEquals(2, response.getHintLevel());
        assertTrue(response.getHint().contains("Trace"));
        assertEquals("return 0;", response.getAiFeedback().getFixedCode());
    }

    private SubmitCodeRequest request() {
        SubmitCodeRequest request = new SubmitCodeRequest();
        request.setProblemId(10L);
        request.setLanguage("java");
        request.setCode("class Solution { int search() { return 0; } }");
        return request;
    }

    private static class TestFixture {
        private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
        private final CodeEvaluationService evaluationService = mock(CodeEvaluationService.class);
        private final SubmissionService service;

        TestFixture() {
            AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
            ProblemRepository problemRepository = mock(ProblemRepository.class);
            UserRepository userRepository = mock(UserRepository.class);
            UserProblemProgressRepository progressRepository = mock(UserProblemProgressRepository.class);
            AnalysisService analysisService = mock(AnalysisService.class);

            User user = new User();
            user.setId(1L);
            user.setName("Sanket");
            user.setEmail("sanket@example.com");
            when(authenticatedUserService.currentUser()).thenReturn(user);

            Track track = new Track();
            track.setId(3L);
            track.setName("Arrays");

            Problem problem = new Problem();
            problem.setId(10L);
            problem.setTrack(track);
            problem.setDifficulty(Difficulty.EASY);
            problem.setStarterCode("return -1;");
            problem.setSolutionCode("return 0;");
            when(problemRepository.findById(10L)).thenReturn(Optional.of(problem));

            AnalysisResponse feedback = new AnalysisResponse();
            feedback.setBugs(List.of("Boundary issue."));
            feedback.setFixedCode("return 0;");
            feedback.setExplanation("Fix the boundary.");
            feedback.setEdgeCasesToTest(List.of("single item"));
            feedback.setTimeComplexity("O(1)");
            feedback.setSpaceComplexity("O(1)");
            feedback.setOptimality("Optimal.");
            feedback.setLearningResources(List.of("https://example.com"));
            when(analysisService.analyzeCode(any())).thenReturn(feedback);

            when(submissionRepository.countByUserIdAndProblemId(1L, 10L)).thenReturn(0);
            when(submissionRepository.existsByUserIdAndProblemIdAndStatus(1L, 10L, SubmissionStatus.CORRECT))
                    .thenReturn(false);
            when(progressRepository.findByUserIdAndProblemId(1L, 10L)).thenReturn(Optional.empty());

            service = new SubmissionService(
                    authenticatedUserService,
                    problemRepository,
                    submissionRepository,
                    userRepository,
                    progressRepository,
                    evaluationService,
                    new GamificationService(),
                    analysisService);
        }
    }
}
