package com.tareasdomesticas.backend.dto;

public class TransferirAdminResponse {

    private Long idGrupo;
    private Long idAnteriorAdmin;
    private Long idNuevoAdmin;
    private String mensaje;

    public TransferirAdminResponse() {
    }

    public TransferirAdminResponse(Long idGrupo, Long idAnteriorAdmin, Long idNuevoAdmin, String mensaje) {
        this.idGrupo = idGrupo;
        this.idAnteriorAdmin = idAnteriorAdmin;
        this.idNuevoAdmin = idNuevoAdmin;
        this.mensaje = mensaje;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Long getIdAnteriorAdmin() {
        return idAnteriorAdmin;
    }

    public void setIdAnteriorAdmin(Long idAnteriorAdmin) {
        this.idAnteriorAdmin = idAnteriorAdmin;
    }

    public Long getIdNuevoAdmin() {
        return idNuevoAdmin;
    }

    public void setIdNuevoAdmin(Long idNuevoAdmin) {
        this.idNuevoAdmin = idNuevoAdmin;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
