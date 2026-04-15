CREATE TABLE tareas (
    id_tarea BIGSERIAL PRIMARY KEY,
    id_grupo BIGINT NOT NULL,
    id_usuario_asignado BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(180),
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(15) NOT NULL,
    fecha_limite TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tareas_grupo
        FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo),

    CONSTRAINT fk_tareas_usuario_asignado
        FOREIGN KEY (id_usuario_asignado) REFERENCES usuarios(id_usuario)
);
