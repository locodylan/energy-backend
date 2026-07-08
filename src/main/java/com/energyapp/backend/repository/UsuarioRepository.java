package com.energyapp.backend.repository;

import com.energyapp.backend.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);
}