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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TareaDetalleIntegrationTest {

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
    private Usuario otroUsuario;
    private Grupo grupoA;
    private Grupo grupoB;
    private String tokenAdmin;
    private String tokenMiembro;
    private String tokenOtro;

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

        adminUsuario = usuarioRepository.save(crearUsuario("Admin", "admin-detalle@test.com"));
        miembroUsuario = usuarioRepository.save(crearUsuario("Miembro", "miembro-detalle@test.com"));
        otroUsuario = usuarioRepository.save(crearUsuario("Otro", "otro-detalle@test.com"));

        grupoA = grupoRepository.save(crearGrupo("Grupo A", "GRA123"));
        grupoB = grupoRepository.save(crearGrupo("Grupo B", "GRB123"));

        miembroGrupoRepository.save(crearMiembro(adminUsuario, grupoA, adminRole));
        miembroGrupoRepository.save(crearMiembro(miembroUsuario, grupoA, miembroRole));
        miembroGrupoRepository.save(crearMiembro(otroUsuario, grupoB, adminRole));

        tokenAdmin = "token-admin-detalle";
        tokenMiembro = "token-miembro-detalle";
        tokenOtro = "token-otro-detalle";

        sesionRepository.save(crearSesion(adminUsuario, tokenAdmin, LocalDateTime.now().plusHours(2), null));
        sesionRepository.save(crearSesion(miembroUsuario, tokenMiembro, LocalDateTime.now().plusHours(2), null));
        sesionRepository.save(crearSesion(otroUsuario, tokenOtro, LocalDateTime.now().plusHours(2), null));
    }

    @Test
    void givenAuthenticatedUser_whenGetDetalle_thenRetornaInformacionCompleta() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Lavar platos",
                "Con detergente",
                grupoA,
                miembroUsuario,
                PrioridadTarea.ALTA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(5)
        ));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTarea").value(tarea.getIdTarea()))
                .andExpect(jsonPath("$.nombre").value("Lavar platos"))
                .andExpect(jsonPath("$.descripcion").value("Con detergente"))
                .andExpect(jsonPath("$.prioridad").value("ALTA"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.responsable").value(miembroUsuario.getNombre()));
    }

    @Test
    void givenTaskWithoutDescripcion_whenGetDetalle_thenRetornaDescripcionNull() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Tender cama",
                null,
                grupoA,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(4)
        ));

        MvcResult result = mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(json.has("descripcion"));
        assertTrue(json.get("descripcion").isNull());
    }

    @Test
    void givenPendingTask_whenGetDetalle_thenEstadoPendiente() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Pendiente", "D1", grupoA, miembroUsuario,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void givenInProgressTask_whenGetDetalle_thenEstadoEnProgreso() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("En progreso", "D2", grupoA, miembroUsuario,
                PrioridadTarea.MEDIA, EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    @Test
    void givenCompletedTask_whenGetDetalle_thenEstadoCompletada() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Completa", "D3", grupoA, miembroUsuario,
                PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void givenOverdueTask_whenGetDetalle_thenEstadoVencida() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Vencida", "D4", grupoA, miembroUsuario,
                PrioridadTarea.MEDIA, EstadoTarea.VENCIDA, LocalDateTime.now().minusHours(1)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VENCIDA"));
    }

    @Test
    void givenUserFromAnotherGroup_whenGetDetalle_thenForbidden() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Privada", "D5", grupoB, otroUsuario,
                PrioridadTarea.ALTA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tiene permiso para consultar esta tarea"));
    }

    @Test
    void givenIdInexistente_whenGetDetalle_thenNotFound() throws Exception {
        mockMvc.perform(get("/tareas/{idTarea}", 999999L)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Tarea no encontrada"));
    }

    @Test
    void givenRequestWithoutToken_whenGetDetalle_thenUnauthorized() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea("Sin token", "D6", grupoA, miembroUsuario,
                PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token de autorizacion es obligatorio"));
    }

    @Test
    void givenDetalleQuery_whenGetDetalle_thenNoModificaDatosPersistidos() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.now().plusHours(5);
        Tarea tarea = tareaRepository.save(crearTarea(
                "No mutar",
                "Debe quedar igual",
                grupoA,
                miembroUsuario,
                PrioridadTarea.BAJA,
                EstadoTarea.PENDIENTE,
                fechaOriginal
        ));

        mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk());

        Tarea persistida = tareaRepository.findById(tarea.getIdTarea()).orElseThrow();
        assertEquals("No mutar", persistida.getNombre());
        assertEquals("Debe quedar igual", persistida.getDescripcion());
        assertEquals(PrioridadTarea.BAJA, persistida.getPrioridad());
        assertEquals(EstadoTarea.PENDIENTE, persistida.getEstado());
        assertEquals(fechaOriginal, persistida.getFechaLimite());
    }

    @Test
    void givenTableroAndDetalle_whenConsultingBoth_thenCamposPrincipalesCoinciden() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Coincidir",
                "Comparacion",
                grupoA,
                miembroUsuario,
                PrioridadTarea.ALTA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(4)
        ));

        MvcResult tableroResult = mockMvc.perform(get("/tareas/tablero")
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult detalleResult = mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode tableroRoot = objectMapper.readTree(tableroResult.getResponse().getContentAsString());
        JsonNode pendientes = tableroRoot.path("tablero").path("PENDIENTE");

        boolean encontradaEnTablero = false;
        for (JsonNode item : pendientes) {
            if (item.path("idTarea").asLong() == tarea.getIdTarea()) {
                encontradaEnTablero = true;
                assertEquals("Coincidir", item.path("nombre").asText());
                assertEquals("PENDIENTE", item.path("estado").asText());
            }
        }
        assertTrue(encontradaEnTablero);

        JsonNode detalleNode = objectMapper.readTree(detalleResult.getResponse().getContentAsString());
        assertEquals(tarea.getIdTarea(), detalleNode.path("idTarea").asLong());
        assertEquals("Coincidir", detalleNode.path("nombre").asText());
        assertEquals("PENDIENTE", detalleNode.path("estado").asText());
    }

    @Test
    void givenValidDetalleQuery_whenGetDetalle_thenCompletesUnderOneSecond() throws Exception {
        Tarea tarea = tareaRepository.save(crearTarea(
                "Rapida",
                "Medicion",
                grupoA,
                miembroUsuario,
                PrioridadTarea.MEDIA,
                EstadoTarea.PENDIENTE,
                LocalDateTime.now().plusHours(2)
        ));

        long inicio = System.nanoTime();
        MvcResult result = mockMvc.perform(get("/tareas/{idTarea}", tarea.getIdTarea())
                        .header("Authorization", "Bearer " + tokenMiembro))
                .andExpect(status().isOk())
                .andReturn();
        long fin = System.nanoTime();

        assertNotNull(result.getResponse().getContentAsString());
        long duracionMs = (fin - inicio) / 1_000_000;
        assertThat(duracionMs).isLessThan(1000);
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