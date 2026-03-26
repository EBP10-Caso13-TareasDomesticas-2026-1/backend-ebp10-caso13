package com.tareasdomesticas.backend.dto;

public class InicioSesionResponse {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private String token;
    private String mensaje;

    public InicioSesionResponse() {
    }

    public InicioSesionResponse(Long idUsuario, String nombre, String correo, String token, String mensaje) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}