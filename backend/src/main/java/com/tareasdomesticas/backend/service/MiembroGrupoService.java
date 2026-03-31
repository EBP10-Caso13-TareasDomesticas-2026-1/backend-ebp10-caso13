package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.MiembroGrupo;
import com.tareasdomesticas.backend.repository.MiembroGrupoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MiembroGrupoService {

    private final MiembroGrupoRepository miembroGrupoRepository;

    public MiembroGrupoService(MiembroGrupoRepository miembroGrupoRepository) {
        this.miembroGrupoRepository = miembroGrupoRepository;
    }

    public List<MiembroGrupo> listarTodos() {
        return miembroGrupoRepository.findAll();
    }

    public Optional<MiembroGrupo> buscarPorId(Long id) {
        return miembroGrupoRepository.findById(id);
    }

    public MiembroGrupo guardar(MiembroGrupo miembroGrupo) {
        if (miembroGrupo.getFechaUnion() == null) {
            miembroGrupo.setFechaUnion(LocalDateTime.now());
        }
        return miembroGrupoRepository.save(miembroGrupo);
    }

    public MiembroGrupo crearMiembro(MiembroGrupo miembroGrupo) {
        return guardar(miembroGrupo);
    }
}
