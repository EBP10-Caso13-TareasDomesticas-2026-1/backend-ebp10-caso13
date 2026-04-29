package com.tareasdomesticas.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UnirseGrupoRequest {

    @NotBlank(message = "El codigo de invitacion es obligatorio")
    private String codigoInvitacion;

    public UnirseGrupoRequest() {
    }

    public UnirseGrupoRequest(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }
}
