package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existePorCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}