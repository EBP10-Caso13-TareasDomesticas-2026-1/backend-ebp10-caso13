package com.tareasdomesticas.backend.dto;

public class RestablecerContrasenaResponse {

    private String mensaje;

    public RestablecerContrasenaResponse() {
    }

    public RestablecerContrasenaResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
