# backend-ebp10-caso13

Backend de la aplicación del Caso 13 (**Sistema de Organización de Tareas Domésticas**) desarrollado para el equipo **EBP10 de CodeF@ctory**.

## Descripción del proyecto

Este repositorio contiene la base del backend del sistema para la organización de tareas domésticas en grupos familiares.  
En esta primera etapa se construyó la estructura inicial del proyecto en **Java con Spring Boot**, conectada a **PostgreSQL**, y se dejó implementada la base técnica para el Sprint 1.

El backend fue organizado en capas y preparado para soportar las historias de usuario iniciales relacionadas con autenticación de usuarios y gestión básica del grupo familiar.

---

## Objetivo del Sprint 1

El Sprint 1 estuvo enfocado en dejar lista la base de datos y la estructura backend necesaria para soportar las siguientes funcionalidades iniciales:

- Registro de usuario
- Inicio de sesión
- Cierre de sesión
- Creación de grupo familiar
- Invitación y unión de miembros mediante código

---

## Herramientas y versiones utilizadas

### Entorno de desarrollo
- **IDE:** IntelliJ IDEA
- **Lenguaje:** Java
- **JDK:** 17
- **Build tool:** Maven
- **Framework backend:** Spring Boot 4.0.4
- **Base de datos:** PostgreSQL 17.9
- **Gestor visual de base de datos:** pgAdmin 4
- **Control de versiones:** Git + GitHub

### Dependencias principales
- Spring Web
- Spring Data JPA
- PostgreSQL Driver

---

## Estructura base implementada

El proyecto fue organizado con una estructura en capas para facilitar la separación de responsabilidades:

- `entity` → entidades JPA que representan las tablas de la base de datos
- `repository` → acceso a datos mediante `JpaRepository`
- `service` → lógica base de negocio y operaciones intermedias
- `controller` → endpoints REST de prueba y verificación
- `dto` → objetos de transferencia de datos para comenzar el manejo de historias reales

---

## Base de datos del Sprint 1

Se creó y validó la base de datos principal:

- `tareas_domesticas`

Y también una base de prueba para validar el script limpio:

- `tareas_domesticas_test`

### Tablas creadas para el Sprint 1
- `roles`
- `usuarios`
- `grupos`
- `miembros_grupo`
- `sesiones`

### Propósito de cada tabla

#### `roles`
Almacena los roles base del sistema:
- ADMINISTRADOR
- MIEMBRO

#### `usuarios`
Guarda la información básica de los usuarios registrados:
- nombre
- correo
- hash de contraseña
- hash de pin de seguridad
- fecha de creación

#### `grupos`
Representa los grupos familiares del sistema:
- nombre del grupo
- código de invitación
- fecha de creación

#### `miembros_grupo`
Relaciona:
- usuario
- grupo
- rol

Esta tabla permite saber qué usuario pertenece a qué grupo y con qué rol.

#### `sesiones`
Permite modelar el inicio y cierre de sesión:
- usuario asociado
- token
- fecha de creación
- fecha de expiración
- fecha de cierre

---

## Script SQL generado

Se generó y validó un script limpio de creación de base de datos para el Sprint 1:

- creación de tablas
- llaves primarias
- llaves foráneas
- restricciones `UNIQUE`
- carga inicial de roles

Este script fue probado desde cero en una base separada para asegurar que pudiera ejecutarse correctamente sin depender de pruebas previas.

---

## Validaciones realizadas en PostgreSQL

Durante la construcción de la base de datos se realizaron pruebas manuales para verificar:

- creación correcta de usuarios
- creación correcta de grupos
- asociación de usuarios a grupos
- asignación de roles
- creación de sesiones
- cierre de sesión
- restricción para evitar duplicar un mismo usuario dentro del mismo grupo

También se verificó la correcta relación entre tablas mediante consultas SQL con `JOIN`.

---

## Configuración del backend con Spring Boot

Se creó un proyecto Spring Boot con Maven y se configuró la conexión con PostgreSQL usando `application.properties`.

### Configuración aplicada
- conexión al motor PostgreSQL local
- acceso con usuario `postgres`
- uso de la base `tareas_domesticas`
- `spring.jpa.hibernate.ddl-auto=none` para evitar que Spring modificara las tablas ya creadas manualmente

---

## Entidades JPA implementadas

Se crearon las siguientes entidades:

- `Role`
- `Usuario`
- `Grupo`
- `MiembroGrupo`
- `Sesion`

Estas entidades fueron mapeadas a las tablas del Sprint 1 usando anotaciones JPA.

---

## Repositories implementados

Se crearon los repositories base para acceso a datos:

- `RoleRepository`
- `UsuarioRepository`
- `GrupoRepository`
- `MiembroGrupoRepository`
- `SesionRepository`

Además, en `UsuarioRepository` se agregó soporte para:

- buscar usuario por correo
- validar si un correo ya existe

---

## Services implementados

Se crearon los services base:

- `RoleService`
- `UsuarioService`
- `GrupoService`
- `MiembroGrupoService`
- `SesionService`

Estos servicios permiten centralizar operaciones básicas como:

- listar registros
- buscar por id
- guardar entidades
- validar existencia de correo en usuarios

---

## Controllers implementados

Se crearon endpoints iniciales de verificación para comprobar la conexión completa entre:

- controlador
- servicio
- repository
- entidad
- base de datos

### Endpoints disponibles actualmente
- `GET /usuarios`
- `GET /grupos`
- `GET /roles`
- `GET /miembros-grupo`
- `GET /sesiones`

Estos endpoints fueron usados para validar que el backend estaba leyendo correctamente la información almacenada en PostgreSQL.

---

## Estado actual del backend

Hasta este punto, el proyecto ya cuenta con:

- estructura inicial del backend
- base de datos funcional del Sprint 1
- conexión exitosa entre Spring Boot y PostgreSQL
- entidades JPA mapeadas
- repositories y services base
- endpoints GET de prueba funcionando
- proyecto versionado y subido al repositorio de GitHub

---

## Próximo paso

El siguiente bloque de trabajo corresponde a comenzar la implementación de la primera historia real del Sprint 1:

- **registro de usuario**

Para ello ya se dejó creado el package `dto` y el DTO inicial:

- `RegistroUsuarioRequest`

---

## Notas

- La base relacional usada en esta etapa corresponde únicamente al alcance del Sprint 1.
- Las tablas relacionadas con tareas, estados, prioridades, comentarios, categorías, score y reportes quedan para los siguientes sprints.
- El enfoque inicial fue asegurar primero una base sólida de persistencia y arquitectura antes de implementar la lógica completa de negocio.

---

## Autoría y trabajo en equipo

Desarrollado para el equipo de **Análisis 2 - EBP10 de CodeF@ctory**.
