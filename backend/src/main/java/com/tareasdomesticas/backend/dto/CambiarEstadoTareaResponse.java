package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.PrioridadTarea;

public class CambiarEstadoTareaResponse {

    private Long idTarea;
    private String nombre;
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaCambioEstado;
    private Long idGrupo;
    private Long idUsuarioAsignado;
    private Integer puntosSumados;
    private Integer puntosTotalesMiembro;
    private String mensaje;

    public CambiarEstadoTareaResponse() {
    }

    public CambiarEstadoTareaResponse(Long idTarea, String nombre, PrioridadTarea prioridad, EstadoTarea estado,
                                      LocalDateTime fechaLimite, LocalDateTime fechaCambioEstado, Long idGrupo,
                                      Long idUsuarioAsignado, Integer puntosSumados, Integer puntosTotalesMiembro,
                                      String mensaje) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
        this.fechaCambioEstado = fechaCambioEstado;
        this.idGrupo = idGrupo;
        this.idUsuarioAsignado = idUsuarioAsignado;
        this.puntosSumados = puntosSumados;
        this.puntosTotalesMiembro = puntosTotalesMiembro;
        this.mensaje = mensaje;
    }

    public Long getIdTarea() { return idTarea; }
    public void setIdTarea(Long idTarea) { this.idTarea = idTarea; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public PrioridadTarea getPrioridad() { return prioridad; }
    public void setPrioridad(PrioridadTarea prioridad) { this.prioridad = prioridad; }
    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }
    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDateTime fechaLimite) { this.fechaLimite = fechaLimite; }
    public LocalDateTime getFechaCambioEstado() { return fechaCambioEstado; }
    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado) { this.fechaCambioEstado = fechaCambioEstado; }
    public Long getIdGrupo() { return idGrupo; }
    public void setIdGrupo(Long idGrupo) { this.idGrupo = idGrupo; }
    public Long getIdUsuarioAsignado() { return idUsuarioAsignado; }
    public void setIdUsuarioAsignado(Long idUsuarioAsignado) { this.idUsuarioAsignado = idUsuarioAsignado; }
    public Integer getPuntosSumados() { return puntosSumados; }
    public void setPuntosSumados(Integer puntosSumados) { this.puntosSumados = puntosSumados; }
    public Integer getPuntosTotalesMiembro() { return puntosTotalesMiembro; }
    public void setPuntosTotalesMiembro(Integer puntosTotalesMiembro) { this.puntosTotalesMiembro = puntosTotalesMiembro; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
