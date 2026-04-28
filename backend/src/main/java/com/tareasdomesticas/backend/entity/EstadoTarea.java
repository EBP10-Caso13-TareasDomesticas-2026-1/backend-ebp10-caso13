package com.tareasdomesticas.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    VENCIDA;

    @JsonCreator
    public static EstadoTarea fromString(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        return EstadoTarea.valueOf(normalized);
    }
}
