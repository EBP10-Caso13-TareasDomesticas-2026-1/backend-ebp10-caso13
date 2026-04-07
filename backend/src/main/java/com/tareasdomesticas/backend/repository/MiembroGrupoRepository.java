package com.tareasdomesticas.backend.repository;

import com.tareasdomesticas.backend.entity.MiembroGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Long> {

    Optional<MiembroGrupo> findByUsuarioIdUsuario(Long idUsuario);
}