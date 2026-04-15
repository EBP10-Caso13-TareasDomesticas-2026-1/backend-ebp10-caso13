package com.tareasdomesticas.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareasdomesticas.backend.entity.MiembroGrupo;

public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Long> {

    Optional<MiembroGrupo> findByUsuarioIdUsuario(Long idUsuario);

    Optional<MiembroGrupo> findByUsuarioIdUsuarioAndGrupoIdGrupo(Long idUsuario, Long idGrupo);
}