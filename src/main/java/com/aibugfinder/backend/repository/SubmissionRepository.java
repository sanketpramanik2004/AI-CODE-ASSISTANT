package com.aibugfinder.backend.repository;

import com.aibugfinder.backend.entity.Submission;
import com.aibugfinder.backend.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    int countByUserIdAndProblemId(Long userId, Long problemId);

    boolean existsByUserIdAndProblemIdAndStatus(Long userId, Long problemId, SubmissionStatus status);

    List<Submission> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
