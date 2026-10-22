package com.dysight.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.repository.DispositivoIoTRepository;

import java.util.List;

@Service
public class ShellyScheduler {

    private final DispositivoIoTRepository dispositivoRepository;
    private final ShellyService shellyService;

    public ShellyScheduler(
            DispositivoIoTRepository dispositivoRepository,
            ShellyService shellyService) {

        this.dispositivoRepository = dispositivoRepository;
        this.shellyService = shellyService;
    }

    @Scheduled(fixedRate = 60000)
    public void registrarLecturas() {

        List<DispositivoIoT> dispositivos =
                dispositivoRepository.findAll();

        for (DispositivoIoT dispositivo : dispositivos) {

             if (!dispositivo.isActivo()) {
        continue;
    }

            if (dispositivo.getIpShelly() == null ||
                    dispositivo.getIpShelly().isBlank()) {
                continue;
            }

            try {

                shellyService.guardarLecturaShelly(dispositivo);

                System.out.println(
                        "Lectura automática registrada: "
                                + dispositivo.getNombre()
                );

            } catch (Exception e) {

                System.err.println(
                        "No se pudo registrar lectura de "
                                + dispositivo.getNombre()
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }
}