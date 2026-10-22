package com.dysight.backend.service;

import org.springframework.stereotype.Service;

import com.dysight.backend.model.ConsumoEnergia;
import com.dysight.backend.repository.ConsumoRepository;

import java.util.List;

@Service
public class PrediccionIAService {

    private final ConsumoRepository consumoRepository;

    public PrediccionIAService(ConsumoRepository consumoRepository) {
        this.consumoRepository = consumoRepository;
    }

    public String predecirConsumo(Integer usuarioId) {

        List<ConsumoEnergia> lista =
                consumoRepository.findByUsuarioId(usuarioId);

        if (lista.size() < 5) {
            return "Aún no existen suficientes datos para realizar una predicción.";
        }

        double promedio = lista.stream()
        .mapToDouble(c -> c.getConsumo())
        .average()
        .orElse(0);

double tendencia = lista.stream()
        .skip(Math.max(0, lista.size() - 5))
        .mapToDouble(c -> c.getConsumo())
        .average()
        .orElse(promedio);

        double prediccion = (promedio * 0.4) + (tendencia * 0.6);

        return String.format(
                "Según tu historial, mañana consumirás aproximadamente %.2f kWh.",
                prediccion
        );
    }
}