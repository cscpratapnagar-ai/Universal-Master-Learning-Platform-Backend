package com.masterlearning.platform.core.security;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RoleRepository extends JpaRepository<Role,UUID>{ Optional<Role> findByCode(String code); }