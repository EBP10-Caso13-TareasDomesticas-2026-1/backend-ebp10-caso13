package com.tareasdomesticas.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tareasdomesticas.backend.dto.AbandonarGrupoResponse;
import com.tareasdomesticas.backend.dto.EliminarMiembroResponse;
import com.tareasdomesticas.backend.dto.GrupoLoginResponse;
import com.tareasdomesticas.backend.dto.MiembroGrupoResponse;
import com.tareasdomesticas.backend.dto.TransferirAdminResponse;
import com.tareasdomesticas.backend.dto.UnirseGrupoRequest;
import com.tareasdomesticas.backend.dto.UnirseGrupoResponse;
import com.tareasdomesticas.backend.dto.RankingResponse;
import com.tareasdomesticas.backend.dto.RankingMemberResponse;
import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Tarea;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import com.tareasdomesticas.backend.repository.TareaRepository;

@Service
public class MiembroGrupoService {

    private static final String ROL_MIEMBRO = "MIEMBRO";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final MiembroGrupoRepository miembroGrupoRepository;
    private final SesionService sesionService;
    private final GrupoService grupoService;
    private final RoleService roleService;
    private final TareaRepository tareaRepository;

        public record RankingEntry(Long idUsuario, String nombre, Integer puntos, Integer tareasCompletadas) {}

    public MiembroGrupoService(MiembroGrupoRepository miembroGrupoRepository,
                               SesionService sesionService,
                               GrupoService grupoService,
                               RoleService roleService,
                               TareaRepository tareaRepository) {
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.sesionService = sesionService;
        this.grupoService = grupoService;
        this.roleService = roleService;
        this.tareaRepository = tareaRepository;
    }

    public List<MiembroGrupo> listarTodos() {
        return miembroGrupoRepository.findAll();
    }

    public Optional<MiembroGrupo> buscarPorId(Long id) {
        return miembroGrupoRepository.findById(id);
    }

    public MiembroGrupo guardar(MiembroGrupo miembroGrupo) {
        return miembroGrupoRepository.save(miembroGrupo);
    }

    public boolean usuarioYaPerteneceAGrupo(Long usuarioId) {
        return miembroGrupoRepository.findByUsuarioIdUsuario(usuarioId).isPresent();
    }

