# backend-ebp10-caso13

Backend del **Sistema de Organización de Tareas Domésticas** (Caso 13), desarrollado con **Java + Spring Boot** y **PostgreSQL** por el equipo **EBP10 de CodeF@ctory**.

---

## Heramientas y lenguajes:

| Herramienta | Versión |
|---|---|
| IDE | IntelliJ IDEA |
| Lenguaje | Java |
| JDK | 17 |
| Build tool | Maven |
| Framework | Spring Boot 4.0.4 |
| Base de datos | PostgreSQL 17.9 |
| Gestor BD visual | pgAdmin 4 |
| Control de versiones | Git + GitHub |

**Dependencias principales:** Spring Web · Spring Data JPA · PostgreSQL Driver

---

## Estructura del proyecto

```
backend/
├── entity/       → Entidades JPA (tablas de la BD)
├── repository/   → Acceso a datos con JpaRepository
├── service/      → Lógica de negocio
├── controller/   → Endpoints REST
└── dto/          → Objetos de transferencia de datos
```

---

## Base de datos — Sprint 1

**Base de datos principal:** `tareas_domesticas`
**Base de prueba:** `tareas_domesticas_test`

**Tablas creadas:**

| Tabla | Propósito |
|---|---|
| `roles` | Roles del sistema: ADMINISTRADOR y MIEMBRO |
| `usuarios` | Datos de registro: nombre, correo, contraseña (hash), pin (hash) |
| `grupos` | Grupos familiares con código de invitación |
| `miembros_grupo` | Relación usuario ↔ grupo ↔ rol |
| `sesiones` | Control de inicio/cierre de sesión con token |

El script de creación limpio está en [`Sprint_1.sql`](./Sprint_1.sql) e incluye tablas, llaves primarias, foráneas, restricciones UNIQUE y carga inicial de roles.

---

## Endpoints disponibles (verificación)

```
GET /usuarios
GET /grupos
GET /roles
GET /miembros-grupo
GET /sesiones
```

> Estos endpoints validan que la conexión entre Spring Boot y PostgreSQL funcione correctamente.

---

## Estado actual — Sprint 1

- [x] Estructura del backend en capas
- [x] Base de datos funcional con script validado
- [x] Conexión Spring Boot ↔ PostgreSQL
- [x] Entidades JPA, Repositories y Services base
- [x] Endpoints GET de verificación funcionando
- [ ] **Próximo:** Implementar historia de registro de usuario (DTO `RegistroUsuarioRequest` ya creado)

---

## Clonar y configurar el proyecto en tu máquina

### 1. Requisitos previos

Asegúrate de tener instalado:

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
- [Git](https://git-scm.com/downloads)
- Acceso al proyecto de Supabase (PostgreSQL en la nube)

### 2. Clonar el repositorio

Abre una terminal y ejecuta:

```bash
git clone https://github.com/EBP10-Caso13-TareasDomesticas-2026-1/backend-ebp10-caso13.git
cd backend-ebp10-caso13
```

### 3. Configurar variables de entorno (Supabase)

El proyecto ya no usa PostgreSQL local. La conexión se define por variables de entorno en `backend/.env`.

1. Entra a la carpeta `backend/`
2. Copia `backend/.env.example` como `backend/.env`
3. Completa (o valida) los valores de Supabase:

```properties
SUPABASE_DB_URL=jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=postgres.jndjfhgnulmtxdmxmjzm
SUPABASE_DB_PASSWORD=TU_PASSWORD_SUPABASE
```

La aplicación carga este archivo automáticamente y aplica SSL (`sslmode=require`) para la conexión remota.

### 4. Base de datos

La estructura de tablas y relaciones se mantiene exactamente igual al Sprint 1.
Si necesitas recrearla en otro entorno, usa `Sprint_1.sql` desde el editor SQL de Supabase.

### 5. Abrir y ejecutar el proyecto en IntelliJ

1. Abre IntelliJ IDEA → **File > Open** → selecciona la carpeta `backend/`
2. Espera a que Maven descargue las dependencias automáticamente
3. Busca la clase principal `BackendApplication.java` y ejecútala con el botón ▶️
4. Verifica en el navegador o Postman que el backend responde en `http://localhost:8080`

### 6. Verificar que todo funciona

Prueba uno de los endpoints en tu navegador o Postman:

```
GET http://localhost:8080/roles
```

Si ves la lista de roles, el backend está funcionando correctamente. 

---


## Equipo

Desarrollado por el equipo **EBP10 — Análisis 2, CodeF@ctory**.


