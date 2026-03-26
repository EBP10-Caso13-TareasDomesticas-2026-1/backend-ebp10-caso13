package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.repository.GrupoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;

    public GrupoService(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Optional<Grupo> buscarPorId(Long id) {
        return grupoRepository.findById(id);
    }

    public Optional<Grupo> buscarPorCodigoInvitacion(String codigoInvitacion) {
        return grupoRepository.findByCodigoInvitacion(codigoInvitacion);
    }

    public Grupo guardar(Grupo grupo) {
        return grupoRepository.save(grupo);
    }
}