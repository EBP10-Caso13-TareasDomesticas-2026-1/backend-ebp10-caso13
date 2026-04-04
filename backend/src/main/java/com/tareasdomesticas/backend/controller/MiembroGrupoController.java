package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.dto.UnirseGrupoRequest;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.service.GrupoService;
import com.tareasdomesticas.backend.service.MiembroGrupoService;
import com.tareasdomesticas.backend.service.RoleService;
import com.tareasdomesticas.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/miembros-grupo")
public class MiembroGrupoController {

    private final MiembroGrupoService miembroGrupoService;
    private final UsuarioService usuarioService;
    private final GrupoService grupoService;
    private final RoleService roleService;

    public MiembroGrupoController(MiembroGrupoService miembroGrupoService,
                                  UsuarioService usuarioService,
                                  GrupoService grupoService,
                                  RoleService roleService) {
        this.miembroGrupoService = miembroGrupoService;
        this.usuarioService = usuarioService;
        this.grupoService = grupoService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<MiembroGrupo> listarMiembrosGrupo() {
        return miembroGrupoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<?> unirseAGrupo(@RequestBody UnirseGrupoRequest request) {
        try {
            if (miembroGrupoService.usuarioYaPerteneceAGrupo(request.getIdUsuario())) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El usuario ya pertenece a un grupo");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            Usuario usuario = usuarioService.buscarPorId(request.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Grupo grupo = grupoService.buscarPorCodigoInvitacion(request.getCodigoInvitacion())
                    .orElseThrow(() -> new RuntimeException("Código de invitación inválido"));

            Role rolMiembro = roleService.buscarPorNombre("MIEMBRO")
                    .orElseThrow(() -> new RuntimeException("Rol MIEMBRO no encontrado"));

            MiembroGrupo miembroGrupo = new MiembroGrupo();
            miembroGrupo.setUsuario(usuario);
            miembroGrupo.setGrupo(grupo);
            miembroGrupo.setRol(rolMiembro);
            miembroGrupo.setFechaUnion(LocalDateTime.now());

            MiembroGrupo miembroGuardado = miembroGrupoService.guardar(miembroGrupo);

            return ResponseEntity.ok(miembroGuardado);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}