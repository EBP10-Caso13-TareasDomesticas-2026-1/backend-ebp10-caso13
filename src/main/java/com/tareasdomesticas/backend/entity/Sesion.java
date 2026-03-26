package com.tareasdomesticas.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long idSesion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

    public Sesion() {
    }

    public Sesion(Long idSesion, Usuario usuario, String token, LocalDateTime creadaEn,
                  LocalDateTime expiraEn, LocalDateTime cerradaEn) {
        this.idSesion = idSesion;
        this.usuario = usuario;
        this.token = token;
        this.creadaEn = creadaEn;
        this.expiraEn = expiraEn;
        this.cerradaEn = cerradaEn;
    }

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(LocalDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public LocalDateTime getCerradaEn() {
        return cerradaEn;
    }

    public void setCerradaEn(LocalDateTime cerradaEn) {
        this.cerradaEn = cerradaEn;
    }
}