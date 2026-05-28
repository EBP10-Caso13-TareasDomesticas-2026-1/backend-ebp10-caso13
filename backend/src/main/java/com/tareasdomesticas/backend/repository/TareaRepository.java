package com.tareasdomesticas.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByEstadoInAndFechaLimiteBefore(List<EstadoTarea> estados, LocalDateTime fechaLimite);
    List<Tarea> findByEstadoInAndFechaLimiteBeforeAndEliminadoFalse(List<EstadoTarea> estados, LocalDateTime fechaLimite);
    List<Tarea> findByGrupoIdGrupo(Long idGrupo);
    List<Tarea> findByGrupoIdGrupoAndEliminadoFalse(Long idGrupo);
    Optional<Tarea> findByIdTareaAndEliminadoFalse(Long idTarea);

    List<Tarea> findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstadoIn(
            Long idGrupo, Long idUsuario, List<EstadoTarea> estados);
    List<Tarea> findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstadoInAndEliminadoFalse(
            Long idGrupo, Long idUsuario, List<EstadoTarea> estados);

    List<Tarea> findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(
            Long idGrupo, Long idUsuario, EstadoTarea estado);

    List<Tarea> findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
            Long idGrupo, Long idUsuario, boolean exMiembro);
}
