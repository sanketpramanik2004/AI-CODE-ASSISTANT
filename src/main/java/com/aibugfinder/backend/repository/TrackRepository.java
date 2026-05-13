package com.aibugfinder.backend.repository;

import com.aibugfinder.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findAllByOrderByDisplayOrderAscIdAsc();

    Optional<Track> findByNameIgnoreCase(String name);
}
