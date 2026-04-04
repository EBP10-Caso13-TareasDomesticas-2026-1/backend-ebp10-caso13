package com.tareasdomesticas.backend.dto;

public class UnirseGrupoRequest {

    private Long usuarioId;
    private String codigoInvitacion;

    public UnirseGrupoRequest() {
    }

    public UnirseGrupoRequest(Long usuarioId, String codigoInvitacion) {
        this.usuarioId = usuarioId;
        this.codigoInvitacion = codigoInvitacion;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }
}
