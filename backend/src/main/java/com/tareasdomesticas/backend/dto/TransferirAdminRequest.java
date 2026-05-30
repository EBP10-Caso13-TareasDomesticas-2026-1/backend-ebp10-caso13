package com.tareasdomesticas.backend.dto;

import jakarta.validation.constraints.NotNull;

public class TransferirAdminRequest {

    @NotNull(message = "El id del nuevo administrador es obligatorio")
    private Long idNuevoAdmin;

    public TransferirAdminRequest() {
    }

    public TransferirAdminRequest(Long idNuevoAdmin) {
        this.idNuevoAdmin = idNuevoAdmin;
    }

    public Long getIdNuevoAdmin() {
        return idNuevoAdmin;
    }

    public void setIdNuevoAdmin(Long idNuevoAdmin) {
        this.idNuevoAdmin = idNuevoAdmin;
    }
}
