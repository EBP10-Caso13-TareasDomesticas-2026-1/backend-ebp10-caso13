package com.tareasdomesticas.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareasdomesticas.backend.entity.*;
import com.tareasdomesticas.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GrupoRankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private MiembroGrupoRepository miembroGrupoRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private SesionRepository sesionRepository;

    private Grupo grupo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        tareaRepository.deleteAll();
        miembroGrupoRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        Role miembroRole = roleRepository.save(new Role(null, "MIEMBRO"));
        Role adminRole = roleRepository.save(new Role(null, "ADMINISTRADOR"));

        Usuario u1 = new Usuario(); u1.setNombre("Ana"); u1.setCorreo("ana@example.com"); u1.setContrasenaHash(passwordEncoder.encode("pass1")); u1.setPinSeguridadHash(passwordEncoder.encode("00000")); u1.setCreadoEn(LocalDateTime.now()); u1 = usuarioRepository.save(u1);
        Usuario u2 = new Usuario(); u2.setNombre("Bea"); u2.setCorreo("bea@example.com"); u2.setContrasenaHash(passwordEncoder.encode("pass2")); u2.setPinSeguridadHash(passwordEncoder.encode("00000")); u2.setCreadoEn(LocalDateTime.now()); u2 = usuarioRepository.save(u2);
        Usuario u3 = new Usuario(); u3.setNombre("Carlos"); u3.setCorreo("carlos@example.com"); u3.setContrasenaHash(passwordEncoder.encode("pass3")); u3.setPinSeguridadHash(passwordEncoder.encode("00000")); u3.setCreadoEn(LocalDateTime.now()); u3 = usuarioRepository.save(u3);
        Usuario u4 = new Usuario(); u4.setNombre("Diego"); u4.setCorreo("diego@example.com"); u4.setContrasenaHash(passwordEncoder.encode("pass4")); u4.setPinSeguridadHash(passwordEncoder.encode("00000")); u4.setCreadoEn(LocalDateTime.now()); u4 = usuarioRepository.save(u4);

        Grupo g = new Grupo();
        g.setNombre("CasaTest");
        g.setCodigoInvitacion("ABC123");
        g.setCreadoEn(LocalDateTime.now());
        grupo = grupoRepository.save(g);

        MiembroGrupo m1 = new MiembroGrupo(); m1.setUsuario(u1); m1.setGrupo(grupo); m1.setRol(adminRole); m1.setFechaUnion(LocalDateTime.now()); m1.setPuntos(30); miembroGrupoRepository.save(m1);
        MiembroGrupo m2 = new MiembroGrupo(); m2.setUsuario(u2); m2.setGrupo(grupo); m2.setRol(miembroRole); m2.setFechaUnion(LocalDateTime.now()); m2.setPuntos(20); miembroGrupoRepository.save(m2);
        MiembroGrupo m3 = new MiembroGrupo(); m3.setUsuario(u3); m3.setGrupo(grupo); m3.setRol(miembroRole); m3.setFechaUnion(LocalDateTime.now()); m3.setPuntos(20); miembroGrupoRepository.save(m3);
        MiembroGrupo m4 = new MiembroGrupo(); m4.setUsuario(u4); m4.setGrupo(grupo); m4.setRol(miembroRole); m4.setFechaUnion(LocalDateTime.now()); m4.setPuntos(5); miembroGrupoRepository.save(m4);

        // completar tareas para cada usuario
        tareaRepository.save(new Tarea(null, grupo, u1, "T1", null, PrioridadTarea.ALTA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusDays(1), LocalDateTime.now()));
        tareaRepository.save(new Tarea(null, grupo, u1, "T2", null, PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        tareaRepository.save(new Tarea(null, grupo, u2, "T3", null, PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        tareaRepository.save(new Tarea(null, grupo, u3, "T4", null, PrioridadTarea.MEDIA, EstadoTarea.COMPLETADA, LocalDateTime.now().plusDays(1), LocalDateTime.now()));
        
        // crear sesion para u1 (admin) para autorización en MockMvc
        Sesion s = new Sesion();
        s.setUsuario(u1);
        s.setToken("token-admin");
        s.setCreadaEn(LocalDateTime.now());
        s.setExpiraEn(LocalDateTime.now().plusHours(2));
        sesionRepository.save(s);
        
    }

    @Test
    void visualizarRanking_exito_muestraListaConDatos() throws Exception {
        mockMvc.perform(get("/grupos/" + grupo.getIdGrupo() + "/ranking")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking").isArray())
                .andExpect(jsonPath("$.ranking", hasSize(4)))
                .andExpect(jsonPath("$.ranking[0].nombre").value("Ana"))
                .andExpect(jsonPath("$.ranking[0].puntos").value(30))
                .andExpect(jsonPath("$.ranking[0].tareasCompletadas").value(2));
    }

    @Test
    void ordenarDescendente_yEmpates_posicionesCorrectas() throws Exception {
        mockMvc.perform(get("/grupos/" + grupo.getIdGrupo() + "/ranking")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking[0].posicion").value(1))
                .andExpect(jsonPath("$.ranking[1].posicion").value(2))
                .andExpect(jsonPath("$.ranking[2].posicion").value(2))
                .andExpect(jsonPath("$.ranking[1].nombre").value("Bea"))
                .andExpect(jsonPath("$.ranking[2].nombre").value("Carlos"));
    }

    @Test
    void sinTareasCompletadas_muestraMensaje() throws Exception {
        // limpiar tareas
        tareaRepository.deleteAll();

        mockMvc.perform(get("/grupos/" + grupo.getIdGrupo() + "/ranking")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("No hay tareas completadas en el grupo"));
    }
}
