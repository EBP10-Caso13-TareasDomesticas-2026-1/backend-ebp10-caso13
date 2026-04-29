package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.dto.GrupoLoginResponse;
import com.tareasdomesticas.backend.dto.InicioSesionRequest;
import com.tareasdomesticas.backend.dto.InicioSesionResponse;
import com.tareasdomesticas.backend.dto.RegistroUsuarioRequest;
import com.tareasdomesticas.backend.dto.RegistroUsuarioResponse;
import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.service.MiembroGrupoService;
import com.tareasdomesticas.backend.service.SesionService;
import com.tareasdomesticas.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SesionService sesionService;
    private final MiembroGrupoService miembroGrupoService;
    private final Map<String, ResponseEntity<?>> registroIdempotente = new ConcurrentHashMap<>();

    public UsuarioController(UsuarioService usuarioService,
                             SesionService sesionService,
                             MiembroGrupoService miembroGrupoService) {
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;
        this.miembroGrupoService = miembroGrupoService;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroUsuarioRequest request,
                                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String key = normalizarIdempotencyKey(idempotencyKey);

        if (key != null && registroIdempotente.containsKey(key)) {
            return registroIdempotente.get(key);
        }

        ResponseEntity<?> response = procesarRegistro(request);

        if (key != null && response.getStatusCode().is2xxSuccessful()) {
            registroIdempotente.put(key, response);
        }

        return response;
    }

    private ResponseEntity<?> procesarRegistro(RegistroUsuarioRequest request) {
        try {
            Usuario usuarioGuardado = usuarioService.registrarUsuario(request);

            RegistroUsuarioResponse response = new RegistroUsuarioResponse(
                    usuarioGuardado.getIdUsuario(),
                    usuarioGuardado.getNombre(),
                    usuarioGuardado.getCorreo(),
                    "Usuario registrado correctamente"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    private String normalizarIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody InicioSesionRequest request) {
        try {
            Usuario usuario = usuarioService.validarCredenciales(
                    request.getCorreo(),
                    request.getContrasena()
            );

            Sesion sesion = sesionService.crearSesion(usuario);
            Optional<GrupoLoginResponse> grupo = miembroGrupoService.obtenerGrupoLoginPorUsuario(usuario.getIdUsuario());

            InicioSesionResponse response = new InicioSesionResponse(
                    usuario.getIdUsuario(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    sesion.getToken(),
                    grupo.isPresent(),
                    grupo.orElse(null),
                    "Inicio de sesión correcto"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
