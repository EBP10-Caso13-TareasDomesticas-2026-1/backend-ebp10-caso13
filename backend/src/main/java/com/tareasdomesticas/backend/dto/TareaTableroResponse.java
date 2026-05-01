package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

import com.tareasdomesticas.backend.entity.EstadoTarea;

public class TareaTableroResponse {

    private Long idTarea;
    private String nombre;
    private String responsable;
    private EstadoTarea estado;
    private LocalDateTime fechaLimite;
    private boolean vencida; // 🔥 indicador visual
    private Long idUsuarioAsignado;

    public TareaTableroResponse(Long idTarea, String nombre, String responsable,
                                EstadoTarea estado, LocalDateTime fechaLimite,
                                boolean vencida, Long idUsuarioAsignado) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.responsable = responsable;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
        this.vencida = vencida;
        this.idUsuarioAsignado = idUsuarioAsignado;
    }

    public Long getIdTarea() { return idTarea; }
    public void setIdTarea(Long idTarea) { this.idTarea = idTarea; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }

    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDateTime fechaLimite) { this.fechaLimite = fechaLimite; }

    public boolean isVencida() { return vencida; }
    public void setVencida(boolean vencida) { this.vencida = vencida; }

    public Long getIdUsuarioAsignado() { return idUsuarioAsignado; }
    public void setIdUsuarioAsignado(Long idUsuarioAsignado) { this.idUsuarioAsignado = idUsuarioAsignado; }
}