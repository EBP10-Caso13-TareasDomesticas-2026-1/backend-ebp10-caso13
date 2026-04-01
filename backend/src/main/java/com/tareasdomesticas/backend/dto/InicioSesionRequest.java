package com.tareasdomesticas.backend.dto;

public class InicioSesionRequest {

    private String correo;
    private String contrasena;

    public InicioSesionRequest() {
    }

    public InicioSesionRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}