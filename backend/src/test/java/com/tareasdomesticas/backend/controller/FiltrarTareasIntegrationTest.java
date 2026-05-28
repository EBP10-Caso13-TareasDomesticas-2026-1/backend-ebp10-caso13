package com.tareasdomesticas.backend.controller;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

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
class FiltrarTareasIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private MiembroGrupoRepository miembroGrupoRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private TareaRepository tareaRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario miembro1;
    private Usuario miembro2;
    private Grupo grupo;
    private Grupo otroGrupo;

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

        admin = usuarioRepository.save(crearUsuario("Admin", "admin@filtrar.com"));
        miembro1 = usuarioRepository.save(crearUsuario("Miembro1", "m1@filtrar.com"));
        miembro2 = usuarioRepository.save(crearUsuario("Miembro2", "m2@filtrar.com"));

        grupo = grupoRepository.save(crearGrupo("Casa Filtro", "FLT001"));
        otroGrupo = grupoRepository.save(crearGrupo("Otro Grupo", "FLT002"));

        miembroGrupoRepository.save(crearMiembro(admin, grupo, rolAdmin));
        miembroGrupoRepository.save(crearMiembro(miembro1, grupo, rolMiembro));
        miembroGrupoRepository.save(crearMiembro(miembro2, grupo, rolMiembro));
        miembroGrupoRepository.save(crearMiembro(admin, otroGrupo, rolAdmin));

        // Tareas del grupo principal
        tareaRepository.save(crearTarea("Pendiente-Alta-M1",    grupo, miembro1, PrioridadTarea.ALTA,  EstadoTarea.PENDIENTE,   LocalDateTime.now().plusHours(3)));
        tareaRepository.save(crearTarea("Pendiente-Media-M1",   grupo, miembro1, PrioridadTarea.MEDIA, EstadoTarea.PENDIENTE,   LocalDateTime.now().plusHours(4)));
        tareaRepository.save(crearTarea("EnProgreso-Alta-M1",   grupo, miembro1, PrioridadTarea.ALTA,  EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusHours(5)));
        tareaRepository.save(crearTarea("Completada-Baja-M2",   grupo, miembro2, PrioridadTarea.BAJA,  EstadoTarea.COMPLETADA,  LocalDateTime.now().plusHours(2)));
        tareaRepository.save(crearTarea("Pendiente-Alta-M2",    grupo, miembro2, PrioridadTarea.ALTA,  EstadoTarea.PENDIENTE,   LocalDateTime.now().plusHours(6)));

        sesionRepository.save(crearSesion(admin,    "token-admin-filtrar",   LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(miembro1, "token-m1-filtrar",      LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(miembro2, "token-m2-filtrar",      LocalDateTime.now().plusHours(2)));
    }

    @Test
    void filtrar_porUnEstado_retornaSoloTareasConEseEstado() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("estados", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(3)))
                .andExpect(jsonPath("$.tareas[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.filtrosAplicados.estados[0]").value("PENDIENTE"));
    }

    @Test
    void filtrar_porVariosEstados_retornaTareasDeAmbos() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("estados", "PENDIENTE", "EN_PROGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(4)));
    }

    @Test
    void filtrar_porPrioridad_retornaSoloPorPrioridad() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("prioridades", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(3)))
                .andExpect(jsonPath("$.filtrosAplicados.prioridades[0]").value("ALTA"));
    }

    @Test
    void filtrar_porMiembro_retornaSoloPorMiembro() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("idsMiembros", String.valueOf(miembro1.getIdUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(3)))
                .andExpect(jsonPath("$.filtrosAplicados.idsMiembros[0]").value(miembro1.getIdUsuario()));
    }

    @Test
    void filtrar_combinandoEstadoPrioridadMiembro_aplicaAnd() throws Exception {
        // PENDIENTE + ALTA + miembro1 → solo "Pendiente-Alta-M1"
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("estados", "PENDIENTE")
                        .param("prioridades", "ALTA")
                        .param("idsMiembros", String.valueOf(miembro1.getIdUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(1)))
                .andExpect(jsonPath("$.tareas[0].nombre").value("Pendiente-Alta-M1"));
    }

    @Test
    void filtrar_sinFiltros_retornaTodasLasTareasDelGrupo() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(5)));
    }

    @Test
    void filtrar_sinCoincidencias_retornaListaVaciaConMensaje() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo())
                        .header("Authorization", "Bearer token-admin-filtrar")
                        .param("estados", "VENCIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tareas", hasSize(0)))
                .andExpect(jsonPath("$.mensaje")
                        .value("No hay tareas que coincidan con los filtros seleccionados"));
    }

    @Test
    void filtrar_usuarioNoPertenece_retornaForbidden() throws Exception {
        // miembro1 pertenece a grupo, NO a otroGrupo
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", otroGrupo.getIdGrupo())
                        .header("Authorization", "Bearer token-m1-filtrar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No pertenece a ese grupo"));
    }

    @Test
    void filtrar_sinToken_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/tareas/grupo/{idGrupo}/filtrar", grupo.getIdGrupo()))
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
