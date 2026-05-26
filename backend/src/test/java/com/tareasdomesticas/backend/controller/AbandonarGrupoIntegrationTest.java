package com.tareasdomesticas.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

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
class AbandonarGrupoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private MiembroGrupoRepository miembroGrupoRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private TareaRepository tareaRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Role rolAdmin;
    private Role rolMiembro;
    private Usuario admin;
    private Usuario miembro;
    private Grupo grupo;

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

        admin = usuarioRepository.save(crearUsuario("Admin", "admin@test.com"));
        miembro = usuarioRepository.save(crearUsuario("Miembro", "miembro@test.com"));

        grupo = grupoRepository.save(crearGrupo("Casa Test", "COD999"));

        miembroGrupoRepository.save(crearMiembro(admin, grupo, rolAdmin));
        miembroGrupoRepository.save(crearMiembro(miembro, grupo, rolMiembro));

        tareaRepository.save(crearTarea("Tarea completada", grupo, miembro,
                PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusHours(5)));

        sesionRepository.save(crearSesion(admin, "token-admin", LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(miembro, "token-miembro", LocalDateTime.now().plusHours(2)));
    }

    // ─── abandonar grupo ───────────────────────────────────────────────────────

    @Test
    void abandonarGrupo_miembroRegular_retornaOk() throws Exception {
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(miembro.getIdUsuario()))
                .andExpect(jsonPath("$.idGrupo").value(grupo.getIdGrupo()))
                .andExpect(jsonPath("$.puntosAcumulados").value(0))
                .andExpect(jsonPath("$.mensaje").value("Has abandonado el grupo correctamente"));
    }

    @Test
    void abandonarGrupo_marcaTareasCompletadasComoExMiembro() throws Exception {
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isOk());

        List<Tarea> tareas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), EstadoTarea.COMPLETADA);
        assertTrue(tareas.stream().allMatch(Tarea::isExMiembro),
                "Todas las tareas COMPLETADAS deben tener exMiembro=true");
    }

    @Test
    void abandonarGrupo_conTareaPendiente_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("Pendiente", grupo, miembro,
                PrioridadTarea.ALTA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No puedes salir del grupo hasta resolver tus tareas activas o vencidas"));
    }

    @Test
    void abandonarGrupo_conTareaEnProgreso_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("En progreso", grupo, miembro,
                PrioridadTarea.MEDIA, EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No puedes salir del grupo hasta resolver tus tareas activas o vencidas"));
    }

    @Test
    void abandonarGrupo_conTareaVencida_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("Vencida", grupo, miembro,
                PrioridadTarea.BAJA, EstadoTarea.VENCIDA, LocalDateTime.now().minusHours(1)));

        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No puedes salir del grupo hasta resolver tus tareas activas o vencidas"));
    }

    @Test
    void abandonarGrupo_adminSinTransferir_retornaBadRequest() throws Exception {
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Debes transferir el rol de administrador antes de abandonar el grupo"));
    }

    @Test
    void abandonarGrupo_adminUnicoMiembro_retornaOk() throws Exception {
        // Eliminar al miembro regular para que admin quede solo
        MiembroGrupo miembroReg = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(miembro.getIdUsuario(), grupo.getIdGrupo())
                .orElseThrow();
        miembroGrupoRepository.delete(miembroReg);

        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(admin.getIdUsuario()))
                .andExpect(jsonPath("$.mensaje").value("Has abandonado el grupo correctamente"));
    }

    // ─── transferir admin ──────────────────────────────────────────────────────

    @Test
    void transferirAdmin_exitoso_retornaOk() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idNuevoAdmin", miembro.getIdUsuario()));

        mockMvc.perform(patch("/grupos/{idGrupo}/transferir-admin", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGrupo").value(grupo.getIdGrupo()))
                .andExpect(jsonPath("$.idAnteriorAdmin").value(admin.getIdUsuario()))
                .andExpect(jsonPath("$.idNuevoAdmin").value(miembro.getIdUsuario()))
                .andExpect(jsonPath("$.mensaje").value("Rol de administrador transferido correctamente"));
    }

    @Test
    void transferirAdmin_solicitanteNoEsAdmin_retornaForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idNuevoAdmin", admin.getIdUsuario()));

        mockMvc.perform(patch("/grupos/{idGrupo}/transferir-admin", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje")
                        .value("No tienes permisos para transferir el rol de administrador"));
    }

    @Test
    void transferirAdmin_nuevoAdminNoPertenece_retornaNotFound() throws Exception {
        Usuario externo = usuarioRepository.save(crearUsuario("Externo", "externo@test.com"));
        String body = objectMapper.writeValueAsString(Map.of("idNuevoAdmin", externo.getIdUsuario()));

        mockMvc.perform(patch("/grupos/{idGrupo}/transferir-admin", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El usuario no es miembro del grupo"));
    }

    @Test
    void transferirAdmin_luego_adminPuedeAbandonar() throws Exception {
        // Transferir primero
        String bodyTransferir = objectMapper.writeValueAsString(
                Map.of("idNuevoAdmin", miembro.getIdUsuario()));

        mockMvc.perform(patch("/grupos/{idGrupo}/transferir-admin", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyTransferir))
                .andExpect(status().isOk());

        // Ahora el ex-admin (ahora MIEMBRO) puede abandonar
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Has abandonado el grupo correctamente"));
    }

    // ─── reingreso revierte ex_miembro ────────────────────────────────────────

    @Test
    void reingreso_trasSalida_revierteExMiembro() throws Exception {
        // Miembro abandona
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isOk());

        // Verificar que las tareas quedaron marcadas
        List<Tarea> marcadas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), true);
        assertFalse(marcadas.isEmpty(), "Debe haber tareas con exMiembro=true tras abandonar");

        // Reingreso con código de invitación
        String bodyUnirse = objectMapper.writeValueAsString(
                Map.of("codigoInvitacion", grupo.getCodigoInvitacion()));

        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-miembro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyUnirse))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rolNombre").value("MIEMBRO"));

        // Verificar que exMiembro fue revertido
        List<Tarea> aun = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), true);
        assertTrue(aun.isEmpty(), "No deben quedar tareas con exMiembro=true tras reingreso");
    }

    @Test
    void abandonarGrupo_sinHeaderAuthorization_retornaUnauthorized() throws Exception {
        mockMvc.perform(post("/grupos/{idGrupo}/abandonar", grupo.getIdGrupo()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token de autorizacion es obligatorio"));
    }

    @Test
    void transferirAdmin_sinHeaderAuthorization_retornaUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idNuevoAdmin", miembro.getIdUsuario()));

        mockMvc.perform(patch("/grupos/{idGrupo}/transferir-admin", grupo.getIdGrupo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token de autorizacion es obligatorio"));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Usuario crearUsuario(String nombre, String correo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasenaHash(passwordEncoder.encode("123456"));
        u.setPinSeguridadHash(passwordEncoder.encode("1234"));
        u.setCreadoEn(LocalDateTime.now());
        return u;
    }

    private Grupo crearGrupo(String nombre, String codigo) {
        Grupo g = new Grupo();
        g.setNombre(nombre);
        g.setCodigoInvitacion(codigo);
        g.setCreadoEn(LocalDateTime.now());
        return g;
    }

    private MiembroGrupo crearMiembro(Usuario usuario, Grupo grupo, Role role) {
        MiembroGrupo m = new MiembroGrupo();
        m.setUsuario(usuario);
        m.setGrupo(grupo);
        m.setRol(role);
        m.setFechaUnion(LocalDateTime.now());
        return m;
    }

    private Tarea crearTarea(String nombre, Grupo grupo, Usuario asignado,
                             PrioridadTarea prioridad, EstadoTarea estado,
                             LocalDateTime fechaLimite) {
        Tarea t = new Tarea();
        t.setNombre(nombre);
        t.setGrupo(grupo);
        t.setUsuarioAsignado(asignado);
        t.setPrioridad(prioridad);
        t.setEstado(estado);
        t.setFechaLimite(fechaLimite);
        t.setFechaCreacion(LocalDateTime.now());
        return t;
    }

    private Sesion crearSesion(Usuario usuario, String token, LocalDateTime expiraEn) {
        Sesion s = new Sesion();
        s.setUsuario(usuario);
        s.setToken(token);
        s.setCreadaEn(LocalDateTime.now());
        s.setExpiraEn(expiraEn);
        s.setCerradaEn(null);
        return s;
    }
}
