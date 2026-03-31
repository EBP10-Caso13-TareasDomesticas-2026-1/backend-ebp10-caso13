package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.service.SesionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SesionController {

    private final SesionService sesionService;

    public SesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @GetMapping("/sesiones")
    public List<Sesion> listarSesiones() {
        return sesionService.listarTodos();
    }

    @DeleteMapping("/cerrar-sesion")
    public ResponseEntity<String> cerrarSesion(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        sesionService.cerrarSesion(token);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
