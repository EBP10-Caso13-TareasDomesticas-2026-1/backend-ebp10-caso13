package com.tareasdomesticas.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tareasdomesticas.backend.dto.UnirseGrupoRequest;
import com.tareasdomesticas.backend.dto.UnirseGrupoResponse;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;

@Service
public class MiembroGrupoService {

    private static final String ROL_MIEMBRO = "MIEMBRO";

    private final MiembroGrupoRepository miembroGrupoRepository;
    private final SesionService sesionService;
    private final GrupoService grupoService;
    private final RoleService roleService;

    public MiembroGrupoService(MiembroGrupoRepository miembroGrupoRepository,
                               SesionService sesionService,
                               GrupoService grupoService,
                               RoleService roleService) {
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.sesionService = sesionService;
        this.grupoService = grupoService;
        this.roleService = roleService;
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
}
