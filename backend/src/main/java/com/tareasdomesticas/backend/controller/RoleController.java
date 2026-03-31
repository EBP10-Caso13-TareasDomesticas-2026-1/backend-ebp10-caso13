package com.tareasdomesticas.backend.controller;

import com.tareasdomesticas.backend.entity.Role;
import com.tareasdomesticas.backend.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public List<Role> listarRoles() {
        return roleService.listarTodos();
    }
}
