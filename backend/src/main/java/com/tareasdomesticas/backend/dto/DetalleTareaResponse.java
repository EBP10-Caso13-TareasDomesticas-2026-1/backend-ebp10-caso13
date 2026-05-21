package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.PrioridadTarea;

public class DetalleTareaResponse {

    private Long idTarea;
    private String nombre;
    private String descripcion;
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private LocalDateTime fechaLimite;
    private Long idGrupo;
    private Long idUsuarioAsignado;
    private String responsable;

    public DetalleTareaResponse() {
    }

    public DetalleTareaResponse(Long idTarea, String nombre, String descripcion, PrioridadTarea prioridad,
                                EstadoTarea estado, LocalDateTime fechaLimite, Long idGrupo,
                                Long idUsuarioAsignado, String responsable) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
        this.idGrupo = idGrupo;
        this.idUsuarioAsignado = idUsuarioAsignado;
        this.responsable = responsable;
    }

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
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

    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Long getIdUsuarioAsignado() {
        return idUsuarioAsignado;
    }

    public void setIdUsuarioAsignado(Long idUsuarioAsignado) {
        this.idUsuarioAsignado = idUsuarioAsignado;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
}