package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

public class EliminarTareaResponse {

    private Long idTarea;
    private boolean eliminado;
    private LocalDateTime fechaEliminacion;
    private String mensaje;

    public EliminarTareaResponse() {
    }

    public EliminarTareaResponse(Long idTarea, boolean eliminado, LocalDateTime fechaEliminacion, String mensaje) {
        this.idTarea = idTarea;
        this.eliminado = eliminado;
        this.fechaEliminacion = fechaEliminacion;
        this.mensaje = mensaje;
    }

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
