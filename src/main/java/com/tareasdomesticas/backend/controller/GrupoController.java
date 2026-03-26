package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.service.GrupoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @GetMapping("/grupos")
    public List<Grupo> listarGrupos() {
        return grupoService.listarTodos();
    }
}