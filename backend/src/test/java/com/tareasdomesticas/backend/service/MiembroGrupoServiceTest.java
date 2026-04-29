package com.tareasdomesticas.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.tareasdomesticas.backend.dto.UnirseGrupoRequest;
import com.tareasdomesticas.backend.dto.UnirseGrupoResponse;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;

@ExtendWith(MockitoExtension.class)
class MiembroGrupoServiceTest {

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private SesionService sesionService;

    @Mock
    private GrupoService grupoService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MiembroGrupoService miembroGrupoService;

    private Usuario usuario;
    private Grupo grupo;
    private Role rolMiembro;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Ana");

        grupo = new Grupo();
        grupo.setIdGrupo(10L);
        grupo.setNombre("Casa");
        grupo.setCodigoInvitacion("ABC123");

        rolMiembro = new Role();
        rolMiembro.setNombre("MIEMBRO");
    }

    @Test
    void unirseAGrupo_caminoFeliz_retornaResponse() {
        UnirseGrupoRequest request = new UnirseGrupoRequest("ABC123");

        when(sesionService.obtenerUsuarioAutenticado("Bearer token-valido")).thenReturn(usuario);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())).thenReturn(Optional.empty());
        when(grupoService.buscarPorCodigoInvitacion("ABC123")).thenReturn(Optional.of(grupo));
        when(roleService.buscarPorNombre("MIEMBRO")).thenReturn(Optional.of(rolMiembro));
        when(miembroGrupoRepository.save(any(MiembroGrupo.class))).thenAnswer(invocation -> {
            MiembroGrupo m = invocation.getArgument(0);
            m.setIdMiembroGrupo(99L);
            return m;
        });

        UnirseGrupoResponse response = miembroGrupoService.unirseAGrupo("Bearer token-valido", request);

        assertEquals(99L, response.getIdMiembroGrupo());
        assertEquals(usuario.getIdUsuario(), response.getIdUsuario());
        assertEquals(grupo.getIdGrupo(), response.getIdGrupo());
        assertEquals("Casa", response.getNombreGrupo());
        assertEquals("MIEMBRO", response.getRolNombre());
        assertNotNull(response.getFechaUnion());
        assertEquals("Te has unido al grupo exitosamente", response.getMensaje());
    }

    @Test
    void unirseAGrupo_tokenInvalido_retornaUnauthorized() {
        when(sesionService.obtenerUsuarioAutenticado("Bearer token-invalido"))
                .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Token de sesion invalido"));

        ApiException ex = assertThrows(ApiException.class,
                () -> miembroGrupoService.unirseAGrupo("Bearer token-invalido",
                        new UnirseGrupoRequest("ABC123")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Token de sesion invalido", ex.getMessage());
    }

    @Test
    void unirseAGrupo_usuarioYaEnGrupo_retornaConflict() {
        MiembroGrupo existente = new MiembroGrupo();
        existente.setUsuario(usuario);

        when(sesionService.obtenerUsuarioAutenticado("Bearer token-valido")).thenReturn(usuario);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()))
                .thenReturn(Optional.of(existente));

        ApiException ex = assertThrows(ApiException.class,
                () -> miembroGrupoService.unirseAGrupo("Bearer token-valido",
                        new UnirseGrupoRequest("ABC123")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("El usuario ya pertenece a un grupo", ex.getMessage());
    }

    @Test
    void unirseAGrupo_codigoInvalido_retornaNotFound() {
        when(sesionService.obtenerUsuarioAutenticado("Bearer token-valido")).thenReturn(usuario);
        when(miembroGrupoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())).thenReturn(Optional.empty());
        when(grupoService.buscarPorCodigoInvitacion("XXXXXX")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> miembroGrupoService.unirseAGrupo("Bearer token-valido",
                        new UnirseGrupoRequest("XXXXXX")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Codigo de invitacion invalido", ex.getMessage());
    }
}
