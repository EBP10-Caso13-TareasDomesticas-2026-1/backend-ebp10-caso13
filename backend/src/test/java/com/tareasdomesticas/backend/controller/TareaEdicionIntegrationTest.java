package com.tareasdomesticas.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.PrioridadTarea;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.entity.Tarea;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.repository.GrupoRepository;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import com.tareasdomesticas.backend.repository.RoleRepository;
import com.tareasdomesticas.backend.repository.SesionRepository;
import com.tareasdomesticas.backend.repository.TareaRepository;
import com.tareasdomesticas.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TareaEdicionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private MiembroGrupoRepository miembroGrupoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Usuario adminUsuario;
    private Usuario miembroUsuario;
    private Grupo grupoPrincipal;
    private String adminToken;
    private String miembroToken;

    @BeforeEach
    void prepararDatos() {
        tareaRepository.deleteAll();
        sesionRepository.deleteAll();
        miembroGrupoRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role(null, "ADMINISTRADOR"));
        Role miembroRole = roleRepository.save(new Role(null, "MIEMBRO"));

        adminUsuario = usuarioRepository.save(crearUsuario("Admin", "admin-edicion@test.com"));
        miembroUsuario = usuarioRepository.save(crearUsuario("Miembro", "miembro-edicion@test.com"));

        grupoPrincipal = grupoRepository.save(crearGrupo("Casa HU-008", "EDT123"));

        miembroGrupoRepository.save(crearMiembro(adminUsuario, grupoPrincipal, adminRole));
        miembroGrupoRepository.save(crearMiembro(miembroUsuario, grupoPrincipal, miembroRole));

        adminToken = "token-admin-edicion";
        miembroToken = "token-miembro-edicion";

        sesionRepository.save(crearSesion(adminUsuario, adminToken, LocalDateTime.now().plusHours(2), null));
        sesionRepository.save(crearSesion(miembroUsuario, miembroToken, LocalDateTime.now().plusHours(2), null));
    }

    @Test
    void givenAdminAndPendingTask_whenPatchEdit_thenUpdatesAndKeepsPending() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Lavar platos",
                "Descripcion inicial",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(4)
        ));

        LocalDateTime nuevaFecha = LocalDateTime.now().plusHours(6);
        Map<String, Object> body = bodyEdicion("Lavar platos profundo", "Usar esponja nueva", "ALTA", nuevaFecha);

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTarea").value(tarea.getIdTarea()))
                .andExpect(jsonPath("$.nombre").value("Lavar platos profundo"))
                .andExpect(jsonPath("$.descripcion").value("Usar esponja nueva"))
                .andExpect(jsonPath("$.prioridad").value("ALTA"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        Tarea actualizada = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Lavar platos profundo", actualizada.getNombre());
        assertEquals("Usar esponja nueva", actualizada.getDescripcion());
        assertEquals(PrioridadTarea.ALTA, actualizada.getPrioridad());
        assertEquals(EstadoTarea.PENDIENTE, actualizada.getEstado());
        assertEquals(nuevaFecha, actualizada.getFechaLimite());
    }

    @Test
    void givenAdminAndInProgressTask_whenPatchEdit_thenUpdatesAndKeepsInProgress() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Ordenar sala",
                "Antes del almuerzo",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.BAJA,
                EstadoTarea.EN_PROGRESO,
                LocalDateTime.now().plusHours(5)
        ));

        LocalDateTime nuevaFecha = LocalDateTime.now().plusHours(8);
        Map<String, Object> body = bodyEdicion("Ordenar sala completa", "Incluye estantes", "MEDIA", nuevaFecha);

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ordenar sala completa"))
                .andExpect(jsonPath("$.prioridad").value("MEDIA"))
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));

        Tarea actualizada = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Ordenar sala completa", actualizada.getNombre());
        assertEquals("Incluye estantes", actualizada.getDescripcion());
        assertEquals(PrioridadTarea.MEDIA, actualizada.getPrioridad());
        assertEquals(EstadoTarea.EN_PROGRESO, actualizada.getEstado());
        assertEquals(nuevaFecha, actualizada.getFechaLimite());
    }

    @Test
    void givenAdminAndOverdueTask_whenPatchWithoutDeadline_thenUpdatesAllowedFieldsAndKeepsDeadline() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.now().minusHours(2);
        Tarea tarea = tareaRepository.save(crearTarea(
                "Sacar basura",
                "Descripcion vieja",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.VENCIDA,
                fechaOriginal
        ));

        Map<String, Object> body = bodyEdicion("Sacar basura reciclaje", "Separar residuos", "ALTA", null);

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sacar basura reciclaje"))
                .andExpect(jsonPath("$.prioridad").value("ALTA"))
                .andExpect(jsonPath("$.estado").value("VENCIDA"));

        Tarea actualizada = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Sacar basura reciclaje", actualizada.getNombre());
        assertEquals("Separar residuos", actualizada.getDescripcion());
        assertEquals(PrioridadTarea.ALTA, actualizada.getPrioridad());
        assertEquals(EstadoTarea.VENCIDA, actualizada.getEstado());
        assertEquals(fechaOriginal, actualizada.getFechaLimite());
    }

    @Test
    void givenAdminAndOverdueTask_whenPatchChangingDeadline_thenRejectsAndKeepsData() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.now().minusHours(3);
        Tarea tarea = tareaRepository.save(crearTarea(
                "Trapear piso",
                "Descripcion original",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.BAJA,
                EstadoTarea.VENCIDA,
                fechaOriginal
        ));

        Map<String, Object> body = bodyEdicion("Trapear piso sala", "Nueva descripcion", "MEDIA", LocalDateTime.now().plusHours(2));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha limite no puede modificarse en una tarea vencida"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Trapear piso", sinCambios.getNombre());
        assertEquals("Descripcion original", sinCambios.getDescripcion());
        assertEquals(PrioridadTarea.BAJA, sinCambios.getPrioridad());
        assertEquals(EstadoTarea.VENCIDA, sinCambios.getEstado());
        assertEquals(fechaOriginal, sinCambios.getFechaLimite());
    }

    @Test
    void givenAdminAndCompletedTask_whenPatchEdit_thenRejectsAndKeepsOriginalData() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.now().plusHours(2);
        Tarea tarea = tareaRepository.save(crearTarea(
                "Planchar ropa",
                "No cambiar",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.COMPLETADA,
                fechaOriginal
        ));

        Map<String, Object> body = bodyEdicion("Planchar ropa total", "Intento de cambio", "ALTA", LocalDateTime.now().plusHours(5));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No se puede editar una tarea completada"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Planchar ropa", sinCambios.getNombre());
        assertEquals("No cambiar", sinCambios.getDescripcion());
        assertEquals(PrioridadTarea.MEDIA, sinCambios.getPrioridad());
        assertEquals(EstadoTarea.COMPLETADA, sinCambios.getEstado());
        assertEquals(fechaOriginal, sinCambios.getFechaLimite());
    }

    @Test
    void givenEditableTask_whenPatchWithoutNombre_thenRejectsValidationAndKeepsData() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Limpiar cocina",
                "Sin cambios",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(2)
        ));

        Map<String, Object> body = bodyEdicion("   ", "Intento invalido", "ALTA", LocalDateTime.now().plusHours(4));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Limpiar cocina", sinCambios.getNombre());
        assertEquals("Sin cambios", sinCambios.getDescripcion());
        assertEquals(PrioridadTarea.MEDIA, sinCambios.getPrioridad());
    }

    @Test
    void givenEditableTask_whenPatchWithNombreOver50_thenRejectsValidationAndKeepsData() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Lavar bano",
                "Sin cambios",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(2)
        ));

        String nombreLargo = "x".repeat(51);
        Map<String, Object> body = bodyEdicion(nombreLargo, "Intento invalido", "ALTA", LocalDateTime.now().plusHours(4));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre no puede superar los 50 caracteres"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Lavar bano", sinCambios.getNombre());
    }

    @Test
    void givenEditableTask_whenPatchWithDescripcionOver180_thenRejectsValidationAndKeepsData() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Doblar ropa",
                "Sin cambios",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.EN_PROGRESO,
                LocalDateTime.now().plusHours(2)
        ));

        String descripcionLarga = "y".repeat(181);
        Map<String, Object> body = bodyEdicion("Doblar ropa", descripcionLarga, "BAJA", LocalDateTime.now().plusHours(4));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.descripcion").value("La descripcion no puede superar los 180 caracteres"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Sin cambios", sinCambios.getDescripcion());
    }

    @Test
    void givenEditableTask_whenPatchWithPastDeadline_thenRejectsAndKeepsData() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.now().plusHours(2);
        Tarea tarea = tareaRepository.save(crearTarea(
                "Limpiar ventanas",
                "Sin cambios",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                fechaOriginal
        ));

        Map<String, Object> body = bodyEdicion("Limpiar ventanas", "Intento invalido", "ALTA", LocalDateTime.now().minusMinutes(5));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha y hora limite deben ser posteriores al momento actual"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals(fechaOriginal, sinCambios.getFechaLimite());
        assertEquals("Sin cambios", sinCambios.getDescripcion());
        assertEquals(PrioridadTarea.MEDIA, sinCambios.getPrioridad());
    }

    @Test
    void givenMemberUser_whenPatchEditTask_thenRejectsByPermissionsAndKeepsData() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Limpiar patio",
                "Descripcion original",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.BAJA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(3)
        ));

        Map<String, Object> body = bodyEdicion("Limpiar patio completo", "Intento miembro", "MEDIA", LocalDateTime.now().plusHours(6));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + miembroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo un administrador puede editar tareas"));

        Tarea sinCambios = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Limpiar patio", sinCambios.getNombre());
        assertEquals("Descripcion original", sinCambios.getDescripcion());
    }

    @Test
    void givenSuccessfulEdit_whenPatchTask_thenDoesNotCreateNewTaskRecord() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Lavar auto",
                "Descripcion original",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(3)
        ));

        long totalAntes = tareaRepository.count();

        Map<String, Object> body = bodyEdicion("Lavar auto completo", "Con cera", "ALTA", LocalDateTime.now().plusHours(7));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        long totalDespues = tareaRepository.count();
        assertEquals(totalAntes, totalDespues);

        Tarea actualizada = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Lavar auto completo", actualizada.getNombre());
        assertEquals(tarea.getIdTarea(), actualizada.getIdTarea());
    }

    @Test
    void givenSuccessfulEdit_whenGetTablero_thenShowsUpdatedInfoInSameStateColumn() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Barrer corredor",
                "Descripcion inicial",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(3)
        ));

        Map<String, Object> body = bodyEdicion("Barrer corredor principal", "Incluir entrada", "ALTA", LocalDateTime.now().plusHours(9));

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/tareas/tablero")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode pendientes = root.path("tablero").path("PENDIENTE");

        boolean encontrada = false;
        for (JsonNode item : pendientes) {
            if (item.path("idTarea").asLong() == tarea.getIdTarea()) {
                encontrada = true;
                assertEquals("Barrer corredor principal", item.path("nombre").asText());
                assertEquals("PENDIENTE", item.path("estado").asText());
            }
        }

        assertTrue(encontrada);
    }

    @Test
    void givenTwoConsecutiveSamePatchRequests_whenEditTask_thenNoDuplicatesAndSingleUpdatedRecord() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Lustrar muebles",
                "Descripcion inicial",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(3)
        ));

        long totalAntes = tareaRepository.count();
        LocalDateTime nuevaFecha = LocalDateTime.now().plusHours(10);
        Map<String, Object> body = bodyEdicion("Lustrar muebles sala", "Con producto especial", "ALTA", nuevaFecha);

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        long totalDespues = tareaRepository.count();
        assertEquals(totalAntes, totalDespues);

        Tarea actualizada = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Lustrar muebles sala", actualizada.getNombre());
        assertEquals("Con producto especial", actualizada.getDescripcion());
        assertEquals(PrioridadTarea.ALTA, actualizada.getPrioridad());
        assertEquals(nuevaFecha, actualizada.getFechaLimite());
    }

    @Test
    void givenValidEdit_whenPatchTask_thenCompletesUnderOneSecondInTestEnvironment() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Limpiar garaje",
                "Descripcion inicial",
                grupoPrincipal,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(3)
        ));

        Map<String, Object> body = bodyEdicion("Limpiar garaje completo", "Mover cajas", "ALTA", LocalDateTime.now().plusHours(11));

        long inicio = System.nanoTime();
        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
        long fin = System.nanoTime();

        long duracionMs = (fin - inicio) / 1_000_000;
        assertThat(duracionMs).isLessThan(1000);
    }

    private Map<String, Object> bodyEdicion(String nombre, String descripcion, String prioridad, LocalDateTime fechaLimite) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        body.put("prioridad", prioridad);
        body.put("fechaLimite", fechaLimite != null ? fechaLimite.toString() : null);
        return body;
    }

    private Usuario crearUsuario(String nombre, String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(passwordEncoder.encode("123456"));
        usuario.setPinSeguridadHash(passwordEncoder.encode("1234"));
        usuario.setCreadoEn(LocalDateTime.now());
        return usuario;
    }

    private Grupo crearGrupo(String nombre, String codigoInvitacion) {
        Grupo grupo = new Grupo();
        grupo.setNombre(nombre);
        grupo.setCodigoInvitacion(codigoInvitacion);
        grupo.setCreadoEn(LocalDateTime.now());
        return grupo;
    }

    private MiembroGrupo crearMiembro(Usuario usuario, Grupo grupo, Role role) {
        MiembroGrupo miembroGrupo = new MiembroGrupo();
        miembroGrupo.setUsuario(usuario);
        miembroGrupo.setGrupo(grupo);
        miembroGrupo.setRol(role);
        miembroGrupo.setFechaUnion(LocalDateTime.now());
        return miembroGrupo;
    }

    private Sesion crearSesion(Usuario usuario, String token, LocalDateTime expiraEn, LocalDateTime cerradaEn) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setToken(token);
        sesion.setCreadaEn(LocalDateTime.now());
        sesion.setExpiraEn(expiraEn);
        sesion.setCerradaEn(cerradaEn);
        return sesion;
    }

    private Tarea crearTarea(String nombre, String descripcion, Grupo grupo, Usuario asignado,
                             PrioridadTarea prioridad, EstadoTarea estado, LocalDateTime fechaLimite) {
        Tarea tarea = new Tarea();
        tarea.setNombre(nombre);
        tarea.setDescripcion(descripcion);
        tarea.setGrupo(grupo);
        tarea.setUsuarioAsignado(asignado);
        tarea.setPrioridad(prioridad);
        tarea.setEstado(estado);
        tarea.setFechaLimite(fechaLimite);
        tarea.setFechaCreacion(LocalDateTime.now());
        tarea.setFechaCambioEstado(LocalDateTime.now());
        return tarea;
    }
}