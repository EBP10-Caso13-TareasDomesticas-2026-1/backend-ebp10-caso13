package com.tareasdomesticas.backend.repository;

import com.tareasdomesticas.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
