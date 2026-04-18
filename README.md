# backend-ebp10-caso13

Backend del Sistema de Organizacion de Tareas Domesticas (Caso 13), desarrollado con Java + Spring Boot.

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-JUnit%20%2B%20MockMvc-25A162?style=for-the-badge)

---

## Stack Tecnologico (Shields.io Badges)

| Tecnologia | Uso |
|---|---|
| ![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white) | Lenguaje principal |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.4-6DB33F?logo=springboot&logoColor=white) | Framework backend |
| ![JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C) | ORM y acceso a datos |
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791?logo=postgresql&logoColor=white) | Base de datos principal |
| ![Security](https://img.shields.io/badge/Spring_Security-Crypto-6DB33F?logo=springsecurity&logoColor=white) | Hash de contrasenas y PIN |
| ![Testing](https://img.shields.io/badge/Testing-JUnit%20%2B%20MockMvc-25A162) | Testing |
| ![H2](https://img.shields.io/badge/H2-InMemory-1A73E8) | Base de datos para pruebas |
| ![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white) | Build y dependencias |
| ![Git](https://img.shields.io/badge/Git-GitHub-F05032?logo=git&logoColor=white) | Control de versiones |

---

## Arquitectura

Proyecto monolitico por capas:

```text
backend/src/main/java/com/tareasdomesticas/backend/
├── controller   -> Endpoints REST
├── service      -> Logica de negocio
├── repository   -> Acceso a datos (JpaRepository)
├── entity       -> Entidades JPA
├── dto          -> Contratos request/response
├── config       -> Configuracion (ej. password encoder)
└── exception    -> Manejo global de excepciones
```

---

## Estado Funcional Actual

Actualmente el backend incluye:

- Registro de usuario
- Inicio de sesion con token
- Cierre de sesion
- Creacion de grupo
- Union a grupo por codigo de invitacion
- Creacion de tarea por administrador de grupo
- Validaciones de negocio y de request (Bean Validation)

---

## Base de Datos

### Produccion / Desarrollo remoto

- Motor: PostgreSQL en Supabase
- Configuracion en `backend/.env`
- SSL habilitado (`sslmode=require`)

### Scripts SQL del proyecto

Ejecuta en este orden:

1. `Sprint_1.sql`
2. `Sprint_2.sql`

### Tablas principales

- `roles`
- `usuarios`
- `grupos`
- `miembros_grupo`
- `sesiones`
- `tareas`

---

## Configuracion Inicial Del Proyecto

### 1) Requisitos

- JDK 17
- Git
- Acceso a Supabase (proyecto y credenciales)

### 2) Clonar

```bash
git clone https://github.com/EBP10-Caso13-TareasDomesticas-2026-1/backend-ebp10-caso13.git
cd backend-ebp10-caso13
```

### 3) Variables de entorno

Desde la raiz del repo:

```bash
copy backend\.env.example backend\.env
```

Luego completa `backend/.env`:

```properties
SUPABASE_DB_URL=jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=postgres.jndjfhgnulmtxdmxmjzm
SUPABASE_DB_PASSWORD=TU_PASSWORD_SUPABASE
```

> Nota: no subas `backend/.env` al repositorio.

### 4) Crear estructura en la BD

Ejecuta en Supabase SQL Editor:

1. `Sprint_1.sql`
2. `Sprint_2.sql`

---

## Ejecutar La Aplicacion

Desde `backend/`:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell tambien puedes usar:

```powershell
.\mvnw.cmd spring-boot:run
```

La app queda en:

- `http://localhost:8080`

---

## Ejecutar Pruebas

Las pruebas usan H2 en memoria con esta configuracion:

- `backend/src/test/resources/application.properties`
- `spring.jpa.hibernate.ddl-auto=create-drop`

### Todas las pruebas

```bash
./mvnw test
```

### Solo una clase de prueba

```bash
./mvnw -Dtest=TareaE2ETest test
```

### Tipos de pruebas actuales

- `TareasDomesticasBackendApplicationTests` (carga de contexto)
- `TareaServiceTest` (unitarias de servicio)
- `TareaControllerIntegrationTest` (integracion HTTP de controlador)
- `TareaE2ETest` (flujo end-to-end completo)

---

## Endpoints Disponibles

### Usuarios

- `GET /usuarios`
- `POST /usuarios/registro`
- `POST /usuarios/login`

### Grupos

- `GET /grupos`
- `POST /grupos`

### Miembros de grupo

- `GET /miembros-grupo`
- `POST /miembros-grupo`

### Roles

- `GET /roles`

### Sesiones

- `GET /sesiones`
- `POST /sesiones/logout`

### Tareas

- `POST /tareas`

---

## Flujo E2E Implementado

El flujo principal validado en pruebas:

1. Registro de administrador
2. Login de administrador
3. Creacion de grupo
4. Registro de segundo usuario
5. Union del segundo usuario al grupo
6. Creacion de tarea asignada al segundo usuario

Validaciones incluidas en el flujo:

- Token Bearer en Authorization
- Usuario asignado pertenece al grupo
- Estado inicial `PENDIENTE`
- Prioridad por defecto `MEDIA`

---

## Problemas Comunes Y Soluciones

### Error de conexion a BD

- Verifica `backend/.env`
- Verifica que corriste `Sprint_1.sql` y `Sprint_2.sql`
- Verifica acceso de red a Supabase

### Tests fallan por entorno

- Asegura ejecutar desde carpeta `backend/`
- Reinstala dependencias con `./mvnw clean test`

---

## Equipo

Desarrollado por EBP10 - Analisis 2, CodeF@ctory.


