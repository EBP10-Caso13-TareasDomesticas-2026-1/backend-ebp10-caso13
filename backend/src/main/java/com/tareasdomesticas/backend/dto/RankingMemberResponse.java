package com.tareasdomesticas.backend.dto;

public class RankingMemberResponse {
    private Integer posicion;
    private Long idUsuario;
    private String nombre;
    private Integer tareasCompletadas;
    private Integer puntos;

    public RankingMemberResponse() {}

    public RankingMemberResponse(Integer posicion, Long idUsuario, String nombre, Integer tareasCompletadas, Integer puntos) {
        this.posicion = posicion;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.tareasCompletadas = tareasCompletadas;
        this.puntos = puntos;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
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

    public Integer getTareasCompletadas() {
        return tareasCompletadas;
    }

    public void setTareasCompletadas(Integer tareasCompletadas) {
        this.tareasCompletadas = tareasCompletadas;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }
}
