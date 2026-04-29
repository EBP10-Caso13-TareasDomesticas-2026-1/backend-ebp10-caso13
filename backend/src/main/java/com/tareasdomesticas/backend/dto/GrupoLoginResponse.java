package com.tareasdomesticas.backend.dto;

import java.util.List;

public class GrupoLoginResponse {

    private Long idGrupo;
    private String nombre;
    private String codigoInvitacion;
    private List<MiembroGrupoResponse> miembros;

    public GrupoLoginResponse() {
    }

    public GrupoLoginResponse(Long idGrupo, String nombre, String codigoInvitacion, List<MiembroGrupoResponse> miembros) {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
        this.codigoInvitacion = codigoInvitacion;
        this.miembros = miembros;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
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

    public List<MiembroGrupoResponse> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<MiembroGrupoResponse> miembros) {
        this.miembros = miembros;
    }
}
