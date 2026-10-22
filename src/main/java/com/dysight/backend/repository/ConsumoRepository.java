package com.dysight.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dysight.backend.model.ConsumoEnergia;

public interface ConsumoRepository
        extends JpaRepository<ConsumoEnergia, Long> {

    List<ConsumoEnergia> findByUsuarioId(Long usuarioId);

    List<ConsumoEnergia> findByUsuarioId(Integer usuarioId);

    List<ConsumoEnergia> findByDispositivo(String dispositivo);

    List<ConsumoEnergia> findByRecomendacionOcultaFalse();

    List<ConsumoEnergia> findByUsuarioIdOrderByFechaHoraAsc(
        Long usuarioId
);

    List<ConsumoEnergia> findByUsuarioIdAndDispositivo(
            Integer usuarioId,
            String dispositivo
    );

    List<ConsumoEnergia> findByUsuarioIdAndDispositivoOrderByFechaHoraAsc(
            Long usuarioId,
            String dispositivo
    );

    List<ConsumoEnergia> findByUsuarioIdAndFechaHoraAfterOrderByFechaHoraAsc(
            Long usuarioId,
            java.time.LocalDateTime fechaHora
    );

    // Nuevo: historial perteneciente a un DispositivoIoT específico
    List<ConsumoEnergia> findByDispositivoIoTIdOrderByFechaHoraAsc(
            Long dispositivoIoTId
    );

    Optional<ConsumoEnergia>
findFirstByUsuario_IdAndDispositivoIoT_IdAndFechaHoraBeforeOrderByFechaHoraDesc(
        Long usuarioId,
        Long dispositivoIoTId,
        LocalDateTime desde
);

List<ConsumoEnergia> findByUsuario_IdAndDispositivoIoT_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
        Long usuarioId,
        Long dispositivoIoTId,
        LocalDateTime desde,
        LocalDateTime hasta
);

    
}