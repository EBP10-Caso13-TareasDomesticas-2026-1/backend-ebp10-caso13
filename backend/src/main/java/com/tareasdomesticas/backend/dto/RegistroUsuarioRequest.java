package com.tareasdomesticas.backend.dto;

public class RegistroUsuarioRequest {

    private String nombre;
    private String correo;
    private String contrasena;
    private String pinSeguridad;

    public RegistroUsuarioRequest() {
    }

    public RegistroUsuarioRequest(String nombre, String correo, String contrasena, String pinSeguridad) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.pinSeguridad = pinSeguridad;
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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getPinSeguridad() {
        return pinSeguridad;
    }

    public void setPinSeguridad(String pinSeguridad) {
        this.pinSeguridad = pinSeguridad;
    }
}