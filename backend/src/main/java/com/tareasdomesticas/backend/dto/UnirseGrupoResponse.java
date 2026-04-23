package com.tareasdomesticas.backend.dto;

import java.time.LocalDateTime;

public class UnirseGrupoResponse {

    private Long idMiembroGrupo;
    private Long idUsuario;
    private Long idGrupo;
    private String nombreGrupo;
    private String rolNombre;
    private LocalDateTime fechaUnion;
    private String mensaje;

    public UnirseGrupoResponse() {
    }

    public UnirseGrupoResponse(Long idMiembroGrupo, Long idUsuario, Long idGrupo,
                               String nombreGrupo, String rolNombre,
                               LocalDateTime fechaUnion, String mensaje) {
        this.idMiembroGrupo = idMiembroGrupo;
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
        this.nombreGrupo = nombreGrupo;
        this.rolNombre = rolNombre;
        this.fechaUnion = fechaUnion;
        this.mensaje = mensaje;
    }

    public Long getIdMiembroGrupo() {
        return idMiembroGrupo;
    }

    public void setIdMiembroGrupo(Long idMiembroGrupo) {
        this.idMiembroGrupo = idMiembroGrupo;
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

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public LocalDateTime getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(LocalDateTime fechaUnion) {
        this.fechaUnion = fechaUnion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
