package com.aibugfinder.backend.repository;

import com.aibugfinder.backend.entity.UserProblemProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProblemProgressRepository extends JpaRepository<UserProblemProgress, Long> {
    Optional<UserProblemProgress> findByUserIdAndProblemId(Long userId, Long problemId);

    long countByUserIdAndProblemTrackIdAndSolvedTrue(Long userId, Long trackId);
}
