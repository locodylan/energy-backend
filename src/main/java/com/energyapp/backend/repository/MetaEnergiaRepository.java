package com.energyapp.backend.repository;

import com.energyapp.backend.model.MetaEnergia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaEnergiaRepository
extends JpaRepository<MetaEnergia, Long> {

    MetaEnergia findByUsuarioId(
            Long usuarioId
    );
}