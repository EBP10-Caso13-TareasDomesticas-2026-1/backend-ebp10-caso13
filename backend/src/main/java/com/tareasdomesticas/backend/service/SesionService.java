package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.exception.ApiException;
import com.tareasdomesticas.backend.repository.SesionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SesionService {

    private final SesionRepository sesionRepository;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    public List<Sesion> listarTodos() {
        return sesionRepository.findAll();
    }

    public Optional<Sesion> buscarPorId(Long id) {
        return sesionRepository.findById(id);
    }

    public Sesion guardar(Sesion sesion) {
        return sesionRepository.save(sesion);
    }

    public Sesion crearSesion(Usuario usuario) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setToken(UUID.randomUUID().toString());
        sesion.setCreadaEn(LocalDateTime.now());
        sesion.setExpiraEn(LocalDateTime.now().plusMinutes(15));
        sesion.setCerradaEn(null);

        return sesionRepository.save(sesion);
    }

    public void cerrarSesion(String token) {
        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        sesion.setCerradaEn(LocalDateTime.now());
        sesionRepository.save(sesion);
    }

    public Usuario obtenerUsuarioAutenticado(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token de sesion invalido"));

        if (sesion.getCerradaEn() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesion ya fue cerrada");
        }

        if (sesion.getExpiraEn() == null || !sesion.getExpiraEn().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesion ha expirado");
        }

        return sesion.getUsuario();
    }

    private String extraerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El token de autorizacion es obligatorio");
        }

        String token = authorizationHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El token de autorizacion es obligatorio");
        }

        return token;
    }
}