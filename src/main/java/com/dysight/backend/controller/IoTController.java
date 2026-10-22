package com.dysight.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.dysight.backend.dto.DispositivoIoTActualizarDTO;
import com.dysight.backend.dto.DispositivoIoTResponseDTO;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.Usuario;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/iot")
@CrossOrigin(origins = "*")
public class IoTController {

    private final DispositivoIoTRepository repository;
    private final UsuarioRepository usuarioRepository;

    public IoTController(
            DispositivoIoTRepository repository,
            UsuarioRepository usuarioRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================================================
    // CONVERTIR DISPOSITIVO A DTO
    // =========================================================

    private DispositivoIoTResponseDTO convertirDTO(
            DispositivoIoT dispositivo
    ) {

        DispositivoIoTResponseDTO dto =
                new DispositivoIoTResponseDTO();

        dto.setId(dispositivo.getId());
        dto.setNombre(dispositivo.getNombre());
        dto.setAparatoConectado(dispositivo.getAparatoConectado());
        dto.setTipo(dispositivo.getTipo());
        dto.setCategoria(dispositivo.getCategoria());
        dto.setEncendido(dispositivo.isEncendido());
        dto.setIpShelly(dispositivo.getIpShelly());
        dto.setShellyId(dispositivo.getShellyId());
        dto.setMac(dispositivo.getMac());

        if (dispositivo.getUsuario() != null) {
            dto.setUsuarioId(
                    dispositivo.getUsuario().getId()
            );
        }

        return dto;
    }

    // =========================================================
    // LISTAR DISPOSITIVOS DE UN USUARIO
    // =========================================================

    @GetMapping
    public List<DispositivoIoTResponseDTO> listar(
            @RequestParam Long usuarioId
    ) {

        return repository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // =========================================================
    // CREAR DISPOSITIVO
    // =========================================================

    @PostMapping("/crear")
    public DispositivoIoT crear(
            @RequestBody DispositivoIoT dispositivo,
            @RequestParam Long usuarioId
    ) {

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        dispositivo.setUsuario(usuario);

        return repository.save(dispositivo);
    }

    // =========================================================
    // CAMBIAR ESTADO POR ID
    // =========================================================

    @PutMapping("/{id}")
    public DispositivoIoT cambiarEstado(
            @PathVariable Long id
    ) {

        DispositivoIoT dispositivo =
                repository.findById(id)
                        .orElse(null);

        if (dispositivo != null) {

            dispositivo.setEncendido(
                    !dispositivo.isEncendido()
            );

            return repository.save(dispositivo);
        }

        return null;
    }

    // =========================================================
    // RESUMEN DE DISPOSITIVOS
    // =========================================================

    @GetMapping("/resumen")
    public Map<String, Integer> resumen(
            @RequestParam Long usuarioId
    ) {

        List<DispositivoIoT> lista =
                repository.findByUsuarioId(usuarioId);

        int encendidos = 0;
        int apagados = 0;

        for (DispositivoIoT d : lista) {

            if (d.isEncendido()) {
                encendidos++;
            } else {
                apagados++;
            }
        }

        Map<String, Integer> resultado =
                new HashMap<>();

        resultado.put(
                "encendidos",
                encendidos
        );

        resultado.put(
                "apagados",
                apagados
        );

        resultado.put(
                "total",
                lista.size()
        );

        return resultado;
    }

    // =========================================================
    // TOGGLE POR NOMBRE
    // =========================================================

    @PutMapping("/toggle")
    public DispositivoIoT toggle(
            @RequestParam Long usuarioId,
            @RequestParam String nombre
    ) {

        DispositivoIoT dispositivo =
                repository.findByNombreAndUsuarioId(
                        nombre,
                        usuarioId
                );

        if (dispositivo == null) {
            return null;
        }

        dispositivo.setEncendido(
                !dispositivo.isEncendido()
        );

        return repository.save(dispositivo);
    }

    // =========================================================
    // ACTUALIZAR NOMBRE Y CATEGORÍA
    // =========================================================

    @PutMapping("/{id}/datos")
    public DispositivoIoTResponseDTO actualizarDatos(
            @PathVariable Long id,
            @RequestBody DispositivoIoTActualizarDTO datos
    ) {

        DispositivoIoT dispositivo =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Dispositivo no encontrado."
                                )
                        );

        dispositivo.setAparatoConectado(datos.getAparatoConectado());

// La categoría es escrita libremente por el usuario
dispositivo.setCategoria(datos.getCategoria());

        DispositivoIoT actualizado =
                repository.save(dispositivo);

        return convertirDTO(actualizado);
    }

    @GetMapping("/{id}")
public DispositivoIoTResponseDTO obtenerPorId(
        @PathVariable Long id
) {
    DispositivoIoT dispositivo =
            repository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Dispositivo no encontrado."
                            )
                    );

    return convertirDTO(dispositivo);
}
}