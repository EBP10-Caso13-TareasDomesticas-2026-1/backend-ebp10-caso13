package com.tareasdomesticas.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "miembros_grupo",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario"})
)
public class MiembroGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_miembro_grupo")
    private Long idMiembroGrupo;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_grupo", nullable = false)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Role rol;

    @Column(name = "fecha_union", nullable = false)
    private LocalDateTime fechaUnion;

    public MiembroGrupo() {
    }

    public MiembroGrupo(Long idMiembroGrupo, Usuario usuario, Grupo grupo, Role rol, LocalDateTime fechaUnion) {
        this.idMiembroGrupo = idMiembroGrupo;
        this.usuario = usuario;
        this.grupo = grupo;
        this.rol = rol;
        this.fechaUnion = fechaUnion;
    }

    public Long getIdMiembroGrupo() {
        return idMiembroGrupo;
    }

    public void setIdMiembroGrupo(Long idMiembroGrupo) {
        this.idMiembroGrupo = idMiembroGrupo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(LocalDateTime fechaUnion) {
        this.fechaUnion = fechaUnion;
    }
}
