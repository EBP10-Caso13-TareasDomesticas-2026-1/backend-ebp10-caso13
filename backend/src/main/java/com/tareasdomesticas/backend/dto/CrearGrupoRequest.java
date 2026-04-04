package com.tareasdomesticas.backend.dto;

public class CrearGrupoRequest {

    private String nombre;
    private Long idUsuario;

    public CrearGrupoRequest() {}

    public CrearGrupoRequest(String nombre, Long idUsuario) {
        this.nombre = nombre;
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}