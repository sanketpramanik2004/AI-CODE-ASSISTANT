package com.aibugfinder.backend.repository;

import com.aibugfinder.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop10ByOrderByXpDescLevelDesc();

    @Query("""
            select count(user)
            from User user
            where user.xp > :xp
               or (user.xp = :xp and user.level > :level)
               or (user.xp = :xp and user.level = :level and user.id < :id)
            """)
    long countUsersAhead(@Param("xp") int xp, @Param("level") int level, @Param("id") Long id);
}
