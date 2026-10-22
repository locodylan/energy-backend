package com.dysight.backend.repository;

import com.dysight.backend.model.RegistroPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroPendienteRepository
        extends JpaRepository<RegistroPendiente, Long> {

    Optional<RegistroPendiente> findByEmail(String email);

    Optional<RegistroPendiente> findByUsername(String username);
}