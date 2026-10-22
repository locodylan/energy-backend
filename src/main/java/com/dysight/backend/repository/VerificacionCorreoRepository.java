package com.dysight.backend.repository;

import com.dysight.backend.model.VerificacionCorreo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacionCorreoRepository
        extends JpaRepository<VerificacionCorreo, Long> {

    Optional<VerificacionCorreo> findTopByEmailOrderByIdDesc(String email);
     Optional<VerificacionCorreo> findTopByEmailAndActivoTrueOrderByIdDesc(String email);
}