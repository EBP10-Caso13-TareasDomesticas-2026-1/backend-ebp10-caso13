CREATE TABLE roles (
    id_rol BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (nombre)
VALUES ('ADMINISTRADOR'), ('MIEMBRO');

CREATE TABLE usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    pin_seguridad_hash VARCHAR(255) NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE grupos (
    id_grupo BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    codigo_invitacion VARCHAR(6) NOT NULL UNIQUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE miembros_grupo (
    id_miembro_grupo BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_grupo BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    fecha_union TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_miembros_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_miembros_grupo
        FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo),

    CONSTRAINT fk_miembros_rol
        FOREIGN KEY (id_rol) REFERENCES roles(id_rol),

    CONSTRAINT uq_usuario_grupo
        UNIQUE (id_usuario, id_grupo)
);

CREATE TABLE sesiones (
    id_sesion BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    creada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en TIMESTAMP NOT NULL,
    cerrada_en TIMESTAMP,

    CONSTRAINT fk_sesiones_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);