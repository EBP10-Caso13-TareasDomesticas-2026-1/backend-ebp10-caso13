package com.tareasdomesticas.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.dto.EditarTareaRequest;
import com.tareasdomesticas.backend.dto.EditarTareaResponse;
import com.tareasdomesticas.backend.entity.EstadoTarea;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.PrioridadTarea;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Tarea;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import com.tareasdomesticas.backend.repository.TareaRepository;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private SesionService sesionService;

    @InjectMocks
    private TareaService tareaService;

        private Tarea tareaPendiente;
        private Tarea tareaEnProgreso;
        private Tarea tareaVencida;
        private Tarea tareaCompletada;

    private Usuario admin;
    private Usuario miembro;
    private Grupo grupo;
    private Role rolAdmin;
    private Role rolMiembro;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setIdUsuario(1L);
        admin.setNombre("Admin");

        miembro = new Usuario();
        miembro.setIdUsuario(2L);
        miembro.setNombre("Miembro");

        grupo = new Grupo();
        grupo.setIdGrupo(10L);
        grupo.setNombre("Casa");

        rolAdmin = new Role();
        rolAdmin.setNombre("ADMINISTRADOR");

        rolMiembro = new Role();
        rolMiembro.setNombre("MIEMBRO");

                tareaPendiente = crearTareaBase(100L, EstadoTarea.PENDIENTE, LocalDateTime.now().plusDays(1));
                tareaEnProgreso = crearTareaBase(101L, EstadoTarea.EN_PROGRESO, LocalDateTime.now().plusDays(1));
                tareaVencida = crearTareaBase(102L, EstadoTarea.VENCIDA, LocalDateTime.now().minusDays(1));
                tareaCompletada = crearTareaBase(103L, EstadoTarea.COMPLETADA, LocalDateTime.now().plusDays(1));
    }

    @Test
    void crearTarea_caminoFeliz_creaPendienteYDevuelveResponse() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Lavar platos",
                "Lavar antes de las 8 pm",
                miembro.getIdUsuario(),
                PrioridadTarea.ALTA,
                LocalDateTime.now().plusHours(2)
        );

        MiembroGrupo adminEnGrupo = new MiembroGrupo();
        adminEnGrupo.setUsuario(admin);
        adminEnGrupo.setGrupo(grupo);
        adminEnGrupo.setRol(rolAdmin);

        MiembroGrupo asignadoEnGrupo = new MiembroGrupo();
        asignadoEnGrupo.setUsuario(miembro);
        asignadoEnGrupo.setGrupo(grupo);
        asignadoEnGrupo.setRol(rolMiembro);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(admin.getIdUsuario())).thenReturn(Optional.of(adminEnGrupo));
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(miembro.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(asignadoEnGrupo));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> {
            Tarea tarea = invocation.getArgument(0);
            tarea.setIdTarea(100L);
            return tarea;
        });

        CrearTareaResponse response = tareaService.crearTarea("Bearer token", request);

        assertEquals(100L, response.getIdTarea());
        assertEquals("Lavar platos", response.getNombre());
        assertEquals(PrioridadTarea.ALTA, response.getPrioridad());
        assertEquals(EstadoTarea.PENDIENTE, response.getEstado());
        assertEquals(miembro.getIdUsuario(), response.getIdUsuarioAsignado());
    }

    @Test
    void crearTarea_sinPrioridadYDescripcionVacia_aplicaDefaultMediaYDescripcionNull() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Barrer sala",
                "   ",
                admin.getIdUsuario(),
                null,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo adminEnGrupo = new MiembroGrupo();
        adminEnGrupo.setUsuario(admin);
        adminEnGrupo.setGrupo(grupo);
        adminEnGrupo.setRol(rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(admin.getIdUsuario())).thenReturn(Optional.of(adminEnGrupo));
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> {
            Tarea tarea = invocation.getArgument(0);
            tarea.setIdTarea(101L);
            return tarea;
        });

        CrearTareaResponse response = tareaService.crearTarea("Bearer token", request);

        assertEquals(PrioridadTarea.MEDIA, response.getPrioridad());
        assertNull(response.getDescripcion());
        assertEquals(EstadoTarea.PENDIENTE, response.getEstado());
    }

    @Test
    void crearTarea_usuarioNoAdministrador_retornaForbidden() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tender cama",
                null,
                miembro.getIdUsuario(),
                PrioridadTarea.BAJA,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo miembroNoAdmin = new MiembroGrupo();
        miembroNoAdmin.setUsuario(admin);
        miembroNoAdmin.setGrupo(grupo);
        miembroNoAdmin.setRol(rolMiembro);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(admin.getIdUsuario())).thenReturn(Optional.of(miembroNoAdmin));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.crearTarea("Bearer token", request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("Solo un administrador puede crear tareas", ex.getMessage());
    }

    @Test
    void crearTarea_usuarioAsignadoFueraGrupo_retornaBadRequest() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Sacar basura",
                null,
                miembro.getIdUsuario(),
                PrioridadTarea.MEDIA,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo adminEnGrupo = new MiembroGrupo();
        adminEnGrupo.setUsuario(admin);
        adminEnGrupo.setGrupo(grupo);
        adminEnGrupo.setRol(rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(admin.getIdUsuario())).thenReturn(Optional.of(adminEnGrupo));
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(miembro.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.crearTarea("Bearer token", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void crearTarea_fechaPasada_retornaBadRequest() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Limpiar cocina",
                null,
                miembro.getIdUsuario(),
                PrioridadTarea.MEDIA,
                LocalDateTime.now().minusMinutes(5)
        );

        MiembroGrupo adminEnGrupo = new MiembroGrupo();
        adminEnGrupo.setUsuario(admin);
        adminEnGrupo.setGrupo(grupo);
        adminEnGrupo.setRol(rolAdmin);

        MiembroGrupo asignadoEnGrupo = new MiembroGrupo();
        asignadoEnGrupo.setUsuario(miembro);
        asignadoEnGrupo.setGrupo(grupo);
        asignadoEnGrupo.setRol(rolMiembro);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(admin.getIdUsuario())).thenReturn(Optional.of(adminEnGrupo));
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(miembro.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(asignadoEnGrupo));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.crearTarea("Bearer token", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("La fecha y hora limite deben ser posteriores al momento actual", ex.getMessage());
    }

    @Test
    void editarTarea_pendiente_actualizaCamposYMantieneEstado() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar platos y vasos",
                "Incluir cubiertos",
                PrioridadTarea.MEDIA,
                LocalDateTime.now().plusHours(5)
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaPendiente.getIdTarea())).thenReturn(Optional.of(tareaPendiente));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarTareaResponse response = tareaService.editarTarea("Bearer token", tareaPendiente.getIdTarea(), request);

        assertEquals(tareaPendiente.getIdTarea(), response.getIdTarea());
        assertEquals("Lavar platos y vasos", response.getNombre());
        assertEquals(PrioridadTarea.MEDIA, response.getPrioridad());
        assertEquals(EstadoTarea.PENDIENTE, response.getEstado());
        assertEquals(request.getFechaLimite(), response.getFechaLimite());
    }

    @Test
    void editarTarea_enProgreso_actualizaCamposYMantieneEstado() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Ordenar cocina",
                "Mover utensilios",
                PrioridadTarea.ALTA,
                LocalDateTime.now().plusHours(6)
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaEnProgreso.getIdTarea())).thenReturn(Optional.of(tareaEnProgreso));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarTareaResponse response = tareaService.editarTarea("Bearer token", tareaEnProgreso.getIdTarea(), request);

        assertEquals(EstadoTarea.EN_PROGRESO, response.getEstado());
        assertEquals(PrioridadTarea.ALTA, response.getPrioridad());
        assertEquals("Ordenar cocina", response.getNombre());
    }

    @Test
    void editarTarea_vencida_permiteCamposSinCambiarFechaLimite() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar patio",
                "Con manguera",
                PrioridadTarea.BAJA,
                null
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaVencida.getIdTarea())).thenReturn(Optional.of(tareaVencida));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarTareaResponse response = tareaService.editarTarea("Bearer token", tareaVencida.getIdTarea(), request);

        assertEquals(EstadoTarea.VENCIDA, response.getEstado());
        assertEquals(tareaVencida.getFechaLimite(), response.getFechaLimite());
        assertEquals("Lavar patio", response.getNombre());
    }

    @Test
    void editarTarea_vencida_conFechaDistinta_retornaBadRequest() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar patio",
                null,
                PrioridadTarea.BAJA,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaVencida.getIdTarea())).thenReturn(Optional.of(tareaVencida));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.editarTarea("Bearer token", tareaVencida.getIdTarea(), request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("La fecha limite no puede modificarse en una tarea vencida", ex.getMessage());
    }

    @Test
    void editarTarea_completada_retornaForbidden() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar patio",
                null,
                PrioridadTarea.BAJA,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaCompletada.getIdTarea())).thenReturn(Optional.of(tareaCompletada));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.editarTarea("Bearer token", tareaCompletada.getIdTarea(), request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("No se puede editar una tarea completada", ex.getMessage());
    }

    @Test
    void editarTarea_miembroNoAdmin_retornaForbidden() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar patio",
                null,
                PrioridadTarea.BAJA,
                LocalDateTime.now().plusHours(1)
        );

        MiembroGrupo miembroEnGrupo = crearMiembro(miembro, grupo, rolMiembro);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(miembro);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(miembro.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(miembroEnGrupo));
        when(tareaRepository.findById(tareaPendiente.getIdTarea())).thenReturn(Optional.of(tareaPendiente));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.editarTarea("Bearer token", tareaPendiente.getIdTarea(), request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("Solo un administrador puede editar tareas", ex.getMessage());
    }

    @Test
    void editarTarea_fechaPasadaEnEditable_retornaBadRequest() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar patio",
                null,
                PrioridadTarea.BAJA,
                LocalDateTime.now().minusMinutes(1)
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaPendiente.getIdTarea())).thenReturn(Optional.of(tareaPendiente));

        ApiException ex = assertThrows(ApiException.class,
                () -> tareaService.editarTarea("Bearer token", tareaPendiente.getIdTarea(), request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("La fecha y hora limite deben ser posteriores al momento actual", ex.getMessage());
    }

    @Test
    void editarTarea_noCreaNuevaTarea() {
        EditarTareaRequest request = new EditarTareaRequest(
                "Lavar platos",
                null,
                PrioridadTarea.MEDIA,
                null
        );

        MiembroGrupo adminEnGrupo = crearMiembro(admin, grupo, rolAdmin);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token")).thenReturn(admin);
        when(miembroGrupoRepository.findByUsuarioIdUsuarioAndGrupoIdGrupo(admin.getIdUsuario(), grupo.getIdGrupo()))
                .thenReturn(Optional.of(adminEnGrupo));
        when(tareaRepository.findById(tareaPendiente.getIdTarea())).thenReturn(Optional.of(tareaPendiente));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarTareaResponse response = tareaService.editarTarea("Bearer token", tareaPendiente.getIdTarea(), request);

        assertEquals(tareaPendiente.getIdTarea(), response.getIdTarea());
        assertEquals(EstadoTarea.PENDIENTE, response.getEstado());
    }
        private MiembroGrupo crearMiembro(Usuario usuario, Grupo grupo, Role role) {
                MiembroGrupo miembroGrupo = new MiembroGrupo();
                miembroGrupo.setUsuario(usuario);
                miembroGrupo.setGrupo(grupo);
                miembroGrupo.setRol(role);
                miembroGrupo.setFechaUnion(LocalDateTime.now());
                return miembroGrupo;
        }

                private Tarea crearTareaBase(Long idTarea, EstadoTarea estado, LocalDateTime fechaLimite) {
                Tarea tarea = new Tarea();
                tarea.setIdTarea(idTarea);
                tarea.setGrupo(grupo);
                tarea.setUsuarioAsignado(miembro);
                tarea.setNombre("Tarea base");
                tarea.setDescripcion("Descripcion base");
                tarea.setPrioridad(PrioridadTarea.MEDIA);
                tarea.setEstado(estado);
                tarea.setFechaLimite(fechaLimite);
                tarea.setFechaCreacion(LocalDateTime.now());
                tarea.setFechaCambioEstado(LocalDateTime.now());
                return tarea;
        }
}
