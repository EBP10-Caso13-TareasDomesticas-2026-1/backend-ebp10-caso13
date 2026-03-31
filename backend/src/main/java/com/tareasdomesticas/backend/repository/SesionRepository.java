package com.tareasdomesticas.backend.repository;

import com.tareasdomesticas.backend.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    Optional<Sesion> findByToken(String token);
}
