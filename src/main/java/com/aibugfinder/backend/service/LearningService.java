package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.ProblemDetailResponse;
import com.aibugfinder.backend.dto.ProblemListResponse;
import com.aibugfinder.backend.dto.TrackResponse;
import com.aibugfinder.backend.entity.Problem;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.exception.ResourceNotFoundException;
import com.aibugfinder.backend.repository.ProblemRepository;
import com.aibugfinder.backend.repository.SubmissionRepository;
import com.aibugfinder.backend.repository.TrackRepository;
import com.aibugfinder.backend.repository.UserProblemProgressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LearningService {
    private final TrackRepository trackRepository;
    private final ProblemRepository problemRepository;
    private final UserProblemProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public LearningService(TrackRepository trackRepository,
            ProblemRepository problemRepository,
            UserProblemProgressRepository progressRepository,
            SubmissionRepository submissionRepository,
            AuthenticatedUserService authenticatedUserService) {
        this.trackRepository = trackRepository;
        this.problemRepository = problemRepository;
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<TrackResponse> tracks() {
        Long userId = currentUserIdOrNull();
        return trackRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(track -> TrackResponse.from(
                        track,
                        userId == null ? 0 : progressRepository.countByUserIdAndProblemTrackIdAndSolvedTrue(userId, track.getId()),
                        problemRepository.countByTrackId(track.getId())))
                .toList();
    }

    public List<ProblemListResponse> problems(Long trackId) {
        List<Problem> problems = trackId == null
                ? problemRepository.findAllByOrderByTrackDisplayOrderAscDisplayOrderAscIdAsc()
                : problemRepository.findByTrackIdOrderByDisplayOrderAscIdAsc(trackId);
        Long userId = currentUserIdOrNull();
        return problems.stream()
                .map(problem -> ProblemListResponse.from(problem, isSolved(userId, problem.getId())))
                .toList();
    }

    public ProblemDetailResponse problem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found."));
        return ProblemDetailResponse.from(problem, isSolved(currentUserIdOrNull(), problem.getId()));
    }

    private boolean isSolved(Long userId, Long problemId) {
        return userId != null && submissionRepository.existsByUserIdAndProblemIdAndStatus(
                userId,
                problemId,
                com.aibugfinder.backend.entity.SubmissionStatus.CORRECT);
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        try {
            User user = authenticatedUserService.currentUser();
            return user.getId();
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
