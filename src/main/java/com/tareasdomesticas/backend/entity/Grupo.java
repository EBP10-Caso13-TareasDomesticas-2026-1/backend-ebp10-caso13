package com.tareasdomesticas.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long idGrupo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "codigo_invitacion", nullable = false, unique = true, length = 6)
    private String codigoInvitacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public Grupo() {
    }

    public Grupo(Long idGrupo, String nombre, String codigoInvitacion, LocalDateTime creadoEn) {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
        this.codigoInvitacion = codigoInvitacion;
        this.creadoEn = creadoEn;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