    public boolean esAdminDelGrupo(Long usuarioId, Long grupoId) {
        Optional<MiembroGrupo> miembro = miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioId, grupoId);
        if (miembro.isPresent()) {
            return "ADMINISTRADOR".equals(miembro.get().getRol().getNombre());
        }
        return false;
    }

    public Optional<GrupoLoginResponse> obtenerGrupoLoginPorUsuario(Long idUsuario) {
        return miembroGrupoRepository.findByUsuarioIdUsuario(idUsuario)
                .map(miembro -> {
                    Grupo grupo = miembro.getGrupo();
                    List<MiembroGrupoResponse> miembros = miembroGrupoRepository.findByGrupoIdGrupo(grupo.getIdGrupo())
                            .stream()
                            .map(item -> new MiembroGrupoResponse(
                                    item.getUsuario().getIdUsuario(),
                                    item.getUsuario().getNombre(),
                                    item.getUsuario().getCorreo(),
                                    item.getRol().getNombre()
                            ))
                            .collect(Collectors.toList());

                    return new GrupoLoginResponse(
                            grupo.getIdGrupo(),
                            grupo.getNombre(),
                            grupo.getCodigoInvitacion(),
                            miembros
                    );
                });
    }

    @Transactional
    public UnirseGrupoResponse unirseAGrupo(String authorizationHeader, UnirseGrupoRequest request) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        if (miembroGrupoRepository.findByUsuarioIdUsuario(usuarioAutenticado.getIdUsuario()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "El usuario ya pertenece a un grupo");
        }

        Grupo grupo = grupoService.buscarPorCodigoInvitacion(request.getCodigoInvitacion())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Codigo de invitacion invalido"));

        Role rolMiembro = roleService.buscarPorNombre(ROL_MIEMBRO)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol MIEMBRO no configurado"));

        MiembroGrupo miembroGrupo = new MiembroGrupo();
        miembroGrupo.setUsuario(usuarioAutenticado);
        miembroGrupo.setGrupo(grupo);
        miembroGrupo.setRol(rolMiembro);
        miembroGrupo.setFechaUnion(LocalDateTime.now());

        MiembroGrupo guardado = miembroGrupoRepository.save(miembroGrupo);

        List<Tarea> tareasExMiembro = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
                        grupo.getIdGrupo(), usuarioAutenticado.getIdUsuario(), true);
        if (!tareasExMiembro.isEmpty()) {
            tareasExMiembro.forEach(t -> t.setExMiembro(false));
            tareaRepository.saveAll(tareasExMiembro);
        }

        return new UnirseGrupoResponse(
                guardado.getIdMiembroGrupo(),
                guardado.getUsuario().getIdUsuario(),
                guardado.getGrupo().getIdGrupo(),
                guardado.getGrupo().getNombre(),
                guardado.getRol().getNombre(),
                guardado.getFechaUnion(),
                "Te has unido al grupo exitosamente"
        );
    }

    @Transactional
    public AbandonarGrupoResponse abandonarGrupo(Long idGrupo, String authorizationHeader) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembro = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), idGrupo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No perteneces a este grupo"));

        List<Tarea> tareasActivas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstadoIn(
                        idGrupo, usuarioAutenticado.getIdUsuario(),
                        List.of(EstadoTarea.PENDIENTE, EstadoTarea.EN_PROGRESO, EstadoTarea.VENCIDA));

        if (!tareasActivas.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No puedes salir del grupo hasta resolver tus tareas activas o vencidas");
        }

        if (ROL_ADMINISTRADOR.equals(miembro.getRol().getNombre())) {
            List<MiembroGrupo> miembrosActivos = miembroGrupoRepository.findByGrupoIdGrupo(idGrupo);
            if (miembrosActivos.size() > 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Debes transferir el rol de administrador antes de abandonar el grupo");
            }
        }

        List<Tarea> tareasCompletadas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(
                        idGrupo, usuarioAutenticado.getIdUsuario(), EstadoTarea.COMPLETADA);
        if (!tareasCompletadas.isEmpty()) {
            tareasCompletadas.forEach(t -> t.setExMiembro(true));
            tareaRepository.saveAll(tareasCompletadas);
        }

        Integer puntosAcumulados = miembro.getPuntos() != null ? miembro.getPuntos() : 0;
        miembroGrupoRepository.delete(miembro);

        return new AbandonarGrupoResponse(usuarioAutenticado.getIdUsuario(), idGrupo,
                puntosAcumulados, "Has abandonado el grupo correctamente");
    }

    @Transactional
    public TransferirAdminResponse transferirAdmin(Long idGrupo, Long idNuevoAdmin,
                                                   String authorizationHeader) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembroSolicitante = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), idGrupo)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "No tienes permisos para transferir el rol de administrador"));

        if (!ROL_ADMINISTRADOR.equals(miembroSolicitante.getRol().getNombre())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para transferir el rol de administrador");
        }

        MiembroGrupo miembroNuevoAdmin = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(idNuevoAdmin, idGrupo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "El usuario no es miembro del grupo"));

        Role rolAdministrador = roleService.buscarPorNombre(ROL_ADMINISTRADOR)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol ADMINISTRADOR no configurado"));

        Role rolMiembro = roleService.buscarPorNombre(ROL_MIEMBRO)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol MIEMBRO no configurado"));

        miembroSolicitante.setRol(rolMiembro);
        miembroNuevoAdmin.setRol(rolAdministrador);

        miembroGrupoRepository.save(miembroSolicitante);
        miembroGrupoRepository.save(miembroNuevoAdmin);

        return new TransferirAdminResponse(idGrupo, usuarioAutenticado.getIdUsuario(),
                idNuevoAdmin, "Rol de administrador transferido correctamente");
    }

    @Transactional
    public EliminarMiembroResponse eliminarMiembro(Long idGrupo, Long idUsuarioAEliminar,
                                                   String authorizationHeader) {
        Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

        MiembroGrupo miembroSolicitante = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), idGrupo)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "No tienes permisos para eliminar miembros del grupo"));

        if (!ROL_ADMINISTRADOR.equals(miembroSolicitante.getRol().getNombre())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar miembros del grupo");
        }

        MiembroGrupo miembroAEliminar = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(idUsuarioAEliminar, idGrupo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Miembro no encontrado en el grupo"));

        List<Tarea> tareasActivas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstadoIn(
                        idGrupo, idUsuarioAEliminar,
                        List.of(EstadoTarea.PENDIENTE, EstadoTarea.EN_PROGRESO, EstadoTarea.VENCIDA));

        if (!tareasActivas.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El miembro tiene tareas activas o vencidas que deben resolverse antes de ser removido del grupo");
        }

        List<Tarea> tareasCompletadas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(
                        idGrupo, idUsuarioAEliminar, EstadoTarea.COMPLETADA);
        if (!tareasCompletadas.isEmpty()) {
            tareasCompletadas.forEach(t -> t.setExMiembro(true));
            tareaRepository.saveAll(tareasCompletadas);
        }

        miembroGrupoRepository.delete(miembroAEliminar);

        return new EliminarMiembroResponse(idUsuarioAEliminar, idGrupo,
                "Miembro eliminado correctamente");
    }

        @Transactional(readOnly = true)
        public RankingResponse obtenerRankingPorGrupo(Long idGrupo, String authorizationHeader) {
                Usuario usuarioAutenticado = sesionService.obtenerUsuarioAutenticado(authorizationHeader);

                // verificar que el usuario pertenece al grupo
                miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(usuarioAutenticado.getIdUsuario(), idGrupo)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No perteneces a este grupo"));

                // obtener miembros del grupo
                var miembros = miembroGrupoRepository.findByGrupoIdGrupo(idGrupo);

                // calcular puntajes y tareas completadas por miembro
                var entradas = miembros.stream().map(m -> {
                        Long idUsuario = m.getUsuario().getIdUsuario();
                        String nombre = m.getUsuario().getNombre();
                        Integer puntos = m.getPuntos() != null ? m.getPuntos() : 0;
                        int completadas = tareaRepository.findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(idGrupo, idUsuario, EstadoTarea.COMPLETADA)
                                        .size();
                        return new RankingEntry(idUsuario, nombre, puntos, completadas);
                }).toList();

                // si nadie tiene tareas completadas -> mensaje
                boolean cualquierCompletada = entradas.stream().anyMatch(e -> e.tareasCompletadas() > 0);
                if (!cualquierCompletada) {
                        return new RankingResponse("No hay tareas completadas en el grupo");
                }

                // ordenar por puntos desc, y en caso de empate por nombre asc
                var ordenado = entradas.stream()
                                .sorted((a, b) -> {
                                        int cmp = b.puntos().compareTo(a.puntos());
                                        if (cmp != 0) return cmp;
                                        return a.nombre().compareToIgnoreCase(b.nombre());
                                })
                                .toList();

                // asignar posiciones con empates
                java.util.List<com.tareasdomesticas.backend.dto.RankingMemberResponse> ranking = new java.util.ArrayList<>();
                int posicion = 1;
                for (int i = 0; i < ordenado.size(); i++) {
                        var entry = ordenado.get(i);
                        if (i > 0) {
                                var prev = ordenado.get(i - 1);
                                if (!entry.puntos().equals(prev.puntos())) {
                                        posicion = i + 1;
                                }
                        }
                        ranking.add(new com.tareasdomesticas.backend.dto.RankingMemberResponse(posicion, entry.idUsuario(), entry.nombre(), entry.tareasCompletadas(), entry.puntos()));
                }

                return new RankingResponse(ranking);
        }
}
