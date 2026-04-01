package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.repository.SesionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public void cerrarSesion(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token no valido");
        }

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada para el token proporcionado"));

        sesion.setCerradaEn(LocalDateTime.now());
        sesionRepository.save(sesion);
    }
}