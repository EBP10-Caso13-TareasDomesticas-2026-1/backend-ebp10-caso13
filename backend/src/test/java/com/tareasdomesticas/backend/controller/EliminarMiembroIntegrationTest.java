package com.tareasdomesticas.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class EliminarMiembroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private RoleRepository roleRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private MiembroGrupoRepository miembroGrupoRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private TareaRepository tareaRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario miembro;
    private Usuario miembroSinTareas;
    private Grupo grupo;

    @BeforeEach
    void prepararDatos() {
        tareaRepository.deleteAll();
        sesionRepository.deleteAll();
        miembroGrupoRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        Role rolAdmin = roleRepository.save(new Role(null, "ADMINISTRADOR"));
        Role rolMiembro = roleRepository.save(new Role(null, "MIEMBRO"));

        admin = usuarioRepository.save(crearUsuario("Admin", "admin@test.com"));
        miembro = usuarioRepository.save(crearUsuario("Miembro", "miembro@test.com"));
        miembroSinTareas = usuarioRepository.save(crearUsuario("Libre", "libre@test.com"));

        grupo = grupoRepository.save(crearGrupo("Casa Test", "INV999"));

        miembroGrupoRepository.save(crearMiembro(admin, grupo, rolAdmin));
        miembroGrupoRepository.save(crearMiembro(miembro, grupo, rolMiembro));
        miembroGrupoRepository.save(crearMiembro(miembroSinTareas, grupo, rolMiembro));

        tareaRepository.save(crearTarea("Limpiar cocina", grupo, miembro,
                PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusHours(5)));

        sesionRepository.save(crearSesion(admin, "token-admin", LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(miembro, "token-miembro", LocalDateTime.now().plusHours(2)));
    }

    @Test
    void eliminarMiembro_caminoFeliz_retornaOk() throws Exception {
        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembroSinTareas.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(miembroSinTareas.getIdUsuario()))
                .andExpect(jsonPath("$.idGrupo").value(grupo.getIdGrupo()))
                .andExpect(jsonPath("$.mensaje").value("Miembro eliminado correctamente"));

        assertTrue(miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(miembroSinTareas.getIdUsuario(), grupo.getIdGrupo())
                .isEmpty(), "El registro de miembros_grupo debe haber sido eliminado de la BD");
    }

    @Test
    void eliminarMiembro_miembroConSoloTareasCompletadas_retornaOkYMarcaExMiembro() throws Exception {
        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembro.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Miembro eliminado correctamente"));

        List<Tarea> tareas = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndEstado(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), EstadoTarea.COMPLETADA);
        tareas.forEach(t -> assertFalse(!t.isExMiembro(),
                "La tarea completada debe tener exMiembro=true"));
    }

    @Test
    void eliminarMiembro_solicitanteNoEsAdmin_retornaForbidden() throws Exception {
        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembroSinTareas.getIdUsuario())
                        .header("Authorization", "Bearer token-miembro"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje")
                        .value("No tienes permisos para eliminar miembros del grupo"));
    }

    @Test
    void eliminarMiembro_miembroConTareaPendiente_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("Barrer sala", grupo, miembro,
                PrioridadTarea.ALTA, EstadoTarea.PENDIENTE, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembro.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El miembro tiene tareas activas o vencidas que deben resolverse antes de ser removido del grupo"));
    }

    @Test
    void eliminarMiembro_miembroConTareaEnProgreso_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("Cocinar", grupo, miembro,
                PrioridadTarea.MEDIA, EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusHours(3)));

        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembro.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El miembro tiene tareas activas o vencidas que deben resolverse antes de ser removido del grupo"));
    }

    @Test
    void eliminarMiembro_miembroConTareaVencida_retornaConflict() throws Exception {
        tareaRepository.save(crearTarea("Tarea vencida", grupo, miembro,
                PrioridadTarea.BAJA, EstadoTarea.VENCIDA, LocalDateTime.now().minusHours(1)));

        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembro.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El miembro tiene tareas activas o vencidas que deben resolverse antes de ser removido del grupo"));
    }

    @Test
    void eliminarMiembro_miembroInexistente_retornaNotFound() throws Exception {
        Long idInexistente = 99999L;

        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), idInexistente)
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Miembro no encontrado en el grupo"));
    }

    @Test
    void reingreso_revierteExMiembro_enTareasCompletadas() throws Exception {
        // Eliminar miembro (sus tareas COMPLETADAS quedan con exMiembro=true)
        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembro.getIdUsuario())
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk());

        // Verificar que la tarea quedó marcada
        List<Tarea> tareasAntes = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), true);
        assertFalse(tareasAntes.isEmpty(), "Debe haber tareas marcadas como ex_miembro=true");

        // Reingreso: unirse con código de invitación
        String bodyJson = objectMapper.writeValueAsString(
                java.util.Map.of("codigoInvitacion", grupo.getCodigoInvitacion()));

        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-miembro")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rolNombre").value("MIEMBRO"));

        // Verificar que exMiembro fue revertido a false
        List<Tarea> tareasExMiembro = tareaRepository
                .findByGrupoIdGrupoAndUsuarioAsignadoIdUsuarioAndExMiembro(
                        grupo.getIdGrupo(), miembro.getIdUsuario(), true);
        assertFalse(!tareasExMiembro.isEmpty(),
                "Todas las tareas deben tener exMiembro=false tras el reingreso");
    }

    @Test
    void eliminarMiembro_sinHeaderAuthorization_retornaUnauthorized() throws Exception {
        mockMvc.perform(delete("/grupos/{idGrupo}/miembros/{idMiembro}",
                        grupo.getIdGrupo(), miembroSinTareas.getIdUsuario()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token de autorizacion es obligatorio"));
    }

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
