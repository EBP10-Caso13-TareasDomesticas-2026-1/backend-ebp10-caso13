package com.tareasdomesticas.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TareaEliminacionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private MiembroGrupoRepository miembroGrupoRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private TareaRepository tareaRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Role rolAdmin;
    private Role rolMiembro;
    private Usuario adminGrupoA;
    private Usuario miembroGrupoA;
    private Usuario adminGrupoB;
    private Grupo grupoA;
    private Grupo grupoB;
    private String tokenAdminA;
    private String tokenMiembroA;
    private String tokenAdminB;

    @BeforeEach
    void prepararDatos() {
        tareaRepository.deleteAll();
        sesionRepository.deleteAll();
        miembroGrupoRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        rolAdmin = roleRepository.save(new Role(null, "ADMINISTRADOR"));
        rolMiembro = roleRepository.save(new Role(null, "MIEMBRO"));

        adminGrupoA = usuarioRepository.save(crearUsuario("Admin A", "admin-a-eliminar@test.com"));
        miembroGrupoA = usuarioRepository.save(crearUsuario("Miembro A", "miembro-a-eliminar@test.com"));
        adminGrupoB = usuarioRepository.save(crearUsuario("Admin B", "admin-b-eliminar@test.com"));

        grupoA = grupoRepository.save(crearGrupo("Casa A", "DEA001"));
        grupoB = grupoRepository.save(crearGrupo("Casa B", "DEB001"));

        miembroGrupoRepository.save(crearMiembro(adminGrupoA, grupoA, rolAdmin, 0));
        miembroGrupoRepository.save(crearMiembro(miembroGrupoA, grupoA, rolMiembro, 0));
        miembroGrupoRepository.save(crearMiembro(adminGrupoB, grupoB, rolAdmin, 0));

        tokenAdminA = "token-admin-eliminar-a";
        tokenMiembroA = "token-miembro-eliminar-a";
        tokenAdminB = "token-admin-eliminar-b";

        sesionRepository.save(crearSesion(adminGrupoA, tokenAdminA));
        sesionRepository.save(crearSesion(miembroGrupoA, tokenMiembroA));
        sesionRepository.save(crearSesion(adminGrupoB, tokenAdminB));
    }

    @Test
    void adminEliminaTareaCorrectamente_conSoftDeleteYEstadoIntacto() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Limpiar cocina", "Descripcion", grupoA, miembroGrupoA,
                PrioridadTarea.ALTA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTarea").value(tarea.getIdTarea()))
                .andExpect(jsonPath("$.eliminado").value(true))
                .andExpect(jsonPath("$.fechaEliminacion").exists())
                .andExpect(jsonPath("$.mensaje").value("Tarea eliminada correctamente"));

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertTrue(persistida.isEliminado());
        assertNotNull(persistida.getFechaEliminacion());
        assertEquals(EstadoTarea.PENDIENTE, persistida.getEstado());
    }

    @Test
    void tareaEliminadaDesapareceDelTablero() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Visible tablero", "Antes", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));

        JsonNode tableroAntes = obtenerTablero(tokenAdminA);
        assertTrue(contieneTarea(tableroAntes, tarea.getIdTarea()));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        JsonNode tableroDespues = obtenerTablero(tokenAdminA);
        assertFalse(contieneTarea(tableroDespues, tarea.getIdTarea()));
    }

    @Test
    void cancelacionDeEliminacion_sinDelete_noModificaBaseDeDatos() {
        Tarea tarea = tareaRepository.save(crearTarea("No eliminar", "Cancelada", grupoA, miembroGrupoA,
                PrioridadTarea.BAJA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(5)));

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertFalse(persistida.isEliminado());
        assertNull(persistida.getFechaEliminacion());
        assertEquals("No eliminar", persistida.getNombre());
    }

    @Test
    void eliminacionConservaTrazabilidadDeDatosHistoricos() throws Exception {
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(6);
        Tarea tarea = tareaRepository.save(crearTarea("Trazable", "Datos originales", grupoA, miembroGrupoA,
                PrioridadTarea.ALTA, EstadoTarea.EN_PROGRESO, fechaLimite));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Trazable", persistida.getNombre());
        assertEquals("Datos originales", persistida.getDescripcion());
        assertEquals(PrioridadTarea.ALTA, persistida.getPrioridad());
        assertEquals(fechaLimite, persistida.getFechaLimite());
        assertEquals(miembroGrupoA.getIdUsuario(), persistida.getUsuarioAsignado().getIdUsuario());
        assertEquals(EstadoTarea.EN_PROGRESO, persistida.getEstado());
        assertTrue(persistida.isEliminado());
        assertNotNull(persistida.getFechaEliminacion());
    }

    @Test
    void eliminarTareasPendienteEnProgresoVencidaYCompletada_conservaEstadoOriginal() throws Exception {
        for (EstadoTarea estado : List.of(EstadoTarea.PENDIENTE, EstadoTarea.EN_PROGRESO, EstadoTarea.VENCIDA, EstadoTarea.COMPLETADA)) {
            Tarea tarea = tareaRepository.save(crearTarea("Estado " + estado, "Sin cambio", grupoA, miembroGrupoA,
                    PrioridadTarea.MEDIA, estado, fechaParaEstado(estado)));

            mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                            .header("Authorization", "Bearer " + tokenAdminA))
                    .andExpect(status().isOk());

            Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
            assertEquals(estado, persistida.getEstado());
            assertTrue(persistida.isEliminado());
        }
    }

    @Test
    void eliminarTareaCompletada_conservaPuntajeYNoApareceEnTablero() throws Exception {
        MiembroGrupo membresia = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(miembroGrupoA.getIdUsuario(), grupoA.getIdGrupo())
                .orElseThrow();
        membresia.setPuntos(15);
        miembroGrupoRepository.save(membresia);

        Tarea tarea = tareaRepository.save(crearTarea("Completada con puntos", "Historica", grupoA, miembroGrupoA,
                PrioridadTarea.ALTA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        MiembroGrupo despues = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(miembroGrupoA.getIdUsuario(), grupoA.getIdGrupo())
                .orElseThrow();
        assertEquals(15, despues.getPuntos());
        assertEquals(EstadoTarea.COMPLETADA, tareaRepository.findById(tarea.getIdTarea()).orElseThrow().getEstado());
        assertFalse(contieneTarea(obtenerTablero(tokenAdminA), tarea.getIdTarea()));
    }

    @Test
    void miembroNoPuedeEliminarTareaDeSuGrupo() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Protegida", "Miembro intenta", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembroA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado"));

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertFalse(persistida.isEliminado());
        assertNull(persistida.getFechaEliminacion());
    }

    @Test
    void adminNoPuedeEliminarTareaDeOtroGrupo() throws Exception {
        Tarea tareaOtroGrupo = tareaRepository.save(crearTarea("Otro grupo", "No exponer", grupoB, adminGrupoB,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(delete("/tareas/{idTarea}", tareaOtroGrupo.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado"));

        Tarea persistida = tareaRepository.findById(tareaOtroGrupo.getIdTarea()).orElseThrow();
        assertFalse(persistida.isEliminado());
        assertNull(persistida.getFechaEliminacion());
    }

    @Test
    void adminNoPuedeConocerEstadoEliminadoDeTareaDeOtroGrupo() throws Exception {
        Tarea tareaOtroGrupo = crearTarea("Otro grupo eliminada", "No exponer", grupoB, adminGrupoB,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3));
        tareaOtroGrupo.setEliminado(true);
        tareaOtroGrupo.setFechaEliminacion(LocalDateTime.now().minusMinutes(5));
        tareaOtroGrupo = tareaRepository.save(tareaOtroGrupo);

        mockMvc.perform(delete("/tareas/{idTarea}", tareaOtroGrupo.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado"));
    }

    @Test
    void eliminarTareaInexistente_retornaNotFound() throws Exception {
        mockMvc.perform(delete("/tareas/{idTarea}", 999999L)
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Tarea no encontrada"));

        assertEquals(0, tareaRepository.count());
    }

    @Test
    void eliminarTareaYaEliminada_retornaConflictYNoAlteraFechaOriginal() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Doble delete", "Una vez", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        LocalDateTime fechaPrimeraEliminacion = tareaRepository.findById(tarea.getIdTarea()).orElseThrow().getFechaEliminacion();
        long totalAntes = tareaRepository.count();

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La tarea ya fue eliminada"));

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals(fechaPrimeraEliminacion, persistida.getFechaEliminacion());
        assertEquals(totalAntes, tareaRepository.count());
    }

    @Test
    void tareaEliminadaNoSePuedeEditar() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Original", "No tocar", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyEdicion("Nuevo", "Intento", "ALTA", LocalDateTime.now().plusHours(5)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La tarea ya fue eliminada"));

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("Original", persistida.getNombre());
        assertEquals("No tocar", persistida.getDescripcion());
        assertEquals(PrioridadTarea.MEDIA, persistida.getPrioridad());
    }

    @Test
    void tareaEliminadaNoSePuedeCambiarDeEstado() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Sin estado nuevo", "Bloqueada", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusHours(4)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/tareas/{idTarea}/estado", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "COMPLETADA"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La tarea ya fue eliminada"));

        assertEquals(EstadoTarea.EN_PROGRESO, tareaRepository.findById(tarea.getIdTarea()).orElseThrow().getEstado());
    }

    @Test
    void tareaEliminadaNoApareceEnDetalleDeUsuarioFinal() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Detalle oculto", "No visible", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Tarea no encontrada"));
    }

    @Test
    void eliminacionValidaCompletaEnMenosDeDosSegundos() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Rapida", "Medicion", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));

        long inicio = System.nanoTime();
        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());
        long fin = System.nanoTime();

        assertThat((fin - inicio) / 1_000_000).isLessThan(2000);
    }

    @Test
    void eliminacionNoReduceConteoFisicoDeTareas() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Persistente", "Auditoria", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));
        long totalAntes = tareaRepository.count();

        mockMvc.perform(delete("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        long totalDespues = tareaRepository.count();
        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals(totalAntes, totalDespues);
        assertTrue(persistida.isEliminado());
    }

    @Test
    void tareaEliminadaNoAfectaValidacionesDeTareasActivas() throws Exception {
        Tarea activa = tareaRepository.save(crearTarea("Activa eliminada", "No debe bloquear", grupoA, miembroGrupoA,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(4)));

        mockMvc.perform(delete("/tareas/{idTarea}", activa.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupoA.getIdGrupo())
                        .header("Authorization", "Bearer " + tokenMiembroA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Has abandonado el grupo correctamente"));

        List<Tarea> activas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstadoInAndEliminadoFalse(
                        grupoA.getIdGrupo(), miembroGrupoA.getIdUsuario(),
                        List.of(EstadoTarea.PENDIENTE, EstadoTarea.EN_PROGRESO, EstadoTarea.VENCIDA));
        assertTrue(activas.isEmpty());
    }

    private JsonNode obtenerTablero(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/tareas/tablero")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("tablero");
    }

    private boolean contieneTarea(JsonNode tablero, Long idTarea) {
        for (JsonNode grupoEstado : tablero) {
            for (JsonNode item : grupoEstado) {
                if (item.path("idTarea").asLong() == idTarea) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Object> bodyEdicion(String nombre, String descripcion, String prioridad, LocalDateTime fechaLimite) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        body.put("prioridad", prioridad);
        body.put("fechaLimite", fechaLimite.toString());
        return body;
    }

    private LocalDateTime fechaParaEstado(EstadoTarea estado) {
        if (estado == EstadoTarea.VENCIDA) {
            return LocalDateTime.now().minusHours(1);
        }
        return LocalDateTime.now().plusHours(2);
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

    private MiembroGrupo crearMiembro(Usuario usuario, Grupo grupo, Role role, Integer puntos) {
        MiembroGrupo miembroGrupo = new MiembroGrupo();
        miembroGrupo.setUsuario(usuario);
        miembroGrupo.setGrupo(grupo);
        miembroGrupo.setRol(role);
        miembroGrupo.setPuntos(puntos);
        miembroGrupo.setFechaUnion(LocalDateTime.now());
        return miembroGrupo;
    }

    private Sesion crearSesion(Usuario usuario, String token) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setToken(token);
        sesion.setCreadaEn(LocalDateTime.now());
        sesion.setExpiraEn(LocalDateTime.now().plusHours(2));
        sesion.setCerradaEn(null);
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
