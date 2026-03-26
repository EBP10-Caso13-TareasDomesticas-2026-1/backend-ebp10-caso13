package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Sesion;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.repository.SesionRepository;
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
}