package com.tareasdomesticas.backend.repository;

import com.tareasdomesticas.backend.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByCodigoInvitacion(String codigoInvitacion);
}