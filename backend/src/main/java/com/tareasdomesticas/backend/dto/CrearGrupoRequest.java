package com.tareasdomesticas.backend.dto;

public class CrearGrupoRequest {

    private String nombre;
    private String codigoInvitacion;
    private Long idUsuario;

    public CrearGrupoRequest() {
    }

    public CrearGrupoRequest(String nombre, String codigoInvitacion, Long idUsuario) {
        this.nombre = nombre;
        this.codigoInvitacion = codigoInvitacion;
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}