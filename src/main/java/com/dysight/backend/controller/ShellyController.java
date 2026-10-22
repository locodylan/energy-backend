package com.dysight.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.dysight.backend.dto.ConsumoEnergiaResponseDTO;
import com.dysight.backend.dto.DispositivoIoTResponseDTO;
import com.dysight.backend.dto.DispositivoShellyRegistrarDTO;
import com.dysight.backend.model.ConsumoEnergia;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.service.ShellyService;
import com.dysight.backend.service.ShellyCloudService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/iot/shelly")
@CrossOrigin(origins = "*")
public class ShellyController {

    private final ShellyService shellyService;
    private final ShellyCloudService shellyCloudService;
    private final DispositivoIoTRepository dispositivoRepository;

    public ShellyController(
            ShellyService shellyService,
            ShellyCloudService shellyCloudService,
            DispositivoIoTRepository dispositivoRepository) {
        this.shellyService = shellyService;
          this.shellyCloudService = shellyCloudService;
        this.dispositivoRepository = dispositivoRepository;
    }

    private DispositivoIoTResponseDTO convertirDTO(DispositivoIoT dispositivo) {

    DispositivoIoTResponseDTO dto = new DispositivoIoTResponseDTO();

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
        dto.setUsuarioId(dispositivo.getUsuario().getId());
    }

    return dto;
}

    @GetMapping("/buscar")
public List<Map<String, Object>> buscarDispositivosShelly() {
    return shellyService.buscarDispositivosShelly();
}

    @GetMapping("/status")
    public Map<String, Object> obtenerEstado(@RequestParam Long dispositivoId) {

        DispositivoIoT dispositivo = dispositivoRepository
                .findById(dispositivoId)
                .orElseThrow(() ->
                        new RuntimeException("Dispositivo no encontrado."));

        return shellyService.obtenerEstado(dispositivo);
    }

    @PostMapping("/registrar")
public DispositivoIoT registrarShelly(
        @RequestParam String ip,
        @RequestParam Long usuarioId) {

    return shellyService.registrarShelly(ip, usuarioId);
}

@PostMapping("/guardar-lectura")
public ConsumoEnergiaResponseDTO guardarLectura(
        @RequestParam Long dispositivoId) {

    DispositivoIoT dispositivo = dispositivoRepository
            .findById(dispositivoId)
            .orElseThrow(() ->
                    new RuntimeException("Dispositivo no encontrado."));

    ConsumoEnergia consumo =
            shellyService.guardarLecturaShelly(dispositivo);

    ConsumoEnergiaResponseDTO dto =
            new ConsumoEnergiaResponseDTO();

    dto.setId(consumo.getId());
    dto.setDispositivo(consumo.getDispositivo());

    if (consumo.getDispositivoIoT() != null) {
        dto.setDispositivoIoTId(
                consumo.getDispositivoIoT().getId()
        );
    }

    dto.setEncendido(consumo.isEncendido());
    dto.setVoltajeV(consumo.getVoltajeV());
    dto.setCorrienteA(consumo.getCorrienteA());
    dto.setPotenciaW(consumo.getPotenciaW());
    dto.setEnergiaWh(consumo.getEnergiaWh());
    dto.setFrecuenciaHz(consumo.getFrecuenciaHz());
    dto.setTemperaturaC(consumo.getTemperaturaC());
    dto.setFechaHora(consumo.getFechaHora());

    return dto;
}

        @PostMapping("/registrar-por-id")
        public DispositivoIoTResponseDTO registrarShellyPorId(
                @RequestBody DispositivoShellyRegistrarDTO datos) {

        DispositivoIoT dispositivo =
        shellyService.registrarShellyPorId(
                datos.getShellyId(),
                datos.getIpShelly(),
                datos.getMac(),
                datos.getTipo(),
                datos.getEnergiaInicialWh(),
                datos.getAparatoConectado(),
                datos.getCategoria(),
                datos.getUsuarioId()
        );

        return convertirDTO(dispositivo);
        }

    @GetMapping("/control")
    public Map<String, Object> cambiarEstado(
            @RequestParam Long dispositivoId,
            @RequestParam boolean encender) {

        DispositivoIoT dispositivo = dispositivoRepository
                .findById(dispositivoId)
                .orElseThrow(() ->
                        new RuntimeException("Dispositivo no encontrado."));

        Map<String, Object> respuesta =
                shellyService.cambiarEstado(dispositivo, encender);

        dispositivo.setEncendido(encender);
        dispositivoRepository.save(dispositivo);

        return respuesta;
    }

@GetMapping("/consumo")
public Map<String, Object> obtenerConsumo(
        @RequestParam Long dispositivoId) {

    DispositivoIoT dispositivo = dispositivoRepository
            .findById(dispositivoId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Dispositivo no encontrado."
                    )
            );

    // El consumo se calcula exclusivamente
    // para este DispositivoIoT.
    double consumoKWh =
            shellyService.calcularConsumoKWh(
                    dispositivo.getId()
            );

    Map<String, Object> respuesta =
            new java.util.HashMap<>();

    respuesta.put(
            "dispositivo",
            dispositivo.getNombre()
    );

    respuesta.put(
            "dispositivoId",
            dispositivo.getId()
    );

    respuesta.put(
            "consumoKWh",
            consumoKWh
    );

    return respuesta;
}

@GetMapping("/detectar")
public Map<String, Object> detectarShelly(
        @RequestParam String ip) {

    return shellyService.detectarShelly(ip);
}

@GetMapping("/cloud-status")
public List<Map<String, Object>> obtenerEstadoCloud(
        @RequestParam Long dispositivoId) {

    DispositivoIoT dispositivo = dispositivoRepository
            .findById(dispositivoId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Dispositivo no encontrado."
                    )
            );

    if (dispositivo.getShellyId() == null
            || dispositivo.getShellyId().isBlank()) {
        throw new RuntimeException(
                "El dispositivo no tiene un Shelly ID configurado."
        );
    }

    String shellyId = dispositivo.getShellyId();

if (shellyId.startsWith("shellyplugusg4-")) {
    shellyId = shellyId.substring("shellyplugusg4-".length());
}

return shellyCloudService.obtenerEstado(shellyId);
}

}