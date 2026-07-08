package com.energyapp.backend.repository;

import com.energyapp.backend.model.ConsumoEnergia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumoRepository
        extends JpaRepository<ConsumoEnergia, Long> {

    List<ConsumoEnergia>
        findByUsuarioId(Long usuarioId);
        List<ConsumoEnergia> findByUsuarioId(Integer usuarioId);
        List<ConsumoEnergia> findByDispositivo(
        String dispositivo
);
List<ConsumoEnergia> findByRecomendacionOcultaFalse();
List<ConsumoEnergia> findByUsuarioIdAndDispositivo(
        Integer usuarioId,
        String dispositivo
);

}