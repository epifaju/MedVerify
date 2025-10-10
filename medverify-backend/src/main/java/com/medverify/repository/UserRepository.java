package com.medverify.repository;

import com.medverify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité User
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Trouve un utilisateur par email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Compte les utilisateurs actifs dans une période
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true " +
            "AND u.lastLoginAt BETWEEN :start AND :end")
    Long countActiveUsers(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Compte les nouveaux utilisateurs dans une période
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    Long countNewUsers(@Param("start") Instant start, @Param("end") Instant end);
}

