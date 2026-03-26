package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.dto.RegistroUsuarioRequest;
import com.tareasdomesticas.backend.dto.RegistroUsuarioResponse;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroUsuarioRequest request) {
        try {
            Usuario usuarioGuardado = usuarioService.registrarUsuario(request);

            RegistroUsuarioResponse response = new RegistroUsuarioResponse(
                    usuarioGuardado.getIdUsuario(),
                    usuarioGuardado.getNombre(),
                    usuarioGuardado.getCorreo(),
                    "Usuario registrado correctamente"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }
}