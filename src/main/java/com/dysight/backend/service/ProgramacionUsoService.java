package com.dysight.backend.service;

import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.EstadoProgramacion;
import com.dysight.backend.model.ProgramacionUso;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.repository.ProgramacionUsoRepository;
import com.dysight.backend.dto.ProgramacionUsoDTO;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProgramacionUsoService {

    private final ProgramacionUsoRepository programacionRepository;
    private final DispositivoIoTRepository dispositivoRepository;
    private final ShellyService shellyService;

    public ProgramacionUsoService(
            ProgramacionUsoRepository programacionRepository,
            DispositivoIoTRepository dispositivoRepository,
            ShellyService shellyService) {

        this.programacionRepository = programacionRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.shellyService = shellyService;
    }
    private ProgramacionUsoDTO convertirADTO(
        ProgramacionUso programacion) {

    ProgramacionUsoDTO dto = new ProgramacionUsoDTO();

    dto.setId(programacion.getId());
    dto.setDispositivoId(
            programacion.getDispositivo().getId()
    );
    dto.setDispositivoNombre(
            programacion.getDispositivo().getNombre()
    );
    dto.setAparatoConectado(
            programacion.getDispositivo().getAparatoConectado()
    );
    dto.setCategoria(
            programacion.getDispositivo().getCategoria()
    );
    dto.setInicio(programacion.getInicio());
    dto.setFin(programacion.getFin());
    dto.setDuracionMinutos(
            programacion.getDuracionMinutos()
    );
    dto.setEstado(programacion.getEstado());

    return dto;
}

    /**
     * Crea una nueva programación de uso.
     */
public ProgramacionUsoDTO crearProgramacion(
        Long dispositivoId,
        Integer duracionMinutos) {

    if (duracionMinutos == null || duracionMinutos <= 0) {
        throw new RuntimeException(
                "La duración debe ser mayor a 0 minutos."
        );
    }

    DispositivoIoT dispositivo = dispositivoRepository
            .findById(dispositivoId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Dispositivo no encontrado."
                    ));

    // Consultamos el estado físico real del Shelly
    Map<String, Object> estadoShelly =
            shellyService.obtenerEstado(dispositivo);

    Boolean estadoFisico =
            (Boolean) estadoShelly.get("output");

    if (estadoFisico == null) {
        throw new RuntimeException(
                "No se pudo obtener el estado del dispositivo."
        );
    }

    // Sincronizamos el estado de la base de datos
    if (dispositivo.isEncendido() != estadoFisico) {
        dispositivo.setEncendido(estadoFisico);
        dispositivoRepository.save(dispositivo);
    }

    // Para crear una programación de uso,
    // el dispositivo debe estar físicamente encendido.
    if (!estadoFisico) {
        throw new RuntimeException(
                "El dispositivo debe estar encendido para crear una programación de uso."
        );
    }

    boolean tieneProgramacionActiva =
            programacionRepository.existsByDispositivoIdAndEstadoIn(
                    dispositivoId,
                    List.of(
                            EstadoProgramacion.ACTIVA,
                            EstadoProgramacion.AVISO
                    )
            );

    if (tieneProgramacionActiva) {
        throw new RuntimeException(
                "El dispositivo ya tiene una programación de uso activa."
        );
    }

    LocalDateTime inicio = LocalDateTime.now();
    LocalDateTime fin = inicio.plusMinutes(duracionMinutos);

    ProgramacionUso programacion = new ProgramacionUso();

    programacion.setDispositivo(dispositivo);
    programacion.setInicio(inicio);
    programacion.setFin(fin);
    programacion.setDuracionMinutos(duracionMinutos);
    programacion.setEstado(EstadoProgramacion.ACTIVA);

    ProgramacionUso guardada =
            programacionRepository.save(programacion);

    return convertirADTO(guardada);
}

    /**
     * Busca las programaciones activas y controla
     * automáticamente el aviso y el apagado.
     *
     * Se ejecuta cada 5 segundos.
     */
    @Scheduled(fixedRate = 5000)
    public void revisarProgramaciones() {

        LocalDateTime ahora = LocalDateTime.now();

        List<ProgramacionUso> programaciones =
                programacionRepository.findByEstadoIn(
                        List.of(
                                EstadoProgramacion.ACTIVA,
                                EstadoProgramacion.AVISO
                        )
                );

        for (ProgramacionUso programacion : programaciones) {

            LocalDateTime fin = programacion.getFin();

            /*
             * Momento en el que debe aparecer el aviso:
             * exactamente 1 minuto antes de finalizar.
             */
            LocalDateTime momentoAviso =
                    fin.minusMinutes(1);

            /*
             * Primero comprobamos si ya terminó.
             */
            if (!ahora.isBefore(fin)) {

                apagarAutomaticamente(programacion);

                continue;
            }

            /*
             * Si estamos dentro del último minuto,
             * cambiamos el estado a AVISO.
             */
            if (!ahora.isBefore(momentoAviso)
                    && programacion.getEstado()
                    == EstadoProgramacion.ACTIVA) {

                programacion.setEstado(
                        EstadoProgramacion.AVISO
                );

                programacionRepository.save(programacion);
            }
        }
    }

    /**
     * Apaga físicamente el Shelly.
     */
    private void apagarAutomaticamente(
            ProgramacionUso programacion) {

        try {

            DispositivoIoT dispositivo =
                    programacion.getDispositivo();

            /*
             * Apagamos el Shelly físicamente.
             */
            shellyService.cambiarEstado(
                    dispositivo,
                    false
            );

            /*
             * Actualizamos también el estado
             * almacenado en la base de datos.
             */
            dispositivo.setEncendido(false);

            dispositivoRepository.save(dispositivo);

            programacion.setEstado(
                    EstadoProgramacion.APAGADA
            );

            programacionRepository.save(programacion);

        } catch (Exception e) {

            System.out.println(
                    "No se pudo apagar automáticamente "
                    + "el dispositivo "
                    + programacion.getDispositivo().getId()
                    + ": "
                    + e.getMessage()
            );
        }
    }

    /**
     * El usuario decidió mantener el dispositivo encendido.
     */
    public ProgramacionUsoDTO mantenerEncendido(
        Long programacionId) {

    ProgramacionUso programacion =
            programacionRepository
                    .findById(programacionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Programación no encontrada."
                            ));

    if (programacion.getEstado()
            != EstadoProgramacion.AVISO) {

        throw new RuntimeException(
                "La programación no está en estado de aviso."
        );
    }

    programacion.setEstado(
            EstadoProgramacion.MANTENER
    );

    ProgramacionUso actualizada =
            programacionRepository.save(programacion);

    return convertirADTO(actualizada);
}

    /**
     * El usuario decidió apagar el dispositivo.
     */
    
    public ProgramacionUsoDTO apagarPorUsuario(
        Long programacionId) {

    ProgramacionUso programacion =
            programacionRepository
                    .findById(programacionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Programación no encontrada."
                            ));

    if (programacion.getEstado()
            != EstadoProgramacion.AVISO) {

        throw new RuntimeException(
                "La programación no está en estado de aviso."
        );
    }

    DispositivoIoT dispositivo =
            programacion.getDispositivo();

    shellyService.cambiarEstado(
            dispositivo,
            false
    );

    dispositivo.setEncendido(false);
    dispositivoRepository.save(dispositivo);

    programacion.setEstado(
            EstadoProgramacion.APAGADA
    );

    ProgramacionUso actualizada =
            programacionRepository.save(programacion);

    return convertirADTO(actualizada);
}

    /**
     * Obtiene una programación por su ID.
     */
   public ProgramacionUsoDTO obtenerPorId(Long id) {

    ProgramacionUso programacion =
            programacionRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Programación no encontrada."
                            ));

    return convertirADTO(programacion);
}

    /**
     * Obtiene las programaciones de un usuario.
     */
    public List<ProgramacionUsoDTO> obtenerPorUsuario(
        Long usuarioId) {

    return programacionRepository
            .findByDispositivoUsuarioId(usuarioId)
            .stream()
            .map(this::convertirADTO)
            .toList();
}

