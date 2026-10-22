package com.dysight.backend.repository;

import com.dysight.backend.model.ProgramacionHorario;
import com.dysight.backend.model.EstadoProgramacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramacionHorarioRepository
        extends JpaRepository<ProgramacionHorario, Long> {

    List<ProgramacionHorario> findByDispositivoUsuarioId(
            Long usuarioId
    );

    List<ProgramacionHorario> findByDispositivoUsuarioIdAndEstado(
            Long usuarioId,
            EstadoProgramacion estado
    );

    List<ProgramacionHorario> findByEstado(
            EstadoProgramacion estado
    );

    boolean existsByDispositivoIdAndEstado(
            Long dispositivoId,
            EstadoProgramacion estado
    );

    @Query("""
        SELECT DISTINCT p
        FROM ProgramacionHorario p
        LEFT JOIN FETCH p.dias
        WHERE p.estado = :estado
    """)
    List<ProgramacionHorario> findByEstadoConDias(
            @Param("estado") EstadoProgramacion estado
    );
}