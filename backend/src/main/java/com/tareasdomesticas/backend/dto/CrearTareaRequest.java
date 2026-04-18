package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.PrioridadTarea;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearTareaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Size(max = 180, message = "La descripcion no puede superar los 180 caracteres")
    private String descripcion;

    @NotNull(message = "El miembro asignado es obligatorio")
    private Long idUsuarioAsignado;

    private PrioridadTarea prioridad;

    @NotNull(message = "La fecha y hora limite es obligatoria")
    @Future(message = "La fecha y hora limite deben ser posteriores al momento actual")
    private LocalDateTime fechaLimite;

    public CrearTareaRequest() {
    }

    public CrearTareaRequest(String nombre, String descripcion, Long idUsuarioAsignado, PrioridadTarea prioridad,
                             LocalDateTime fechaLimite) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idUsuarioAsignado = idUsuarioAsignado;
        this.prioridad = prioridad;
        this.fechaLimite = fechaLimite;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getIdUsuarioAsignado() {
        return idUsuarioAsignado;
    }

    public void setIdUsuarioAsignado(Long idUsuarioAsignado) {
        this.idUsuarioAsignado = idUsuarioAsignado;
    }

    public PrioridadTarea getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadTarea prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
