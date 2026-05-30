package com.tareasdomesticas.backend.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.PrioridadTarea;
import com.tareasdomesticas.backend.entity.Tarea;

public class TareaFiltroSpecification {

    private TareaFiltroSpecification() {}

    public static Specification<Tarea> porGrupo(Long idGrupo) {
        return (root, query, cb) -> cb.equal(root.get("grupo").get("idGrupo"), idGrupo);
    }

    public static Specification<Tarea> porEstados(List<EstadoTarea> estados) {
        if (estados == null || estados.isEmpty()) return null;
        return (root, query, cb) -> root.get("estado").in(estados);
    }

    public static Specification<Tarea> porPrioridades(List<PrioridadTarea> prioridades) {
        if (prioridades == null || prioridades.isEmpty()) return null;
        return (root, query, cb) -> root.get("prioridad").in(prioridades);
    }

    public static Specification<Tarea> porMiembros(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, query, cb) -> root.get("usuarioAsignado").get("idUsuario").in(ids);
    }
}
