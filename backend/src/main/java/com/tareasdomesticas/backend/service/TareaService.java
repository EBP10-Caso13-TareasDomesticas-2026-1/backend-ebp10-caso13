package com.tareasdomesticas.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tareasdomesticas.backend.dto.CambiarEstadoTareaRequest;
import com.tareasdomesticas.backend.dto.CambiarEstadoTareaResponse;
import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.dto.DetalleTareaResponse;
import com.tareasdomesticas.backend.dto.EditarTareaRequest;
import com.tareasdomesticas.backend.dto.EditarTareaResponse;
import com.tareasdomesticas.backend.dto.EliminarTareaResponse;
import com.tareasdomesticas.backend.dto.FiltrarTareasResponse;
import com.tareasdomesticas.backend.dto.TareaTableroResponse;
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

    // ===============================
    // 🧩 TABLERO (ESCENARIOS 1,2,3,4)
    // ===============================
    @Transactional(readOnly = true)
    public Map<EstadoTarea, List<TareaTableroResponse>> obtenerTablero(String authorizationHeader) {

        Usuario usuario = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembro = miembroGrupoRepository
                .findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No pertenece a un grupo"));

        List<Tarea> tareas = tareaRepository.findByGrupoIdGrupoAndEliminadoFalse(
                miembro.getGrupo().getIdGrupo()
        );

        return tareas.stream()
                .sorted(Comparator.comparing(Tarea::getFechaLimite))
                .map(t -> new TareaTableroResponse(
                        t.getIdTarea(),
                        t.getNombre(),
                        t.getUsuarioAsignado().getNombre(),
                        t.getEstado(),
                        t.getFechaLimite(),
                        t.getEstado() == EstadoTarea.VENCIDA
                ))
                .collect(Collectors.groupingBy(TareaTableroResponse::getEstado));
    }

    // ===============================
    // 🧩 NUEVO: TABLERO POR ID DE GRUPO (COMPATIBILIDAD FRONT)
    // ===============================
    @Transactional(readOnly = true)
    public Map<EstadoTarea, List<TareaTableroResponse>> obtenerTableroPorGrupo(
            String authorizationHeader, Long idGrupo) {

        Usuario usuario = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembro = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuario.getIdUsuario(), idGrupo)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.FORBIDDEN,
                        "No pertenece a ese grupo"
                ));

        List<Tarea> tareas = tareaRepository.findByGrupoIdGrupoAndEliminadoFalse(idGrupo);

        return tareas.stream()
                .sorted(Comparator.comparing(Tarea::getFechaLimite))
                .map(t -> new TareaTableroResponse(
                        t.getIdTarea(),
                        t.getNombre(),
                        t.getUsuarioAsignado().getNombre(),
                        t.getEstado(),
                        t.getFechaLimite(),
                        t.getEstado() == EstadoTarea.VENCIDA
                ))
                .collect(Collectors.groupingBy(TareaTableroResponse::getEstado));
    }

    // ===============================
    // 🧩 FILTRAR TAREAS (HU-011)
    // ===============================
    @Transactional(readOnly = true)
    public FiltrarTareasResponse filtrarTareas(Long idGrupo, List<String> estados,
            List<String> prioridades, List<Long> idsMiembros, String authorizationHeader) {

        Usuario usuario = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        if (miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuario.getIdUsuario(), idGrupo)
                .isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No pertenece a ese grupo");
        }

        List<EstadoTarea> estadosEnum = convertirEstados(estados);
        List<PrioridadTarea> prioridadesEnum = convertirPrioridades(prioridades);

        List<Tarea> tareas = tareaRepository.findByGrupoIdGrupoAndEliminadoFalse(idGrupo).stream()
                .filter(t -> estadosEnum == null || estadosEnum.contains(t.getEstado()))
                .filter(t -> prioridadesEnum == null || prioridadesEnum.contains(t.getPrioridad()))
                .filter(t -> idsMiembros == null || idsMiembros.isEmpty()
                        || idsMiembros.contains(t.getUsuarioAsignado().getIdUsuario()))
                .toList();

        List<DetalleTareaResponse> items = tareas.stream()
                .map(t -> new DetalleTareaResponse(
                        t.getIdTarea(),
                        t.getNombre(),
                        t.getDescripcion(),
                        t.getPrioridad(),
                        t.getEstado(),
                        t.getFechaLimite(),
                        t.getGrupo().getIdGrupo(),
                        t.getUsuarioAsignado().getIdUsuario(),
                        t.getUsuarioAsignado().getNombre(),
                        t.isExMiembro()))
                .toList();

        String mensaje = tareas.isEmpty()
                ? "No hay tareas que coincidan con los filtros seleccionados"
                : tareas.size() + " tarea(s) encontrada(s)";

        List<String> estadosAplicados = estadosEnum == null ? null
                : estadosEnum.stream().map(Enum::name).toList();
        List<String> prioridadesAplicadas = prioridadesEnum == null ? null
                : prioridadesEnum.stream().map(Enum::name).toList();

        FiltrarTareasResponse.FiltrosAplicados filtros =
                new FiltrarTareasResponse.FiltrosAplicados(estadosAplicados, prioridadesAplicadas, idsMiembros);

        return new FiltrarTareasResponse(mensaje, filtros, items);
    }

    private List<EstadoTarea> convertirEstados(List<String> valores) {
        if (valores == null || valores.isEmpty()) return null;
        return valores.stream()
                .map(s -> {
                    try { return EstadoTarea.fromString(s); }
                    catch (Exception e) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Estado invalido: " + s);
                    }
                })
                .toList();
    }

    private List<PrioridadTarea> convertirPrioridades(List<String> valores) {
        if (valores == null || valores.isEmpty()) return null;
        return valores.stream()
                .map(s -> {
                    try { return PrioridadTarea.valueOf(s.trim().toUpperCase()); }
                    catch (Exception e) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Prioridad invalida: " + s);
                    }
                })
                .toList();
    }

    // ===============================
    // 🧩 CREAR TAREA
    // ===============================
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
        tarea.setFechaCambioEstado(null);

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

            // ===============================
            // 🧩 DETALLE DE TAREA
            // ===============================
            @Transactional(readOnly = true)
            public DetalleTareaResponse obtenerDetalleTarea(String authorizationHeader, Long idTarea) {
            Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

            Tarea tarea = tareaRepository.findByIdTareaAndEliminadoFalse(idTarea)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

            miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), tarea.getGrupo().getIdGrupo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tiene permiso para consultar esta tarea"));

            return new DetalleTareaResponse(
                tarea.getIdTarea(),
                tarea.getNombre(),
                tarea.getDescripcion(),
                tarea.getPrioridad(),
                tarea.getEstado(),
                tarea.getFechaLimite(),
                tarea.getGrupo().getIdGrupo(),
                tarea.getUsuarioAsignado().getIdUsuario(),
                tarea.getUsuarioAsignado().getNombre(),
                tarea.isExMiembro()
            );
            }

    // ===============================
    // 🧩 EDITAR TAREA
    // ===============================
    @Transactional
    public EditarTareaResponse editarTarea(String authorizationHeader, Long idTarea, EditarTareaRequest request) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        validarTareaNoEliminada(tarea);
        evaluarVencimiento(tarea);

        MiembroGrupo miembroAutenticado = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), tarea.getGrupo().getIdGrupo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "El usuario no pertenece al grupo de la tarea"));

        boolean esAdministrador = ROL_ADMINISTRADOR.equalsIgnoreCase(miembroAutenticado.getRol().getNombre());
        if (!esAdministrador) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede editar tareas");
        }

        if (tarea.getEstado() == EstadoTarea.COMPLETADA) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No se puede editar una tarea completada");
        }

        boolean permiteFecha = tarea.getEstado() == EstadoTarea.PENDIENTE || tarea.getEstado() == EstadoTarea.EN_PROGRESO;

        if (!permiteFecha && request.getFechaLimite() != null
                && !request.getFechaLimite().equals(tarea.getFechaLimite())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha limite no puede modificarse en una tarea vencida");
        }

        if (permiteFecha && request.getFechaLimite() != null && !request.getFechaLimite().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La fecha y hora limite deben ser posteriores al momento actual");
        }

        tarea.setNombre(request.getNombre().trim());
        tarea.setDescripcion(normalizarDescripcion(request.getDescripcion()));
        if (request.getPrioridad() != null) {
            tarea.setPrioridad(request.getPrioridad());
        }
        if (permiteFecha && request.getFechaLimite() != null) {
            tarea.setFechaLimite(request.getFechaLimite());
        }

        Tarea tareaGuardada = tareaRepository.save(tarea);

        return new EditarTareaResponse(
                tareaGuardada.getIdTarea(),
                tareaGuardada.getNombre(),
                tareaGuardada.getDescripcion(),
                tareaGuardada.getPrioridad(),
                tareaGuardada.getEstado(),
                tareaGuardada.getFechaLimite(),
                tareaGuardada.getFechaCambioEstado(),
                tareaGuardada.getGrupo().getIdGrupo(),
                tareaGuardada.getUsuarioAsignado().getIdUsuario(),
                "Tarea actualizada correctamente"
        );
    }

    // ===============================
    // 🧩 CAMBIAR ESTADO
    // ===============================
    @Transactional
    public CambiarEstadoTareaResponse cambiarEstado(String authorizationHeader, Long idTarea, CambiarEstadoTareaRequest request) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        validarTareaNoEliminada(tarea);
        evaluarVencimiento(tarea);

        MiembroGrupo miembroAutenticado = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), tarea.getGrupo().getIdGrupo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "El usuario no pertenece al grupo de la tarea"));

        boolean esAdministrador = ROL_ADMINISTRADOR.equalsIgnoreCase(miembroAutenticado.getRol().getNombre());
        boolean esUsuarioAsignado = tarea.getUsuarioAsignado().getIdUsuario().equals(usuarioAutenticado.getIdUsuario());
        EstadoTarea estadoActual = tarea.getEstado();
        EstadoTarea nuevoEstado = request.getEstado();

        if (!esAdministrador && !esUsuarioAsignado) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tiene permiso para cambiar el estado de una tarea no asignada");
        }

        if (!esAdministrador) {
            validarCambioMiembro(estadoActual, nuevoEstado);
        }

        LocalDateTime fechaCambio = LocalDateTime.now();
        tarea.setEstado(nuevoEstado);
        tarea.setFechaCambioEstado(fechaCambio);

        Integer puntosSumados = 0;
        Integer puntosTotales = null;

        if (nuevoEstado == EstadoTarea.COMPLETADA && estadoActual != EstadoTarea.COMPLETADA) {
            MiembroGrupo miembroAsignado = miembroGrupoRepository
                    .findByUsuarioIdUsuarioAndGrupoIdGrupo(tarea.getUsuarioAsignado().getIdUsuario(), tarea.getGrupo().getIdGrupo())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El usuario asignado no pertenece al grupo"));

            puntosSumados = calcularPuntos(tarea.getPrioridad());
            int puntosActuales = miembroAsignado.getPuntos() != null ? miembroAsignado.getPuntos() : 0;
            miembroAsignado.setPuntos(puntosActuales + puntosSumados);
            miembroGrupoRepository.save(miembroAsignado);
            puntosTotales = miembroAsignado.getPuntos();
        } else {
            MiembroGrupo miembroAsignado = miembroGrupoRepository
                    .findByUsuarioIdUsuarioAndGrupoIdGrupo(tarea.getUsuarioAsignado().getIdUsuario(), tarea.getGrupo().getIdGrupo())
                    .orElse(null);
            if (miembroAsignado != null) {
                puntosTotales = miembroAsignado.getPuntos();
            }
        }

        Tarea tareaGuardada = tareaRepository.save(tarea);
        return construirCambioEstadoResponse(tareaGuardada, puntosSumados, puntosTotales, "Estado de tarea actualizado correctamente");
    }

    // ===============================
    // ELIMINAR TAREA
    // ===============================
    @Transactional
    public EliminarTareaResponse eliminarTarea(String authorizationHeader, Long idTarea) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        MiembroGrupo miembroAutenticado = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), tarea.getGrupo().getIdGrupo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Acceso denegado"));

        boolean esAdministrador = ROL_ADMINISTRADOR.equalsIgnoreCase(miembroAutenticado.getRol().getNombre());
        if (!esAdministrador) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }

        if (tarea.isEliminado()) {
            throw new ApiException(HttpStatus.CONFLICT, "La tarea ya fue eliminada");
        }

        LocalDateTime fechaEliminacion = LocalDateTime.now();
        tarea.setEliminado(true);
        tarea.setFechaEliminacion(fechaEliminacion);

        Tarea tareaGuardada = tareaRepository.save(tarea);

        return new EliminarTareaResponse(
                tareaGuardada.getIdTarea(),
                tareaGuardada.isEliminado(),
                tareaGuardada.getFechaEliminacion(),
                "Tarea eliminada correctamente"
        );
    }

    // ===============================
    // 🧩 TAREAS VENCIDAS AUTOMÁTICAS
    // ===============================
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void marcarTareasVencidas() {
        List<Tarea> tareasVencidas = tareaRepository.findByEstadoInAndFechaLimiteBeforeAndEliminadoFalse(
                List.of(EstadoTarea.PENDIENTE, EstadoTarea.EN_PROGRESO),
                LocalDateTime.now()
        );

        LocalDateTime fechaCambio = LocalDateTime.now();
        for (Tarea tarea : tareasVencidas) {
            tarea.setEstado(EstadoTarea.VENCIDA);
            tarea.setFechaCambioEstado(fechaCambio);
        }
        tareaRepository.saveAll(tareasVencidas);
    }

    // ===============================
    // 🔧 MÉTODOS AUXILIARES
    // ===============================
    private void evaluarVencimiento(Tarea tarea) {
        validarTareaNoEliminada(tarea);

        if ((tarea.getEstado() == EstadoTarea.PENDIENTE || tarea.getEstado() == EstadoTarea.EN_PROGRESO)
                && tarea.getFechaLimite().isBefore(LocalDateTime.now())) {
            tarea.setEstado(EstadoTarea.VENCIDA);
            tarea.setFechaCambioEstado(LocalDateTime.now());
            tareaRepository.save(tarea);
        }
    }

    private void validarTareaNoEliminada(Tarea tarea) {
        if (tarea.isEliminado()) {
            throw new ApiException(HttpStatus.CONFLICT, "La tarea ya fue eliminada");
        }
    }

    private void validarCambioMiembro(EstadoTarea estadoActual, EstadoTarea nuevoEstado) {
        if (estadoActual == EstadoTarea.VENCIDA) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Esta tarea ha vencido. Solo el administrador puede reabrirla.");
        }

        if (estadoActual == EstadoTarea.COMPLETADA) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "No se puede revertir una tarea que ya fue completada");
        }

        if (estadoActual == EstadoTarea.PENDIENTE
                && (nuevoEstado == EstadoTarea.EN_PROGRESO || nuevoEstado == EstadoTarea.COMPLETADA)) {
            return;
        }

        if (estadoActual == EstadoTarea.EN_PROGRESO && nuevoEstado == EstadoTarea.COMPLETADA) {
            return;
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, "Cambio de estado no permitido");
    }

    private Integer calcularPuntos(PrioridadTarea prioridad) {
        if (prioridad == PrioridadTarea.ALTA) return 15;
        if (prioridad == PrioridadTarea.MEDIA) return 10;
        return 5;
    }

    private CambiarEstadoTareaResponse construirCambioEstadoResponse(Tarea tarea, Integer puntosSumados,
                                                                     Integer puntosTotales, String mensaje) {
        return new CambiarEstadoTareaResponse(
                tarea.getIdTarea(),
                tarea.getNombre(),
                tarea.getPrioridad(),
                tarea.getEstado(),
                tarea.getFechaLimite(),
                tarea.getFechaCambioEstado(),
                tarea.getGrupo().getIdGrupo(),
                tarea.getUsuarioAsignado().getIdUsuario(),
                puntosSumados,
                puntosTotales,
                mensaje
        );
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null) return null;
        String d = descripcion.trim();
        return d.isEmpty() ? null : d;
    }
}
