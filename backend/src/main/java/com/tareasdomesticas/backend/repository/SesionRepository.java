package com.tareasdomesticas.backend.repository;

import com.tareasdomesticas.backend.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
}
