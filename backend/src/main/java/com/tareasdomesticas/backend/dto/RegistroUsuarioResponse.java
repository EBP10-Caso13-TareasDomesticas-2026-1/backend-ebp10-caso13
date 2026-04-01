package com.tareasdomesticas.backend.dto;

public class RegistroUsuarioResponse {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private String mensaje;

    public RegistroUsuarioResponse() {
    }

    public RegistroUsuarioResponse(Long idUsuario, String nombre, String correo, String mensaje) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.mensaje = mensaje;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}