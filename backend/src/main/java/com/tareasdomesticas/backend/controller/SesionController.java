package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.service.SesionService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
