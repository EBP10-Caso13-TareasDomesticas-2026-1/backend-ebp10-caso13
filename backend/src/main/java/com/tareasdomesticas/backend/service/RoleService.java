package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> listarTodos() {
        return roleRepository.findAll();
    }

    public Optional<Role> buscarPorId(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> buscarPorNombre(String nombre) {
        return roleRepository.findByNombre(nombre);
    }

    public Role guardar(Role role) {
        return roleRepository.save(role);
    }
}