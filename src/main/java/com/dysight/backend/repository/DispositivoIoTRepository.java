package com.dysight.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dysight.backend.model.DispositivoIoT;

import java.util.List;

public interface DispositivoIoTRepository extends JpaRepository<DispositivoIoT, Long> {

    List<DispositivoIoT> findByUsuarioId(Long usuarioId);

    DispositivoIoT findByNombreAndUsuarioId(
            String nombre,
            Long usuarioId
    );

    DispositivoIoT findByNombre(String nombre);

    DispositivoIoT findByShellyId(String shellyId);

    List<DispositivoIoT> findAllByShellyIdAndUsuarioId(
        String shellyId,
        Long usuarioId
);
}