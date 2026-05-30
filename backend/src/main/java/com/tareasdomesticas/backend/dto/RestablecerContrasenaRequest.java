package com.tareasdomesticas.backend.dto;

public class RestablecerContrasenaRequest {

    private String correo;
    private String pinSeguridad;
    private String nuevaContrasena;
    private String confirmarContrasena;

    public RestablecerContrasenaRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPinSeguridad() {
        return pinSeguridad;
    }

    public void setPinSeguridad(String pinSeguridad) {
        this.pinSeguridad = pinSeguridad;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }
}
