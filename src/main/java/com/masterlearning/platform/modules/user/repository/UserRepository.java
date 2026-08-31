package com.masterlearning.platform.modules.user.repository;

import com.masterlearning.platform.modules.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            """)
    List<User> findAllWithRoles();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithAuthoritiesById(UUID id);

    boolean existsByEmailIgnoreCase(String email);
}
