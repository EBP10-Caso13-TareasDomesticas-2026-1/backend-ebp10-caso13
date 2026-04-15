package com.tareasdomesticas.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareasdomesticas.backend.entity.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
}
