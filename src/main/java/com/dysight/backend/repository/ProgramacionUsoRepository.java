package com.dysight.backend.repository;

import com.dysight.backend.model.ProgramacionUso;
import com.dysight.backend.model.EstadoProgramacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramacionUsoRepository
        extends JpaRepository<ProgramacionUso, Long> {

    List<ProgramacionUso> findByDispositivoUsuarioId(Long usuarioId);

    List<ProgramacionUso> findByEstadoIn(
            List<EstadoProgramacion> estados
    );

    boolean existsByDispositivoIdAndEstadoIn(
            Long dispositivoId,
            List<EstadoProgramacion> estados
    );
    List<ProgramacionUso> findByDispositivoUsuarioIdAndEstadoIn(
        Long usuarioId,
        List<EstadoProgramacion> estados
);

List<ProgramacionUso> findByDispositivoUsuarioIdAndEstadoInOrderByFinAsc(
            Long usuarioId,
            List<EstadoProgramacion> estados
    );

}