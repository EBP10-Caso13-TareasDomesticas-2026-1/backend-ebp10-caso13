package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.service.SesionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sesiones")
public class SesionController {

    private final SesionService sesionService;

    public SesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<Sesion> listarSesiones() {
        return sesionService.listarTodos();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> cerrarSesion(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        sesionService.cerrarSesion(token);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}