package com.dysight.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dysight.backend.model.Usuario;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);

    Usuario findByEmail(String email);
}