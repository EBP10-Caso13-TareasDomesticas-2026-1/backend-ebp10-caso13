package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.dto.CrearTareaRequest;
import com.tareasdomesticas.backend.dto.CrearTareaResponse;
import com.tareasdomesticas.backend.entity.*;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import com.tareasdomesticas.backend.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
}
