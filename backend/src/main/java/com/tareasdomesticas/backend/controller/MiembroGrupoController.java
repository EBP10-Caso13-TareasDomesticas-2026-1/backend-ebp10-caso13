package com.tareasdomesticas.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tareasdomesticas.backend.dto.UnirseGrupoRequest;
import com.tareasdomesticas.backend.dto.UnirseGrupoResponse;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.service.MiembroGrupoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/miembros-grupo")
public class MiembroGrupoController {

    private final MiembroGrupoService miembroGrupoService;

    public MiembroGrupoController(MiembroGrupoService miembroGrupoService) {
        this.miembroGrupoService = miembroGrupoService;
    }

    @GetMapping
    public List<MiembroGrupo> listarMiembrosGrupo() {
        return miembroGrupoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<UnirseGrupoResponse> unirseAGrupo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UnirseGrupoRequest request) {
        UnirseGrupoResponse response = miembroGrupoService.unirseAGrupo(authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
