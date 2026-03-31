package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.service.MiembroGrupoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MiembroGrupoController {

    private final MiembroGrupoService miembroGrupoService;

    public MiembroGrupoController(MiembroGrupoService miembroGrupoService) {
        this.miembroGrupoService = miembroGrupoService;
    }

    @GetMapping("/miembros-grupo")
    public List<MiembroGrupo> listarMiembrosGrupo() {
        return miembroGrupoService.listarTodos();
    }

    @PostMapping("/miembros-grupo")
    public MiembroGrupo agregarMiembro(@RequestBody MiembroGrupo miembroGrupo) {
        return miembroGrupoService.crearMiembro(miembroGrupo);
    }
}
