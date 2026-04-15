package com.tareasdomesticas.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Sesion;
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
class TareaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

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

    private Usuario adminUsuario;
    private Usuario miembroUsuario;
    private Usuario otroGrupoUsuario;
    private Grupo grupoPrincipal;

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

        adminUsuario = usuarioRepository.save(crearUsuario("Admin", "admin@test.com"));
        miembroUsuario = usuarioRepository.save(crearUsuario("Miembro", "miembro@test.com"));
        otroGrupoUsuario = usuarioRepository.save(crearUsuario("Otro", "otro@test.com"));

        grupoPrincipal = grupoRepository.save(crearGrupo("Casa 1", "ABC123"));
        Grupo grupoSecundario = grupoRepository.save(crearGrupo("Casa 2", "XYZ789"));

        miembroGrupoRepository.save(crearMiembro(adminUsuario, grupoPrincipal, adminRole));
        miembroGrupoRepository.save(crearMiembro(miembroUsuario, grupoPrincipal, miembroRole));
        miembroGrupoRepository.save(crearMiembro(otroGrupoUsuario, grupoSecundario, miembroRole));

        sesionRepository.save(crearSesion(adminUsuario, "token-admin", LocalDateTime.now().plusHours(2), null));
        sesionRepository.save(crearSesion(miembroUsuario, "token-miembro", LocalDateTime.now().plusHours(2), null));
    }

    @Test
    void crearTarea_caminoFeliz_retornaCreated() throws Exception {
        Map<String, Object> body = crearBody(
                "Lavar platos",
                "Cocina completa",
                miembroUsuario.getIdUsuario(),
                "ALTA",
                LocalDateTime.now().plusHours(3)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lavar platos"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.prioridad").value("ALTA"))
                .andExpect(jsonPath("$.idGrupo").value(grupoPrincipal.getIdGrupo()));
    }

    @Test
    void crearTarea_adminAutoasignado_retornaCreated() throws Exception {
        Map<String, Object> body = crearBody(
                "Revisar gastos",
                "",
                adminUsuario.getIdUsuario(),
                "MEDIA",
                LocalDateTime.now().plusHours(4)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuarioAsignado").value(adminUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void crearTarea_sinDescripcion_ySinPrioridad_retornaCreatedConPrioridadMedia() throws Exception {
        Map<String, Object> body = crearBody(
                "Ordenar sala",
                null,
                miembroUsuario.getIdUsuario(),
                null,
                LocalDateTime.now().plusHours(5)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridad").value("MEDIA"))
                .andExpect(jsonPath("$.descripcion").value(nullValue()));
    }

    @Test
    void crearTarea_camposObligatoriosVacios_retornaBadRequest() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "");
        body.put("idUsuarioAsignado", miembroUsuario.getIdUsuario());
        body.put("fechaLimite", LocalDateTime.now().plusHours(3).toString());

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"));
    }

    @Test
    void crearTarea_sinMiembroAsignado_retornaBadRequest() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Limpiar cocina");
        body.put("fechaLimite", LocalDateTime.now().plusHours(3).toString());

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.idUsuarioAsignado")
                        .value("El miembro asignado es obligatorio"));
    }

    @Test
    void crearTarea_sinFechaLimite_retornaBadRequest() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Limpiar cocina");
        body.put("idUsuarioAsignado", miembroUsuario.getIdUsuario());

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaLimite")
                        .value("La fecha y hora limite es obligatoria"));
    }

    @Test
    void crearTarea_fechaLimitePasada_retornaBadRequest() throws Exception {
        Map<String, Object> body = crearBody(
                "Sacar basura",
                "",
                miembroUsuario.getIdUsuario(),
                "BAJA",
                LocalDateTime.now().minusMinutes(1)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaLimite")
                        .value("La fecha y hora limite deben ser posteriores al momento actual"));
    }

    @Test
    void crearTarea_nombreMayorA50_retornaBadRequest() throws Exception {
        Map<String, Object> body = crearBody(
                "123456789012345678901234567890123456789012345678901",
                "",
                miembroUsuario.getIdUsuario(),
                "MEDIA",
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre")
                        .value("El nombre no puede superar los 50 caracteres"));
    }

    @Test
    void crearTarea_descripcionMayorA180_retornaBadRequest() throws Exception {
        String descripcionLarga = "x".repeat(181);
        Map<String, Object> body = crearBody(
                "Tender cama",
                descripcionLarga,
                miembroUsuario.getIdUsuario(),
                "ALTA",
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.descripcion")
                        .value("La descripcion no puede superar los 180 caracteres"));
    }

    @Test
    void crearTarea_usuarioNoAdmin_retornaForbidden() throws Exception {
        Map<String, Object> body = crearBody(
                "Limpiar patio",
                "",
                adminUsuario.getIdUsuario(),
                "MEDIA",
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-miembro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo un administrador puede crear tareas"));
    }

    @Test
    void crearTarea_usuarioAsignadoNoPerteneceAlGrupo_retornaBadRequest() throws Exception {
        Map<String, Object> body = crearBody(
                "Limpiar ventanas",
                "",
                otroGrupoUsuario.getIdUsuario(),
                "MEDIA",
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario asignado no pertenece al grupo del administrador"));
    }

    @Test
    void crearTarea_tokenInvalido_retornaUnauthorized() throws Exception {
        Map<String, Object> body = crearBody(
                "Limpiar mesa",
                "",
                miembroUsuario.getIdUsuario(),
                "MEDIA",
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/tareas")
                        .header("Authorization", "Bearer token-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token de sesion invalido"));
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

    private Map<String, Object> crearBody(String nombre,
                                          String descripcion,
                                          Long idUsuarioAsignado,
                                          String prioridad,
                                          LocalDateTime fechaLimite) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        body.put("idUsuarioAsignado", idUsuarioAsignado);
        body.put("prioridad", prioridad);
                body.put("fechaLimite", fechaLimite.toString());
        return body;
    }
}
