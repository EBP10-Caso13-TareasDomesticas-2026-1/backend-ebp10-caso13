package com.tareasdomesticas.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tareasdomesticas.backend.dto.CambiarEstadoTareaRequest;
import com.tareasdomesticas.backend.dto.CambiarEstadoTareaResponse;
import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.dto.TareaTableroResponse;
import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.service.TareaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    // ===============================
    // 🧩 TABLERO (ESCENARIOS 1 y 2)
    // ===============================
    @GetMapping("/tablero")
    public ResponseEntity<?> obtenerTablero(
            @RequestHeader("Authorization") String authorization) {

        Map<EstadoTarea, List<TareaTableroResponse>> tablero =
                tareaService.obtenerTablero(authorization);

        if (tablero.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "No hay tareas registradas",
                    "tablero", tablero
            ));
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Tablero cargado correctamente",
                "tablero", tablero
        ));
    }

    // ===============================
    // 🧩 COMPATIBILIDAD FRONTEND
    // 👉 /tareas/grupo/{idGrupo}
    // ===============================
    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<?> obtenerTableroPorGrupo(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long idGrupo) {

        Map<EstadoTarea, List<TareaTableroResponse>> tablero =
                tareaService.obtenerTableroPorGrupo(authorization, idGrupo);

        if (tablero.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "No hay tareas registradas para este grupo",
                    "tablero", tablero
            ));
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Tablero del grupo cargado correctamente",
                "tablero", tablero
        ));
    }

    // ===============================
    // 🧩 CREAR TAREA
    // ===============================
    @PostMapping
    public ResponseEntity<CrearTareaResponse> crearTarea(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CrearTareaRequest request
    ) {
        CrearTareaResponse response = tareaService.crearTarea(authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===============================
    // 🧩 CAMBIAR ESTADO
    // ===============================
    @PatchMapping("/{idTarea}/estado")
    public ResponseEntity<CambiarEstadoTareaResponse> cambiarEstado(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long idTarea,
            @Valid @RequestBody CambiarEstadoTareaRequest request
    ) {
        CambiarEstadoTareaResponse response =
                tareaService.cambiarEstado(authorization, idTarea, request);

        return ResponseEntity.ok(response);
    }
}