public List<ProgramacionUsoDTO> obtenerActivasPorUsuario(
        Long usuarioId) {

    return programacionRepository
            .findByDispositivoUsuarioIdAndEstadoInOrderByFinAsc(
                    usuarioId,
                    List.of(
                            EstadoProgramacion.ACTIVA,
                            EstadoProgramacion.AVISO
                    )
            )
            .stream()
            .map(this::convertirADTO)
            .toList();
}

public ProgramacionUsoDTO actualizarDuracion(
        Long programacionId,
        Integer nuevaDuracionMinutos) {

    if (nuevaDuracionMinutos == null
            || nuevaDuracionMinutos <= 0) {

        throw new RuntimeException(
                "La duración debe ser mayor a 0 minutos."
        );
    }

    ProgramacionUso programacion =
            programacionRepository
                    .findById(programacionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Programación no encontrada."
                            ));

    if (programacion.getEstado()
            != EstadoProgramacion.ACTIVA) {

        throw new RuntimeException(
                "Solo se puede modificar una programación activa."
        );
    }

    LocalDateTime nuevoFin =
            programacion.getInicio()
                    .plusMinutes(nuevaDuracionMinutos);

    programacion.setDuracionMinutos(
            nuevaDuracionMinutos
    );

    programacion.setFin(nuevoFin);

    ProgramacionUso actualizada =
            programacionRepository.save(programacion);

    return convertirADTO(actualizada);
}

public ProgramacionUsoDTO cancelarProgramacion(Long programacionId) {

    ProgramacionUso programacion =
            programacionRepository
                    .findById(programacionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Programación no encontrada."
                            ));

    if (programacion.getEstado() != EstadoProgramacion.ACTIVA
            && programacion.getEstado() != EstadoProgramacion.AVISO) {

        throw new RuntimeException(
                "Solo se puede cancelar una programación activa."
        );
    }

    programacion.setEstado(
            EstadoProgramacion.CANCELADA
    );

    ProgramacionUso cancelada =
            programacionRepository.save(programacion);

    return convertirADTO(cancelada);
}

}