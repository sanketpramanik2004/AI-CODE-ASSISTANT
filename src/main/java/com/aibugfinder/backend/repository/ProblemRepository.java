package com.aibugfinder.backend.repository;

import com.aibugfinder.backend.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByTrackId(Long trackId);

    List<Problem> findByTrackIdOrderByDisplayOrderAscIdAsc(Long trackId);

    List<Problem> findAllByOrderByTrackDisplayOrderAscDisplayOrderAscIdAsc();

    Optional<Problem> findByTrackIdAndTitleIgnoreCase(Long trackId, String title);

    long countByTrackId(Long trackId);
}
