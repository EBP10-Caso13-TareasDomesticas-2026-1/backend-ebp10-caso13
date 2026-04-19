package com.tareasdomesticas.backend.dto;

public class UnirseGrupoRequest {

    private Long idUsuario;
    private String codigoInvitacion;

    public UnirseGrupoRequest() {
    }

    public UnirseGrupoRequest(Long idUsuario, String codigoInvitacion) {
        this.idUsuario = idUsuario;
        this.codigoInvitacion = codigoInvitacion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }
}
