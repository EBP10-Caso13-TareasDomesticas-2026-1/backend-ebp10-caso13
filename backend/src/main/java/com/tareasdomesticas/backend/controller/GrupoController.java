package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.dto.CrearGrupoRequest;
import com.tareasdomesticas.backend.dto.GrupoResponse;
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
@RequestMapping("/grupos")
public class GrupoController {

    private final GrupoService grupoService;
    private final MiembroGrupoService miembroGrupoService;
    private final UsuarioService usuarioService;
    private final RoleService roleService;

    public GrupoController(GrupoService grupoService,
                           MiembroGrupoService miembroGrupoService,
                           UsuarioService usuarioService,
                           RoleService roleService) {
        this.grupoService = grupoService;
        this.miembroGrupoService = miembroGrupoService;
        this.usuarioService = usuarioService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<GrupoResponse> listarGrupos() {
        List<Grupo> grupos = grupoService.listarTodos();

        return grupos.stream()
                .map(grupo -> new GrupoResponse(
                        grupo.getIdGrupo(),
                        grupo.getNombre(),
                        null //  miembro NO ve código aquí
                ))
                .toList();
    }
    public ResponseEntity<?> crearGrupo(@RequestBody CrearGrupoRequest request) {
        try {
            if (miembroGrupoService.usuarioYaPerteneceAGrupo(request.getIdUsuario())) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El usuario ya pertenece a un grupo");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            Usuario usuario = usuarioService.buscarPorId(request.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Role rolAdministrador = roleService.buscarPorNombre("ADMINISTRADOR")
                    .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));

            Grupo grupo = new Grupo();
            grupo.setNombre(request.getNombre());

            Grupo grupoGuardado = grupoService.crearGrupo(grupo);

            MiembroGrupo miembroGrupo = new MiembroGrupo();
            miembroGrupo.setUsuario(usuario);
            miembroGrupo.setGrupo(grupoGuardado);
            miembroGrupo.setRol(rolAdministrador);
            miembroGrupo.setFechaUnion(LocalDateTime.now());

            miembroGrupoService.guardar(miembroGrupo);
            GrupoResponse response = new GrupoResponse(
            grupoGuardado.getIdGrupo(),
            grupoGuardado.getNombre(),
            grupoGuardado.getCodigoInvitacion() // aquí sí lo ve el admin creador
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{id}/codigo-invitacion")
    public ResponseEntity<String> obtenerCodigo(@PathVariable Long id) {
        Grupo grupo = grupoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        return ResponseEntity.ok(grupo.getCodigoInvitacion());
    }

    @PatchMapping("/{id}/codigo-invitacion")
    public ResponseEntity<Grupo> regenerarCodigo(@PathVariable Long id) {
        Grupo grupo = grupoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        grupo.setCodigoInvitacion(grupoService.generarCodigoInterno());
        Grupo actualizado = grupoService.guardar(grupo);

        return ResponseEntity.ok(actualizado);
    }
}