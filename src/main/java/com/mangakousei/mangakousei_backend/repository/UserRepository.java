package com.mangakousei.mangakousei_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mangakousei.mangakousei_backend.entity.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);


    @Query("""
        SELECT u FROM User u
        JOIN u.roles r
        WHERE r.roleName = :roleName
        AND (
            LOWER(u.fullName) LIKE %:keyword%
            OR LOWER(u.email)  LIKE %:keyword%
        )
        ORDER BY u.fullName ASC
    """)
    List<User> findByRoleNameAndKeyword(
            @Param("roleName") String roleName,
            @Param("keyword") String keyword);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findAllByRoleName(@Param("roleName") String roleName);
}