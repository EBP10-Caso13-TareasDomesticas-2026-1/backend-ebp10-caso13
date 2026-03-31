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
        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        if (sesion.getCerradaEn() != null) {
            throw new RuntimeException("La sesión ya fue cerrada");
        }

        sesion.setCerradaEn(LocalDateTime.now());
        sesionRepository.save(sesion);
    }

    public boolean sesionValida(String token) {
        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        return sesion.getCerradaEn() == null &&
               sesion.getExpiraEn().isAfter(LocalDateTime.now());
    }
}