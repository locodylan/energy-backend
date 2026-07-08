package com.energyapp.backend.model;

import com.energyapp.backend.repository.ConsumoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecomendacionScheduler {

    private final ConsumoRepository repository;

    public RecomendacionScheduler(ConsumoRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 60000) // cada 1 minuto
    public void limpiarRecomendaciones() {

        LocalDateTime limite =
                LocalDateTime.now().minusMinutes(10);

        List<ConsumoEnergia> lista = repository.findAll();

        for (ConsumoEnergia c : lista) {

            if (c.getRecomendacionCreatedAt() != null &&
                c.getRecomendacionCreatedAt().isBefore(limite)) {

                c.setRecomendacionOculta(true);
                repository.save(c);
            }
        }
    }
}