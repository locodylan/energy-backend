package com.dysight.backend.controller;

import com.dysight.backend.dto.ProgramacionHorarioDTO;
import com.dysight.backend.model.DiaSemana;
import com.dysight.backend.service.ProgramacionHorarioService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/programaciones-horario")
@CrossOrigin(origins = "*")
public class ProgramacionHorarioController {

    private final ProgramacionHorarioService programacionService;

    public ProgramacionHorarioController(
            ProgramacionHorarioService programacionService) {

        this.programacionService = programacionService;
    }

    @PostMapping("/crear")
    public ProgramacionHorarioDTO crearProgramacion(
            @RequestParam Long dispositivoId,
            @RequestParam Set<DiaSemana> dias,
            @RequestParam LocalTime horaInicio,
            @RequestParam LocalTime horaFin) {

        return programacionService.crearProgramacion(
                dispositivoId,
                dias,
                horaInicio,
                horaFin
        );
    }

    @GetMapping("/{id}")
    public ProgramacionHorarioDTO obtenerProgramacion(
            @PathVariable Long id) {

        return programacionService.obtenerPorId(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ProgramacionHorarioDTO> obtenerPorUsuario(
            @PathVariable Long usuarioId) {

        return programacionService.obtenerPorUsuario(
                usuarioId
        );
    }

    @GetMapping("/usuario/{usuarioId}/activas")
    public List<ProgramacionHorarioDTO> obtenerProgramacionesActivas(
            @PathVariable Long usuarioId) {

        return programacionService
                .obtenerActivasPorUsuario(usuarioId);
    }

    @PutMapping("/{id}")
    public ProgramacionHorarioDTO actualizarProgramacion(
            @PathVariable Long id,
            @RequestParam Set<DiaSemana> dias,
            @RequestParam LocalTime horaInicio,
            @RequestParam LocalTime horaFin) {

        return programacionService.actualizarProgramacion(
                id,
                dias,
                horaInicio,
                horaFin
        );
    }

    @PutMapping("/{id}/cancelar")
    public ProgramacionHorarioDTO cancelarProgramacion(
            @PathVariable Long id) {

        return programacionService.cancelarProgramacion(id);
    }
}