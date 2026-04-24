package com.tareasdomesticas.backend.dto;

public class GrupoResponse {

    private Long idGrupo;
    private String nombre;
    private String codigoInvitacion; // solo admin

    public GrupoResponse() {}

    public GrupoResponse(Long idGrupo, String nombre, String codigoInvitacion) {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
        this.codigoInvitacion = codigoInvitacion;
    }

    public Long getIdGrupo() { return idGrupo; }
    public void setIdGrupo(Long idGrupo) { this.idGrupo = idGrupo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoInvitacion() { return codigoInvitacion; }
    public void setCodigoInvitacion(String codigoInvitacion) { this.codigoInvitacion = codigoInvitacion; }
}
