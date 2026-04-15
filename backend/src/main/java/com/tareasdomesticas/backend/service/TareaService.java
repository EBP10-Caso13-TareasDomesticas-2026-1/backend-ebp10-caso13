package com.tareasdomesticas.backend.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.PrioridadTarea;
import com.tareasdomesticas.backend.entity.Tarea;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import com.tareasdomesticas.backend.repository.TareaRepository;

@Service
public class TareaService {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final TareaRepository tareaRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final SesionService sesionService;

    public TareaService(TareaRepository tareaRepository,
                        MiembroGrupoRepository miembroGrupoRepository,
                        SesionService sesionService) {
        this.tareaRepository = tareaRepository;
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.sesionService = sesionService;
    }

    @Transactional
    public CrearTareaResponse crearTarea(String authorizationHeader, CrearTareaRequest request) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembroAdministrador = miembroGrupoRepository.findByUsuarioIdUsuario(usuarioAutenticado.getIdUsuario())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "El usuario no pertenece a ningun grupo"));

        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(miembroAdministrador.getRol().getNombre())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede crear tareas");
        }

        Grupo grupo = miembroAdministrador.getGrupo();

        MiembroGrupo miembroAsignado = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(request.getIdUsuarioAsignado(), grupo.getIdGrupo())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "El usuario asignado no pertenece al grupo del administrador"));

        if (!request.getFechaLimite().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La fecha y hora limite deben ser posteriores al momento actual");
        }

        Tarea tarea = new Tarea();
        tarea.setGrupo(grupo);
        tarea.setUsuarioAsignado(miembroAsignado.getUsuario());
        tarea.setNombre(request.getNombre().trim());
        tarea.setDescripcion(normalizarDescripcion(request.getDescripcion()));
        tarea.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : PrioridadTarea.MEDIA);
        tarea.setEstado(EstadoTarea.PENDIENTE);
        tarea.setFechaLimite(request.getFechaLimite());
        tarea.setFechaCreacion(LocalDateTime.now());

        Tarea tareaGuardada = tareaRepository.save(tarea);

        return new CrearTareaResponse(
                tareaGuardada.getIdTarea(),
                tareaGuardada.getNombre(),
                tareaGuardada.getDescripcion(),
                tareaGuardada.getPrioridad(),
                tareaGuardada.getEstado(),
                tareaGuardada.getFechaLimite(),
                tareaGuardada.getFechaCreacion(),
                tareaGuardada.getGrupo().getIdGrupo(),
                tareaGuardada.getUsuarioAsignado().getIdUsuario(),
                "Tarea creada correctamente"
        );
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null) {
            return null;
        }

        String descripcionNormalizada = descripcion.trim();
        return descripcionNormalizada.isEmpty() ? null : descripcionNormalizada;
    }
}
