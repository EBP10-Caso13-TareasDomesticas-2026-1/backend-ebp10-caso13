package com.tareasdomesticas.backend.dto;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import jakarta.validation.constraints.NotNull;

public class CambiarEstadoTareaRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoTarea estado;

    public CambiarEstadoTareaRequest() {
    }

    public CambiarEstadoTareaRequest(EstadoTarea estado) {
        this.estado = estado;
    }

    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }
}
