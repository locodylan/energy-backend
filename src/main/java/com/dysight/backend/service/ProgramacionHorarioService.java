package com.dysight.backend.service;

import com.dysight.backend.dto.ProgramacionHorarioDTO;
import com.dysight.backend.model.DiaSemana;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.EstadoProgramacion;
import com.dysight.backend.model.ProgramacionHorario;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.repository.ProgramacionHorarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProgramacionHorarioService {

    private final ProgramacionHorarioRepository programacionRepository;
    private final DispositivoIoTRepository dispositivoRepository;

    public ProgramacionHorarioService(
            ProgramacionHorarioRepository programacionRepository,
            DispositivoIoTRepository dispositivoRepository) {

        this.programacionRepository = programacionRepository;
        this.dispositivoRepository = dispositivoRepository;
    }

    public ProgramacionHorarioDTO crearProgramacion(
            Long dispositivoId,
            Set<DiaSemana> dias,
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (dias == null || dias.isEmpty()) {
            throw new RuntimeException(
                    "Debe seleccionar al menos un día."
            );
        }

        if (horaInicio == null || horaFin == null) {
            throw new RuntimeException(
                    "Debe indicar la hora de inicio y la hora de finalización."
            );
        }

        if (horaInicio.equals(horaFin)) {
            throw new RuntimeException(
                    "La hora de inicio y finalización no pueden ser iguales."
            );
        }

        DispositivoIoT dispositivo =
                dispositivoRepository
                        .findById(dispositivoId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Dispositivo no encontrado."
                                ));
        boolean existeProgramacionActiva =
        programacionRepository
                .existsByDispositivoIdAndEstado(
                        dispositivoId,
                        EstadoProgramacion.ACTIVA
                );

if (existeProgramacionActiva) {
    throw new RuntimeException(
            "El dispositivo ya tiene una programación de horario activa."
    );
}

        ProgramacionHorario programacion =
                new ProgramacionHorario();

        programacion.setDispositivo(dispositivo);
        programacion.setDias(new HashSet<>(dias));
        programacion.setHoraInicio(horaInicio);
        programacion.setHoraFin(horaFin);
        programacion.setEstado(
                EstadoProgramacion.ACTIVA
        );

        ProgramacionHorario guardada =
                programacionRepository.save(programacion);

        return convertirADTO(guardada);
    }

    public ProgramacionHorarioDTO obtenerPorId(
            Long programacionId) {

        ProgramacionHorario programacion =
                programacionRepository
                        .findById(programacionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Programación no encontrada."
                                ));

        return convertirADTO(programacion);
    }

    public List<ProgramacionHorarioDTO> obtenerPorUsuario(
            Long usuarioId) {

        return programacionRepository
                .findByDispositivoUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<ProgramacionHorarioDTO> obtenerActivasPorUsuario(
            Long usuarioId) {

        return programacionRepository
                .findByDispositivoUsuarioIdAndEstado(
                        usuarioId,
                        EstadoProgramacion.ACTIVA
                )
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ProgramacionHorarioDTO actualizarProgramacion(
            Long programacionId,
            Set<DiaSemana> dias,
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (dias == null || dias.isEmpty()) {
            throw new RuntimeException(
                    "Debe seleccionar al menos un día."
            );
        }

        if (horaInicio == null || horaFin == null) {
            throw new RuntimeException(
                    "Debe indicar la hora de inicio y la hora de finalización."
            );
        }

        if (horaInicio.equals(horaFin)) {
            throw new RuntimeException(
                    "La hora de inicio y finalización no pueden ser iguales."
            );
        }

        ProgramacionHorario programacion =
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

        programacion.setDias(new HashSet<>(dias));
        programacion.setHoraInicio(horaInicio);
        programacion.setHoraFin(horaFin);

        ProgramacionHorario actualizada =
                programacionRepository.save(programacion);

        return convertirADTO(actualizada);
    }

    public ProgramacionHorarioDTO cancelarProgramacion(
            Long programacionId) {

        ProgramacionHorario programacion =
                programacionRepository
                        .findById(programacionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Programación no encontrada."
                                ));

        if (programacion.getEstado()
                != EstadoProgramacion.ACTIVA) {

            throw new RuntimeException(
                    "Solo se puede cancelar una programación activa."
            );
        }

        programacion.setEstado(
                EstadoProgramacion.CANCELADA
        );

        ProgramacionHorario cancelada =
                programacionRepository.save(programacion);

        return convertirADTO(cancelada);
    }

    private ProgramacionHorarioDTO convertirADTO(
            ProgramacionHorario programacion) {

        ProgramacionHorarioDTO dto =
                new ProgramacionHorarioDTO();

        dto.setId(programacion.getId());

        dto.setDispositivoId(
                programacion.getDispositivo().getId()
        );

        dto.setDispositivoNombre(
                programacion.getDispositivo().getNombre()
        );

        dto.setAparatoConectado(
                programacion.getDispositivo()
                        .getAparatoConectado()
        );

        dto.setCategoria(
                programacion.getDispositivo().getCategoria()
        );

        dto.setDias(
                programacion.getDias()
        );

        dto.setHoraInicio(
                programacion.getHoraInicio()
        );

        dto.setHoraFin(
                programacion.getHoraFin()
        );

        dto.setEstado(
                programacion.getEstado()
        );

        return dto;
    }
}