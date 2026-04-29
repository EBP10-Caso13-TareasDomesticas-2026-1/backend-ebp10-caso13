package com.tareasdomesticas.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tareasdomesticas.backend.dto.*;
import com.tareasdomesticas.backend.entity.*;
import com.tareasdomesticas.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TareaE2ETest {

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

        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void prepararDatos() {
        tareaRepository.deleteAll();
        sesionRepository.deleteAll();
        miembroGrupoRepository.deleteAll();
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(new Role(null, "ADMINISTRADOR"));
        roleRepository.save(new Role(null, "MIEMBRO"));
    }

    @Test
    void testFlujoCompletoCrearTarea() throws Exception {
        String adminCorreo = "admin.e2e@test.com";
        String miembroCorreo = "miembro.e2e@test.com";

        RegistroUsuarioRequest registroAdmin = new RegistroUsuarioRequest(
                "Admin E2E",
                adminCorreo,
                "Admin1234",
                "1234"
        );

        String registroAdminJson = postJson("/usuarios/registro", registroAdmin, null, 201);

        RegistroUsuarioResponse adminCreado =
                objectMapper.readValue(registroAdminJson, RegistroUsuarioResponse.class);
        assertThat(adminCreado.getIdUsuario()).isNotNull();

        InicioSesionRequest loginAdmin = new InicioSesionRequest(adminCorreo, "Admin1234");
        String loginAdminJson = postJson("/usuarios/login", loginAdmin, null, 200);

        InicioSesionResponse loginPayload =
                objectMapper.readValue(loginAdminJson, InicioSesionResponse.class);
        assertThat(loginPayload.getToken()).isNotBlank();

        String bearerToken = "Bearer " + loginPayload.getToken();

        CrearGrupoRequest crearGrupoRequest = new CrearGrupoRequest("Hogar E2E", adminCreado.getIdUsuario());
        String crearGrupoJson = postJson("/grupos", crearGrupoRequest, bearerToken, 201);

        JsonNode grupoNode = objectMapper.readTree(crearGrupoJson);
        Long idGrupoCreado = grupoNode.get("idGrupo").asLong();
        String codigoInvitacion = grupoNode.get("codigoInvitacion").asText();
        assertThat(idGrupoCreado).isNotNull();
        assertThat(codigoInvitacion).isNotBlank();

        RegistroUsuarioRequest registroMiembro = new RegistroUsuarioRequest(
                "Miembro E2E",
                miembroCorreo,
                "Miembro1234",
                "5678"
        );

        String registroMiembroJson = postJson("/usuarios/registro", registroMiembro, null, 201);

        RegistroUsuarioResponse miembroCreado =
                objectMapper.readValue(registroMiembroJson, RegistroUsuarioResponse.class);
        assertThat(miembroCreado.getIdUsuario()).isNotNull();

        InicioSesionRequest loginMiembro = new InicioSesionRequest(miembroCorreo, "Miembro1234");
        String loginMiembroJson = postJson("/usuarios/login", loginMiembro, null, 200);
        InicioSesionResponse loginMiembroPayload =
                objectMapper.readValue(loginMiembroJson, InicioSesionResponse.class);
        String bearerTokenMiembro = "Bearer " + loginMiembroPayload.getToken();

        UnirseGrupoRequest unirseGrupoRequest = new UnirseGrupoRequest(codigoInvitacion);

        String unirseGrupoJson = postJson("/miembros-grupo", unirseGrupoRequest, bearerTokenMiembro, 201);

        JsonNode miembroGrupoNode = objectMapper.readTree(unirseGrupoJson);
        assertThat(miembroGrupoNode.get("idMiembroGrupo").asLong()).isNotNull();

        CrearTareaRequest crearTareaRequest = new CrearTareaRequest();
        crearTareaRequest.setNombre("Lavar los platos");
        crearTareaRequest.setDescripcion("Usar detergente y esponja");
        crearTareaRequest.setIdUsuarioAsignado(miembroCreado.getIdUsuario());
        crearTareaRequest.setFechaLimite(LocalDateTime.now().plusHours(4));

        ObjectNode crearTareaBody = objectMapper.createObjectNode();
        crearTareaBody.put("nombre", crearTareaRequest.getNombre());
        crearTareaBody.put("descripcion", crearTareaRequest.getDescripcion());
        crearTareaBody.put("idUsuarioAsignado", crearTareaRequest.getIdUsuarioAsignado());
        crearTareaBody.put("fechaLimite", crearTareaRequest.getFechaLimite().toString());

        String crearTareaJson = postJson("/tareas", crearTareaBody, bearerToken, 201);

        JsonNode tareaNode = objectMapper.readTree(crearTareaJson);
        Long idTarea = tareaNode.get("idTarea").asLong();
        assertThat(idTarea).isNotNull();
        assertThat(tareaNode.get("estado").asText()).isEqualTo(EstadoTarea.PENDIENTE.name());
        assertThat(tareaNode.get("prioridad").asText()).isEqualTo(PrioridadTarea.MEDIA.name());

        Tarea tareaPersistida = tareaRepository.findById(idTarea).orElse(null);
        assertThat(tareaPersistida).isNotNull();
        assertThat(tareaPersistida.getGrupo().getIdGrupo()).isEqualTo(idGrupoCreado);
        assertThat(tareaPersistida.getUsuarioAsignado().getIdUsuario()).isEqualTo(miembroCreado.getIdUsuario());
        assertThat(tareaPersistida.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);

        MiembroGrupo miembroAsignadoEnGrupo = miembroGrupoRepository
                .findByUsuarioIdUsuarioAndGrupoIdGrupo(miembroCreado.getIdUsuario(), idGrupoCreado)
                .orElse(null);
        assertThat(miembroAsignadoEnGrupo).isNotNull();
    }

        private String postJson(String path, Object body, String authorizationHeader, int expectedStatus) throws Exception {
                var requestBuilder = post(path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body));

                if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        requestBuilder.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
                }

                MvcResult result = mockMvc.perform(requestBuilder)
                                .andExpect(status().is(expectedStatus))
                                .andReturn();

                return result.getResponse().getContentAsString();
    }
}
