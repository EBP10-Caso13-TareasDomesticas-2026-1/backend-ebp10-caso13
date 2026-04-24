package com.tareasdomesticas.backend.service;

import org.springframework.transaction.annotation.Transactional;
import com.tareasdomesticas.backend.entity.Grupo;
import com.tareasdomesticas.backend.repository.GrupoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GrupoService {

    private static final String CODIGO_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final GrupoRepository grupoRepository;
    private final Random random = new Random();

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
    @Transactional
    public Grupo crearGrupo(Grupo grupo) {
        if (grupo.getCodigoInvitacion() == null || grupo.getCodigoInvitacion().isBlank()) {
            grupo.setCodigoInvitacion(generarCodigoInvitacionUnica());
        }
        if (grupo.getCreadoEn() == null) {
            grupo.setCreadoEn(LocalDateTime.now());
        }
        return grupoRepository.save(grupo);
    }

    private String generarCodigoInvitacionUnica() {
        String codigo;
        do {
            codigo = generarCodigoInvitacion();
        } while (grupoRepository.findByCodigoInvitacion(codigo).isPresent());
        return codigo;
    }

    public String generarCodigoInterno() {
        return generarCodigoInvitacionUnica();
    }

    private String generarCodigoInvitacion() {
        StringBuilder builder = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            builder.append(CODIGO_CHARS.charAt(random.nextInt(CODIGO_CHARS.length())));
        }
        return builder.toString();
    }
}
