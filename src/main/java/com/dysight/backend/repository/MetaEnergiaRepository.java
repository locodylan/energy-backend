package com.dysight.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dysight.backend.model.MetaEnergia;

public interface MetaEnergiaRepository
extends JpaRepository<MetaEnergia, Long> {

    MetaEnergia findByUsuarioId(
            Long usuarioId
    );
}