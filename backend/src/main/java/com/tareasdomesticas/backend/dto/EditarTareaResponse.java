package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.PrioridadTarea;

public class EditarTareaResponse {

    private Long idTarea;
    private String nombre;
    private String descripcion;
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaCambioEstado;
    private Long idGrupo;
    private Long idUsuarioAsignado;
    private String mensaje;

    public EditarTareaResponse() {
    }

    public EditarTareaResponse(Long idTarea, String nombre, String descripcion, PrioridadTarea prioridad,
                               EstadoTarea estado, LocalDateTime fechaLimite, LocalDateTime fechaCambioEstado,
                               Long idGrupo, Long idUsuarioAsignado, String mensaje) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
        this.fechaCambioEstado = fechaCambioEstado;
        this.idGrupo = idGrupo;
        this.idUsuarioAsignado = idUsuarioAsignado;
        this.mensaje = mensaje;
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

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado) {
        this.fechaCambioEstado = fechaCambioEstado;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}