package com.dysight.backend.controller;

import com.dysight.backend.dto.ProgramacionUsoDTO;
import com.dysight.backend.service.ProgramacionUsoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programaciones")
@CrossOrigin(origins = "*")
public class ProgramacionUsoController {

    private final ProgramacionUsoService programacionService;

    public ProgramacionUsoController(
            ProgramacionUsoService programacionService) {

        this.programacionService = programacionService;
    }

    /**
     * Crear una programación de uso.
     *
     * Ejemplo:
     * POST /programaciones/crear?dispositivoId=29&duracionMinutos=3
     */
    @PostMapping("/crear")
public ProgramacionUsoDTO crearProgramacion(
        @RequestParam Long dispositivoId,
        @RequestParam Integer duracionMinutos) {

    return programacionService.crearProgramacion(
            dispositivoId,
            duracionMinutos
    );
}

    /**
     * Obtener una programación específica.
     */
    @GetMapping("/{id}")
public ProgramacionUsoDTO obtenerProgramacion(
        @PathVariable Long id) {
    return programacionService.obtenerPorId(id);
}

    /**
     * Obtener todas las programaciones de un usuario.
     */
    @GetMapping("/usuario/{usuarioId}")
public List<ProgramacionUsoDTO> obtenerPorUsuario(
        @PathVariable Long usuarioId) {
    return programacionService.obtenerPorUsuario(usuarioId);
}

@GetMapping("/usuario/{usuarioId}/activas")
public List<ProgramacionUsoDTO> obtenerProgramacionesActivas(
        @PathVariable Long usuarioId) {

    return programacionService
            .obtenerActivasPorUsuario(usuarioId);
}

    /**
     * El usuario decide mantener el dispositivo encendido.
     */
    @PutMapping("/{id}/mantener")
public ProgramacionUsoDTO mantenerEncendido(
        @PathVariable Long id) {

    return programacionService.mantenerEncendido(id);
}

    /**
     * El usuario decide apagar el dispositivo.
     */
    @PutMapping("/{id}/apagar")
public ProgramacionUsoDTO apagarPorUsuario(
        @PathVariable Long id) {

    return programacionService.apagarPorUsuario(id);
}

    @PutMapping("/{id}/duracion")
public ProgramacionUsoDTO actualizarDuracion(
        @PathVariable Long id,
        @RequestParam Integer duracionMinutos) {

    return programacionService.actualizarDuracion(
            id,
            duracionMinutos
    );
}

@PutMapping("/{id}/cancelar")
public ProgramacionUsoDTO cancelarProgramacion(
        @PathVariable Long id) {

    return programacionService.cancelarProgramacion(id);
}

}