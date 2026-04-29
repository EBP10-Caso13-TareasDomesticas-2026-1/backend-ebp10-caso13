package com.tareasdomesticas.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
import com.tareasdomesticas.backend.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MiembroGrupoControllerIntegrationTest {

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
    private BCryptPasswordEncoder passwordEncoder;

    private Usuario usuarioLibre;
    private Usuario usuarioYaEnGrupo;
    private Grupo grupo;

    @BeforeEach
    void prepararDatos() {
        miembroGrupoRepository.deleteAll();
        sesionRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        Role rolMiembro = roleRepository.save(new Role(null, "MIEMBRO"));

        usuarioLibre = usuarioRepository.save(crearUsuario("Ana", "ana@test.com"));
        usuarioYaEnGrupo = usuarioRepository.save(crearUsuario("Luis", "luis@test.com"));

        grupo = grupoRepository.save(crearGrupo("Casa Test", "INV123"));

        miembroGrupoRepository.save(crearMiembro(usuarioYaEnGrupo, grupo, rolMiembro));

        sesionRepository.save(crearSesion(usuarioLibre, "token-libre", LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(usuarioYaEnGrupo, "token-ya-en-grupo", LocalDateTime.now().plusHours(2)));
        sesionRepository.save(crearSesion(usuarioLibre, "token-expirado",
                LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void unirseAGrupo_caminoFeliz_retornaCreated() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-libre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("INV123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMiembroGrupo").isNumber())
                .andExpect(jsonPath("$.idUsuario").value(usuarioLibre.getIdUsuario()))
                .andExpect(jsonPath("$.idGrupo").value(grupo.getIdGrupo()))
                .andExpect(jsonPath("$.nombreGrupo").value("Casa Test"))
                .andExpect(jsonPath("$.rolNombre").value("MIEMBRO"))
                .andExpect(jsonPath("$.fechaUnion").isNotEmpty())
                .andExpect(jsonPath("$.mensaje").value("Te has unido al grupo exitosamente"));
    }

    @Test
    void unirseAGrupo_sinHeaderAuthorization_retornaUnauthorized() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("INV123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token de autorizacion es obligatorio"));
    }

    @Test
    void unirseAGrupo_tokenInvalido_retornaUnauthorized() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-inexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("INV123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token de sesion invalido"));
    }

    @Test
    void unirseAGrupo_tokenExpirado_retornaUnauthorized() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-expirado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("INV123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La sesion ha expirado"));
    }

    @Test
    void unirseAGrupo_usuarioYaEnGrupo_retornaConflict() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-ya-en-grupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("INV123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El usuario ya pertenece a un grupo"));
    }

    @Test
    void unirseAGrupo_codigoInvalido_retornaNotFound() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-libre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("NOEXIS"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Codigo de invitacion invalido"));
    }

    @Test
    void unirseAGrupo_codigoEnBlanco_retornaBadRequest() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-libre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigoInvitacion")
                        .value("El codigo de invitacion es obligatorio"));
    }

    @Test
    void unirseAGrupo_codigoAusente_retornaBadRequest() throws Exception {
        mockMvc.perform(post("/miembros-grupo")
                        .header("Authorization", "Bearer token-libre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigoInvitacion")
                        .value("El codigo de invitacion es obligatorio"));
    }

    private Map<String, String> body(String codigoInvitacion) {
        Map<String, String> b = new HashMap<>();
        b.put("codigoInvitacion", codigoInvitacion);
        return b;
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
