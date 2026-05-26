package com.tareasdomesticas.backend.dto;

public class EliminarMiembroResponse {

    private Long idUsuario;
    private Long idGrupo;
    private String mensaje;

    public EliminarMiembroResponse() {
    }

    public EliminarMiembroResponse(Long idUsuario, Long idGrupo, String mensaje) {
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
