package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.PrioridadTarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditarTareaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Size(max = 180, message = "La descripcion no puede superar los 180 caracteres")
    private String descripcion;

    private PrioridadTarea prioridad;

    private LocalDateTime fechaLimite;

    public EditarTareaRequest() {
    }

    public EditarTareaRequest(String nombre, String descripcion, PrioridadTarea prioridad, LocalDateTime fechaLimite) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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