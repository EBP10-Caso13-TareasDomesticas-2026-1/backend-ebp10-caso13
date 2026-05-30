package com.tareasdomesticas.backend.dto;

public class AbandonarGrupoResponse {

    private Long idUsuario;
    private Long idGrupo;
    private Integer puntosAcumulados;
    private String mensaje;

    public AbandonarGrupoResponse() {
    }

    public AbandonarGrupoResponse(Long idUsuario, Long idGrupo, Integer puntosAcumulados, String mensaje) {
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
        this.puntosAcumulados = puntosAcumulados;
        this.mensaje = mensaje;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Integer getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public void setPuntosAcumulados(Integer puntosAcumulados) {
        this.puntosAcumulados = puntosAcumulados;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
