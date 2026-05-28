package com.tareasdomesticas.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tareasdomesticas.backend.dto.CambiarEstadoTareaRequest;
import com.tareasdomesticas.backend.dto.CambiarEstadoTareaResponse;
import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.dto.DetalleTareaResponse;
import com.tareasdomesticas.backend.dto.EditarTareaRequest;
import com.tareasdomesticas.backend.dto.EditarTareaResponse;
import com.tareasdomesticas.backend.dto.EliminarTareaResponse;
import com.tareasdomesticas.backend.dto.FiltrarTareasResponse;
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
    // 🧩 FILTRAR TAREAS (HU-011)
    // ===============================
    @GetMapping("/grupo/{idGrupo}/filtrar")
    public ResponseEntity<FiltrarTareasResponse> filtrarTareas(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long idGrupo,
            @RequestParam(required = false) List<String> estados,
            @RequestParam(required = false) List<String> prioridades,
            @RequestParam(required = false) List<Long> idsMiembros) {

        FiltrarTareasResponse response =
                tareaService.filtrarTareas(idGrupo, estados, prioridades, idsMiembros, authorization);
        return ResponseEntity.ok(response);
    }

        // ===============================
        // 🧩 DETALLE DE TAREA
        // ===============================
        @GetMapping("/{idTarea}")
        public ResponseEntity<DetalleTareaResponse> obtenerDetalleTarea(
                        @RequestHeader(value = "Authorization", required = false) String authorization,
                        @PathVariable Long idTarea
        ) {
                DetalleTareaResponse response = tareaService.obtenerDetalleTarea(authorization, idTarea);
                return ResponseEntity.ok(response);
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
        // 🧩 EDITAR TAREA
        // ===============================
        @PatchMapping("/{idTarea}")
    public ResponseEntity<EditarTareaResponse> editarTarea(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable Long idTarea,
                        @Valid @RequestBody EditarTareaRequest request
        ) {
                EditarTareaResponse response = tareaService.editarTarea(authorization, idTarea, request);
                return ResponseEntity.ok(response);
        }

        // ===============================
        // ELIMINAR TAREA
        // ===============================
        @DeleteMapping("/{idTarea}")
        public ResponseEntity<EliminarTareaResponse> eliminarTarea(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable Long idTarea
        ) {
                EliminarTareaResponse response = tareaService.eliminarTarea(authorization, idTarea);
                return ResponseEntity.ok(response);
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
