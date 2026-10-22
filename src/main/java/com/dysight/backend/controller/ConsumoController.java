package com.dysight.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.dysight.backend.dto.AhorroInteligente;
import com.dysight.backend.dto.AnalisisDispositivo;
import com.dysight.backend.dto.AnomaliaConsumo;
import com.dysight.backend.dto.ConsumoPorDispositivo;
import com.dysight.backend.dto.ConsumoPorFecha;
import com.dysight.backend.dto.ConsumoPorHora;
import com.dysight.backend.dto.CostoEnergia;
import com.dysight.backend.dto.DashboardEnergia;
import com.dysight.backend.dto.GraficaConsumo;
import com.dysight.backend.dto.PrediccionConsumo;
import com.dysight.backend.dto.PrediccionIA;
import com.dysight.backend.model.ConsumoEnergia;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.Usuario;
import com.dysight.backend.repository.ConsumoRepository;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.repository.UsuarioRepository;
import com.dysight.backend.service.PrediccionIAService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/consumos")

public class ConsumoController {
    
private final UsuarioRepository usuarioRepository;
private final ConsumoRepository repository;
private final DispositivoIoTRepository iotRepository;
private final PrediccionIAService prediccionIAService;

public ConsumoController(
ConsumoRepository repository,
UsuarioRepository usuarioRepository,
DispositivoIoTRepository iotRepository,
PrediccionIAService prediccionIAService
) {

this.repository = repository;
this.usuarioRepository = usuarioRepository;
this.iotRepository = iotRepository;
this.prediccionIAService = prediccionIAService;

}

public List<ConsumoEnergia> historialPorUsuarioYDispositivo(
        Integer usuarioId,
        String dispositivo
) {

    return repository.findByUsuarioIdAndDispositivo(
            usuarioId,
            dispositivo
    );
}
    @GetMapping
    public List<ConsumoEnergia> obtenerConsumos() {
        return repository.findAll();
    }

    @PostMapping
    public ConsumoEnergia agregarConsumo(@RequestBody ConsumoEnergia consumo) {
        return repository.save(consumo);
    }
    

@PostMapping("/agregar-dispositivo")
public DispositivoIoT agregarDispositivo(
        @RequestBody Map<String, Object> data
) {

    String aparatoConectado =
            data.get("aparatoConectado")
                    .toString()
                    .trim();

    Long usuarioId =
            Long.parseLong(
                    data.get("usuarioId").toString()
            );

    if (aparatoConectado.isEmpty()) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nombre del aparato es obligatorio"
        );
    }

    Usuario usuario =
            usuarioRepository.findById(usuarioId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Usuario no encontrado"
                            )
                    );

    /*
     * Obtener TODOS los dispositivos registrados
     * para determinar el siguiente número global
     * de Shelly.
     */
    List<DispositivoIoT> dispositivos =
            iotRepository.findAll();

    int siguienteNumero = 1;

    for (DispositivoIoT dispositivo : dispositivos) {

        String nombre =
                dispositivo.getNombre();

        if (nombre != null &&
                nombre.matches("Shelly \\d+")) {

            try {

                String numeroTexto =
                        nombre.substring(
                                "Shelly ".length()
                        );

                int numero =
                        Integer.parseInt(
                                numeroTexto
                        );

                if (numero >= siguienteNumero) {
                    siguienteNumero = numero + 1;
                }

            } catch (NumberFormatException e) {
                // Ignorar nombres con formato numérico inválido.
            }
        }
    }

    String nombreDispositivo =
            "Shelly " + siguienteNumero;

    DispositivoIoT dispositivo =
            new DispositivoIoT();

    /*
     * Nombre lógico generado automáticamente.
     */
    dispositivo.setNombre(
            nombreDispositivo
    );

    /*
     * Nombre del aparato introducido por el usuario.
     */
    dispositivo.setAparatoConectado(
            aparatoConectado
    );

    dispositivo.setEncendido(
            true
    );

    dispositivo.setUsuario(
            usuario
    );

    /*
     * Este registro todavía no está vinculado
     * necesariamente a un Shelly físico.
     */
    dispositivo.setActivo(
            true
    );

    return iotRepository.save(
            dispositivo
    );
}

@GetMapping("/listar-dispositivos")
public List<DispositivoIoT> listarDispositivos(

        @RequestParam Long usuarioId
) {

    return iotRepository.findByUsuarioId(usuarioId);
}

@GetMapping("/total")
public double consumoTotal(@RequestParam Long usuarioId) {

    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    Map<Long, Double> consumoPorDispositivo =
            new HashMap<>();

    for (ConsumoEnergia c : lecturas) {

        if (c.getDispositivoIoT() == null) {
            continue;
        }

        if (c.getEnergiaWh() == null) {
            continue;
        }

        DispositivoIoT dispositivo =
                c.getDispositivoIoT();

        Long dispositivoId =
                dispositivo.getId();

        Double energiaInicial =
                dispositivo.getEnergiaInicialWh();

        if (energiaInicial == null) {
            continue;
        }

        double consumoWh =
                c.getEnergiaWh() - energiaInicial;

        if (consumoWh < 0) {
            consumoWh = 0;
        }

        /*
         * Guardamos solamente la lectura más alta
         * de cada dispositivo.
         */
        Double consumoAnterior =
                consumoPorDispositivo.get(dispositivoId);

        if (
            consumoAnterior == null ||
            consumoWh > consumoAnterior
        ) {
            consumoPorDispositivo.put(
                    dispositivoId,
                    consumoWh
            );
        }
    }

    double totalWh =
            consumoPorDispositivo.values()
                    .stream()
                    .mapToDouble(valor -> valor)
                    .sum();

    return totalWh / 1000.0;
}

@GetMapping("/promedio")
public double consumoPromedio() {

    List<ConsumoEnergia> consumos = repository.findAll();

    if (consumos.isEmpty()) {
        return 0;
    }

    double total = 0;

    for (ConsumoEnergia c : consumos) {
        total += c.getConsumo();
    }

    return total / consumos.size();
}

@GetMapping("/mayor")
public ConsumoEnergia mayorConsumo() {

    List<ConsumoEnergia> consumos = repository.findAll();

    ConsumoEnergia mayor = consumos.get(0);

    for (ConsumoEnergia c : consumos) {

        if (c.getConsumo() > mayor.getConsumo()) {
            mayor = c;
        }
    }

    return mayor;
}
@GetMapping("/alertas")
public List<ConsumoEnergia> obtenerAlertas() {

    List<ConsumoEnergia> consumos = repository.findAll();

    List<ConsumoEnergia> alertas = new ArrayList<>();

    for (ConsumoEnergia c : consumos) {

        if (c.getConsumo() > 2.0) {
            alertas.add(c);
        }
    }

    return alertas;
}

@GetMapping("/dispositivos")
public List<ConsumoPorDispositivo> consumoPorDispositivo() {

    List<ConsumoEnergia> consumos =
            repository.findAll();

    Map<String, Double> mapa =
            new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        mapa.put(
                c.getDispositivo(),
                mapa.getOrDefault(
                        c.getDispositivo(),
                        0.0
                ) + c.getConsumo()
        );
    }

    List<ConsumoPorDispositivo> resultado =
            new ArrayList<>();

    for (String dispositivo : mapa.keySet()) {

        DispositivoIoT iot =
                iotRepository.findByNombre(dispositivo);

        boolean encendido = false;

        if (iot != null) {
            encendido = iot.isEncendido();
        }

        resultado.add(

                new ConsumoPorDispositivo(
                        dispositivo,
                        mapa.get(dispositivo),
                        encendido
                )
        );
    }

    return resultado;
}
@GetMapping("/prediccion")
public PrediccionConsumo obtenerPrediccion() {

    List<ConsumoEnergia> consumos = repository.findAll();

    if (consumos.isEmpty()) {
        return new PrediccionConsumo(0);
    }

    double total = 0;

    for (ConsumoEnergia c : consumos) {
        total += c.getConsumo();
    }

    double promedio = total / consumos.size();

    double prediccion = promedio * 1.1;

    return new PrediccionConsumo(prediccion);
}
@GetMapping("/ia")
public PrediccionIA prediccionIA() {

    try {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "ia-python/prediccion.py"
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String resultado = reader.readLine();

        process.waitFor();

        return new PrediccionIA(resultado);

    } catch (Exception e) {

        e.printStackTrace();

        return new PrediccionIA("Error al ejecutar IA");
    }
}

@GetMapping("/prediccion-ia")
public Map<String, String> prediccionIA(

        @RequestParam Integer usuarioId

) {

    Map<String, String> respuesta = new HashMap<>();

    respuesta.put(
            "mensaje",
            prediccionIAService.predecirConsumo(usuarioId)
    );

    return respuesta;
}

@GetMapping("/grafica-real")
public List<Map<String, Object>> obtenerGraficaReal(
        @RequestParam Long usuarioId
) {
    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    return lecturas.stream()
            .filter(c -> c.getFechaHora() != null)
            .sorted((a, b) ->
                    a.getFechaHora().compareTo(b.getFechaHora())
            )
            .map(c -> {
                Map<String, Object> dato = new HashMap<>();

                dato.put("fechaHora", c.getFechaHora());
                dato.put(
                        "potenciaW",
                        c.getPotenciaW() != null
                                ? c.getPotenciaW()
                                : 0.0
                );
                dato.put(
                        "dispositivo",
                        c.getDispositivo()
                );
                dato.put(
                        "dispositivoIoTId",
                        c.getDispositivoIoT() != null
                                ? c.getDispositivoIoT().getId()
                                : null
                );

                return dato;
            })
            .toList();
}

@GetMapping("/consumo-acumulado")
public Map<String, Object> obtenerConsumoAcumulado(
        @RequestParam Long usuarioId
) {
    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    /*
     * energiaWh es el contador acumulado físico del Shelly.
     *
     * Cada DispositivoIoT tiene su propia energiaInicialWh,
     * que representa el valor del contador cuando ese dispositivo
     * lógico fue registrado en DySight.
     *
     * Consumo real:
     *
     * energiaWh actual - energiaInicialWh
     */

    Map<Long, Double> consumoPorDispositivo =
            new HashMap<>();

    /*
     * Ordenamos cronológicamente para que la última lectura
     * de cada dispositivo sea la que quede almacenada.
     */
    lecturas.stream()
            .filter(c -> c.getFechaHora() != null)
            .filter(c -> c.getEnergiaWh() != null)
            .filter(c -> c.getDispositivoIoT() != null)
            .sorted((a, b) ->
                    a.getFechaHora().compareTo(b.getFechaHora())
            )
            .forEach(c -> {

                DispositivoIoT dispositivo =
                        c.getDispositivoIoT();

                Long dispositivoId =
                        dispositivo.getId();

                Double energiaActual =
                        c.getEnergiaWh();

                Double energiaInicial =
                        dispositivo.getEnergiaInicialWh();

                /*
                 * Si por alguna razón el dispositivo todavía
                 * no tiene una línea base, no calculamos consumo.
                 */
                if (energiaInicial == null) {
                    return;
                }

                /*
                 * Consumo real desde que el dispositivo fue
                 * registrado en DySight.
                 */
                double consumoWh =
                        energiaActual - energiaInicial;

                /*
                 * Si el contador físico del Shelly se reinició,
                 * evitamos mostrar un consumo negativo.
                 */
                if (consumoWh < 0) {
                    consumoWh = 0;
                }

                /*
                 * Guardamos la última lectura calculada
                 * de este dispositivo lógico.
                 */
                consumoPorDispositivo.put(
                        dispositivoId,
                        consumoWh
                );
            });

    /*
     * Sumamos el consumo real de todos los dispositivos
     * pertenecientes al usuario.
     */
    double consumoTotalWh =
        consumoPorDispositivo.values()
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(valor -> valor)
                .sum();

    double consumoTotalKwh =
            consumoTotalWh / 1000.0;

    Map<String, Object> resultado =
            new HashMap<>();

    resultado.put(
            "consumoAcumuladoWh",
            consumoTotalWh
    );

    resultado.put(
            "consumoAcumuladoKwh",
            consumoTotalKwh
    );

    return resultado;
}

@GetMapping("/grafica-real-24h")
public List<Map<String, Object>> obtenerGraficaReal24Horas(
        @RequestParam Long usuarioId
) {
    LocalDateTime ahora = LocalDateTime.now();

    LocalDateTime hace24Horas =
            ahora.minusHours(24);

    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    return lecturas.stream()
            .filter(c ->
                    c.getFechaHora() != null &&
                    !c.getFechaHora().isBefore(hace24Horas) &&
                    !c.getFechaHora().isAfter(ahora)
            )
            .sorted((a, b) ->
                    a.getFechaHora().compareTo(
                            b.getFechaHora()
                    )
            )
            .map(c -> {
                Map<String, Object> dato =
                        new HashMap<>();

                dato.put(
                        "fechaHora",
                        c.getFechaHora()
                );

                dato.put(
                        "potenciaW",
                        c.getPotenciaW() != null
                                ? c.getPotenciaW()
                                : 0.0
                );

                dato.put(
                        "dispositivo",
                        c.getDispositivo()
                );

                dato.put(
                        "dispositivoIoTId",
                        c.getDispositivoIoT() != null
                                ? c.getDispositivoIoT().getId()
                                : null
                );

                return dato;
            })
            .toList();
}

@GetMapping("/ranking-real")
public List<Map<String, Object>> obtenerRankingReal(
        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    Map<String, List<Double>> agrupado =
            new HashMap<>();

    for (ConsumoEnergia c : lecturas) {

        if (c.getDispositivo() == null ||
                c.getPotenciaW() == null ||
                c.getPotenciaW() <= 0) {
            continue;
        }

        agrupado
                .computeIfAbsent(
                        c.getDispositivo(),
                        k -> new ArrayList<>()
                )
                .add(c.getPotenciaW());
    }

    List<Map<String, Object>> resultado =
            new ArrayList<>();

    for (Map.Entry<String, List<Double>> entry
            : agrupado.entrySet()) {

        List<Double> valores = entry.getValue();

double promedio = valores.stream()
        .filter(Objects::nonNull)
        .mapToDouble(valor -> valor.doubleValue())
        .average()
        .orElse(0.0);

        Map<String, Object> dato =
                new HashMap<>();

        dato.put(
                "dispositivo",
                entry.getKey()
        );

        dato.put(
                "potenciaPromedio",
                promedio
        );

        dato.put(
                "lecturas",
                valores.size()
        );

        resultado.add(dato);
    }

    resultado.sort(
            (a, b) -> Double.compare(
                    ((Number) b.get("potenciaPromedio"))
                            .doubleValue(),
                    ((Number) a.get("potenciaPromedio"))
                            .doubleValue()
            )
    );

    return resultado;
}

@GetMapping("/fechas")
public List<ConsumoPorFecha> consumoPorFecha() {

    List<ConsumoEnergia> consumos =
            repository.findAll();

    Map<String, Double> mapa =
            new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        String fecha =
                c.getFechaHora()
                 .toLocalDate()
                 .toString();

        mapa.put(
                fecha,
                mapa.getOrDefault(fecha, 0.0)
                        + c.getConsumo()
        );
    }

    List<ConsumoPorFecha> resultado =
            new ArrayList<>();

    for (String fecha : mapa.keySet()) {

        resultado.add(
                new ConsumoPorFecha(
                        fecha,
                        mapa.get(fecha)
                )
        );
    }

    return resultado;
}

@GetMapping("/horas-pico")
public List<ConsumoPorHora> horasPico() {

    List<ConsumoEnergia> consumos =
            repository.findAll();

    Map<String, Double> mapa =
            new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        String hora =
                c.getFechaHora()
                 .toLocalTime()
                 .toString();

        mapa.put(
                hora,
                mapa.getOrDefault(hora, 0.0)
                        + c.getConsumo()
        );
    }

    List<ConsumoPorHora> resultado =
            new ArrayList<>();

    for (String hora : mapa.keySet()) {

        resultado.add(
                new ConsumoPorHora(
                        hora,
                        mapa.get(hora)
                )
        );
    }

    return resultado;
}

@GetMapping("/ranking")
public List<ConsumoPorDispositivo> rankingDispositivos(

        @RequestParam Integer usuarioId

) {

    List<ConsumoEnergia> consumos =
            repository.findByUsuarioId(usuarioId);

    Map<String, Double> mapa = new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        mapa.put(
                c.getDispositivo(),
                mapa.getOrDefault(
                        c.getDispositivo(),
                        0.0
                ) + c.getConsumo()
        );
    }

    List<ConsumoPorDispositivo> resultado =
            new ArrayList<>();

    for (String dispositivo : mapa.keySet()) {

        DispositivoIoT dispositivoIoT =
                iotRepository.findByNombreAndUsuarioId(
                        dispositivo,
                        Long.valueOf(usuarioId)
                );

        boolean encendido = false;

        if (dispositivoIoT != null) {
            encendido =
                    dispositivoIoT.isEncendido();
        }

        resultado.add(

                new ConsumoPorDispositivo(

                        dispositivo,
                        mapa.get(dispositivo),
                        encendido
                )
        );
    }

    resultado.sort((a, b) ->
            Double.compare(
                    b.getTotalConsumo(),
                    a.getTotalConsumo()
            )
    );

    return resultado;
}

@GetMapping("/anomalias")
public List<AnomaliaConsumo> detectarAnomalias(
        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =
            repository.findByUsuarioId(usuarioId);

    // Solo consideramos lecturas reales con potencia mayor a 0 W
    List<ConsumoEnergia> lecturasValidas =
            consumos.stream()
                    .filter(c ->
                            c.getPotenciaW() != null &&
                            c.getPotenciaW() > 0
                    )
                    .toList();

    if (lecturasValidas.isEmpty()) {
        return new ArrayList<>();
    }

    double total = 0;

    for (ConsumoEnergia c : lecturasValidas) {
        total += c.getPotenciaW();
    }

    double promedio =
            total / lecturasValidas.size();

    // Margen inicial del 50% sobre el comportamiento promedio
    double limiteAnomalia =
            promedio * 1.5;

    List<AnomaliaConsumo> resultado =
            new ArrayList<>();

    for (ConsumoEnergia c : lecturasValidas) {

        double potencia =
                c.getPotenciaW();

        if (potencia > limiteAnomalia) {

            resultado.add(
                    new AnomaliaConsumo(
                            c.getDispositivo(),
                            potencia,
                            "Potencia anómala detectada"
                    )
            );
        }
    }

    return resultado;
}

@GetMapping("/recomendaciones")
public List<Map<String, String>> recomendaciones(@RequestParam Long usuarioId) {

    try {

        List<ConsumoEnergia> consumos =
                repository.findByUsuarioId(usuarioId);

        if (consumos == null || consumos.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime hace10Minutos =
                LocalDateTime.now().minusMinutes(10);

        List<Map<String, String>> lista = new ArrayList<>();

        for (ConsumoEnergia c : consumos) {

            if (c == null) continue;
            if (c.getId() == null) continue;
            if (c.getDispositivo() == null) continue;
            if (c.getConsumo() == null) continue;

            LocalDateTime fechaCreacion = c.getRecomendacionCreatedAt();

            // inicializar si es null
            if (fechaCreacion == null) {
    fechaCreacion = LocalDateTime.now();
    c.setRecomendacionCreatedAt(fechaCreacion);
}

            if (fechaCreacion.isBefore(hace10Minutos)) {
                continue;
            }

            Map<String, String> r = new HashMap<>();

            r.put("id", String.valueOf(c.getId()));

            if (c.getConsumo() >= 5) {
                r.put("mensaje", "Reducir uso de " + c.getDispositivo());
                r.put("tipo", "alto");
            } else {
                r.put("mensaje", "Consumo estable en " + c.getDispositivo());
                r.put("tipo", "normal");
            }

            lista.add(r);
        }

        return lista;

    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}

@PostMapping("/ocultar-recomendacion/{id}")
public void ocultarRecomendacion(

    @PathVariable Long id

) {

ConsumoEnergia consumo =
        repository.findById(id)
                .orElseThrow();

consumo.setRecomendacionOculta(true);

repository.save(consumo);

}



@GetMapping("/analisis-ia")
public List<AnalisisDispositivo> analisisIA(

        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =

            repository.findByUsuarioId(
                    usuarioId
            );

    Map<String, List<Double>> mapa =
            new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        mapa.putIfAbsent(
                c.getDispositivo(),
                new ArrayList<>()
        );

        mapa.get(
                c.getDispositivo()
        ).add(c.getConsumo());
    }

    List<AnalisisDispositivo> resultado =
            new ArrayList<>();

    for (String dispositivo : mapa.keySet()) {

        List<Double> lista =
                mapa.get(dispositivo);

        double total = 0;

        for (double valor : lista) {
            total += valor;
        }

        double promedio =
                total / lista.size();

        double actual =
                lista.get(lista.size() - 1);

        double porcentaje =
                ((actual - promedio)
                        / promedio) * 100;

        String estado;
        String mensaje;

        if (porcentaje > 20) {

            estado = "PELIGROSO";

            mensaje =
                    "Consumo excesivo detectado";

        } else if (porcentaje > 5) {

            estado = "ALTO";

            mensaje =
                    "Consumo elevado";

        } else {

            estado = "ESTABLE";

            mensaje =
                    "Consumo normal";
        }

        resultado.add(

                new AnalisisDispositivo(

                        dispositivo,
                        promedio,
                        actual,
                        porcentaje,
                        estado,
                        mensaje
                )
        );
    }

    return resultado;
}
@GetMapping("/dashboard")
public DashboardEnergia dashboard(

        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =

            repository.findByUsuarioId(
                    usuarioId
            );
            if (consumos.isEmpty()) {

    return new DashboardEnergia(
            0,
            0,
            0,
            0,
            0
    );
}

    double total = 0;
    double mayor = 0;

    for (ConsumoEnergia c : consumos) {

        total += c.getConsumo();

        if (c.getConsumo() > mayor) {
            mayor = c.getConsumo();
        }
    }

    double promedio = total / consumos.size();

    int alertas = 0;

    for (ConsumoEnergia c : consumos) {

        if (c.getConsumo() > promedio * 1.5) {
            alertas++;
        }
    }

    double prediccionIA = promedio * 1.1;

    return new DashboardEnergia(
            total,
            promedio,
            prediccionIA,
            mayor,
            alertas
    );
}

@GetMapping("/grafica")
public List<GraficaConsumo> graficaConsumo(@RequestParam Long usuarioId) {

    try {

        List<ConsumoEnergia> consumos =
                repository.findByUsuarioId(usuarioId);

        if (consumos == null || consumos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<LocalDate, Double> mapa = new TreeMap<>();

        for (ConsumoEnergia c : consumos) {

            if (c == null) continue;
            if (c.getFechaHora() == null) continue;
            if (c.getConsumo() == null) continue;

            LocalDate fecha = c.getFechaHora().toLocalDate();

            mapa.put(
                    fecha,
                    mapa.getOrDefault(fecha, 0.0) + c.getConsumo()
            );
        }

        List<GraficaConsumo> resultado = new ArrayList<>();

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<LocalDate, Double> entry : mapa.entrySet()) {

            if (entry.getKey() == null || entry.getValue() == null) continue;

            resultado.add(
                    new GraficaConsumo(
                            entry.getKey().format(formato),
                            entry.getValue()
                    )
            );
        }

        return resultado;

    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}

@GetMapping("/historial")
@Transactional(readOnly = true)
public List<ConsumoEnergia> historial(

    @RequestParam Long usuarioId

) {

List<ConsumoEnergia> lista =
        repository.findByUsuarioId(
                usuarioId
        );

// MÁS NUEVOS ARRIBA
lista.sort((a, b) ->
        b.getFechaHora().compareTo(a.getFechaHora())
);

return lista;

}

@GetMapping("/ultimos-consumos")
public List<ConsumoEnergia> ultimosConsumos(

        @RequestParam Long usuarioId

) {

    List<ConsumoEnergia> lista =
            repository.findByUsuarioId(
                    usuarioId
            );

    LocalDateTime hace24Horas =
            LocalDateTime.now().minusHours(24);

    List<ConsumoEnergia> resultado =
            new ArrayList<>();

    for (ConsumoEnergia c : lista) {

        if (
            c.getFechaHora()
             .isAfter(hace24Horas)
        ) {

            resultado.add(c);
        }
    }

    resultado.sort((a, b) ->
            b.getFechaHora()
             .compareTo(a.getFechaHora())
    );

    return resultado;
}

@GetMapping("/grafica-dispositivos")
public Map<String, List<Double>> graficaDispositivos(

        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =

            repository.findByUsuarioId(
                    usuarioId
            );

    Map<String, List<Double>> mapa =
            new HashMap<>();

    for (ConsumoEnergia c : consumos) {

        mapa.putIfAbsent(
                c.getDispositivo(),
                new ArrayList<>()
        );

        mapa.get(
                c.getDispositivo()
        ).add(c.getConsumo());
    }

    return mapa;
}



@GetMapping("/consumo-habitaciones")
public List<Map<String, Object>> consumoPorHabitaciones(
        @RequestParam Long usuarioId,
        @RequestParam(defaultValue = "24h") String periodo) {

    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime desde;

    if ("24h".equalsIgnoreCase(periodo)) {
        desde = ahora.minusHours(24);
    } else if ("7d".equalsIgnoreCase(periodo)) {
        desde = ahora.minusDays(7);
    } else {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El período debe ser 24h o 7d"
        );
    }

    // Obtener los dispositivos del usuario.
    List<DispositivoIoT> dispositivos =
            iotRepository.findByUsuarioId(usuarioId);

    // Obtener las lecturas registradas para el usuario.
    List<ConsumoEnergia> lecturas =
            repository.findByUsuarioId(usuarioId);

    // Agrupar las lecturas por ID de dispositivo.
    Map<Long, List<ConsumoEnergia>> lecturasPorDispositivo =
            new LinkedHashMap<>();

    for (ConsumoEnergia lectura : lecturas) {

        if (lectura == null
                || lectura.getDispositivoIoT() == null
                || lectura.getDispositivoIoT().getId() == null
                || lectura.getFechaHora() == null
                || lectura.getEnergiaWh() == null) {
            continue;
        }

        Long dispositivoId = lectura.getDispositivoIoT().getId();

        lecturasPorDispositivo
                .computeIfAbsent(dispositivoId, k -> new ArrayList<>())
                .add(lectura);
    }

    List<Map<String, Object>> resultado = new ArrayList<>();

    for (DispositivoIoT dispositivo : dispositivos) {

        if (dispositivo == null || dispositivo.getId() == null) {
            continue;
        }

        Long dispositivoId = dispositivo.getId();

        // Obtener la habitación.
        String habitacion = dispositivo.getCategoria();

        if (habitacion == null || habitacion.trim().isEmpty()) {
            habitacion = "Sin habitación";
        } else {
            habitacion = habitacion.trim();
        }

        // Obtener el nombre del aparato.
        String nombreDispositivo = dispositivo.getAparatoConectado();

        if (nombreDispositivo == null
                || nombreDispositivo.trim().isEmpty()) {
            nombreDispositivo = "Aparato";
        } else {
            nombreDispositivo = nombreDispositivo.trim();
        }

        // Obtener únicamente las lecturas de este dispositivo.
        List<ConsumoEnergia> lecturasDispositivo =
                lecturasPorDispositivo.get(dispositivoId);

        if (lecturasDispositivo == null
                || lecturasDispositivo.isEmpty()) {
            continue;
        }

        // Ordenar cronológicamente, de la lectura más antigua a la más reciente.
        lecturasDispositivo.sort(
                (a, b) -> a.getFechaHora().compareTo(b.getFechaHora())
        );

        double consumoDispositivo = 0.0;

        ConsumoEnergia lecturaAnterior = null;
        boolean hayLecturasPeriodo = false;

        for (ConsumoEnergia lectura : lecturasDispositivo) {

            LocalDateTime fecha = lectura.getFechaHora();

            // IMPORTANTE:
            // No utilizar lecturas anteriores al período seleccionado.
            // Así evitamos sumar consumos acumulados fuera del período.
            if (fecha.isBefore(desde)) {
                continue;
            }

            // Ignorar lecturas futuras.
            if (fecha.isAfter(ahora)) {
                break;
            }

            hayLecturasPeriodo = true;

            double energiaActual = lectura.getEnergiaWh();

            // Calcular diferencias solo entre lecturas del período.
            if (lecturaAnterior != null) {

                double energiaAnterior = lecturaAnterior.getEnergiaWh();

                double diferencia = energiaActual - energiaAnterior;

                if (diferencia >= 0) {
                    // Incremento normal del contador acumulado.
                    consumoDispositivo += diferencia;
                } else {
                    // Si el contador se reinició, contabilizar
                    // la energía de la lectura posterior al reinicio.
                    consumoDispositivo += energiaActual;
                }
            }

            lecturaAnterior = lectura;
        }

        // No devolver dispositivos sin lecturas dentro del período.
        if (!hayLecturasPeriodo) {
            continue;
        }

        // Evitar resultados negativos.
        if (consumoDispositivo < 0) {
            consumoDispositivo = 0.0;
        }

        // Convertir Wh a kWh.
        double consumoKWh = consumoDispositivo / 1000.0;

        Map<String, Object> dato = new LinkedHashMap<>();

        dato.put("habitacion", habitacion);
        dato.put("dispositivoId", dispositivoId);
        dato.put("dispositivo", nombreDispositivo);
        dato.put("consumoWh", consumoDispositivo);
        dato.put("consumoKWh", consumoKWh);

        resultado.add(dato);
    }

    return resultado;
}


@GetMapping("/grafica-habitacion")
public List<Map<String, Object>> graficaHabitacion(
        @RequestParam Long usuarioId,
        @RequestParam String habitacion,
        @RequestParam(defaultValue = "24h") String periodo) {

    LocalDateTime ahora = LocalDateTime.now();

    LocalDateTime desde = "7d".equalsIgnoreCase(periodo)
            ? ahora.minusDays(7)
            : ahora.minusHours(24);

    List<ConsumoEnergia> consumos =
            repository.findByUsuarioIdAndFechaHoraAfterOrderByFechaHoraAsc(
                    usuarioId,
                    desde
            );

    List<ConsumoEnergia> filtrados = new ArrayList<>();

    // Filtrar los registros correspondientes a la habitación.
    for (ConsumoEnergia consumo : consumos) {
        if (consumo.getDispositivoIoT() == null) {
            continue;
        }

        DispositivoIoT dispositivo = consumo.getDispositivoIoT();
        String categoria = dispositivo.getCategoria();

        if (categoria == null
                || !categoria.equalsIgnoreCase(habitacion)) {
            continue;
        }

        filtrados.add(consumo);
    }

    // Obtener la última lectura anterior al período por dispositivo.
    Map<Long, Double> energiaAnterior = new HashMap<>();

    for (ConsumoEnergia consumo : filtrados) {
        Long dispositivoId = consumo.getDispositivoIoT().getId();

        if (energiaAnterior.containsKey(dispositivoId)) {
            continue;
        }

        Optional<ConsumoEnergia> lecturaAnterior =
                repository
                        .findFirstByUsuario_IdAndDispositivoIoT_IdAndFechaHoraBeforeOrderByFechaHoraDesc(
                                usuarioId,
                                dispositivoId,
                                desde
                        );

        if (lecturaAnterior.isPresent()) {
            Double energia = lecturaAnterior.get().getEnergiaWh();

            if (energia != null) {
                energiaAnterior.put(dispositivoId, energia);
            }
        }
    }

    double energiaTotalWh = 0.0;

    List<Map<String, Object>> resultado = new ArrayList<>();

    // Calcular el consumo del período mediante diferencias sucesivas.
    for (ConsumoEnergia consumo : filtrados) {
        DispositivoIoT dispositivo = consumo.getDispositivoIoT();
        Long dispositivoId = dispositivo.getId();
        Double energiaActual = consumo.getEnergiaWh();

        if (energiaActual != null) {
            Double energiaPrev = energiaAnterior.get(dispositivoId);

            if (energiaPrev != null) {
                if (energiaActual >= energiaPrev) {
                    energiaTotalWh += energiaActual - energiaPrev;
                } else {
                    // Contempla un posible reinicio del contador.
                    energiaTotalWh += Math.max(0.0, energiaActual);
                }
            }

            energiaAnterior.put(dispositivoId, energiaActual);
        }

        Map<String, Object> item = new LinkedHashMap<>();

        item.put("fechaHora", consumo.getFechaHora());
        item.put("potenciaW", consumo.getPotenciaW());
        item.put("energiaWh", consumo.getEnergiaWh());
        item.put("dispositivo", dispositivo.getAparatoConectado());
        item.put("dispositivoIoTId", dispositivoId);
        item.put("habitacion", dispositivo.getCategoria());

        resultado.add(item);
    }

    // Incluir el total calculado en cada registro para Flutter.
    for (Map<String, Object> item : resultado) {
        item.put("energiaPeriodoWh", energiaTotalWh);
    }

    return resultado;
}


@PostMapping("/guardar")
public ConsumoEnergia guardarConsumo(
        @RequestBody Map<String, Object> data
) {
    ConsumoEnergia consumo = new ConsumoEnergia();

    consumo.setDispositivo(
            data.get("dispositivo").toString()
    );

    consumo.setConsumo(
            Double.parseDouble(
                    data.get("consumo").toString()
            )
    );

    consumo.setFechaHora(
            LocalDateTime.now()
    );

    Long usuarioId =
            Long.parseLong(
                    data.get("usuarioId").toString()
            );

    Usuario usuario =
            usuarioRepository.findById(usuarioId)
                    .orElse(null);

    consumo.setUsuario(usuario);

    String nombreDispositivo =
            consumo.getDispositivo();

    DispositivoIoT dispositivoIoT =
            iotRepository.findByNombreAndUsuarioId(
                    nombreDispositivo,
                    usuarioId
            );

    if (dispositivoIoT == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El dispositivo no existe"
        );
    }

    // Vincular el consumo con el dispositivo lógico.
    consumo.setDispositivoIoT(dispositivoIoT);

    if (!dispositivoIoT.isEncendido()) {
        throw new RuntimeException(
                "El dispositivo está apagado"
        );
    }

    repository.saveAndFlush(consumo);

    if (consumo.getConsumo() >= 5.0) {
        dispositivoIoT.setEncendido(false);
        iotRepository.save(dispositivoIoT);
    }

    return consumo;
}

@GetMapping("/costos")
public CostoEnergia calcularCostos(

        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =

            repository.findByUsuarioId(
                    usuarioId
            );

    double total = 0;

    for (ConsumoEnergia c : consumos) {

        total += c.getConsumo();
    }

    // Tarifa eléctrica simulada
    double tarifa = 0.15;

    double diario =
            total * tarifa;

    double mensual =
            diario * 30;

    double anual =
            mensual * 12;

    return new CostoEnergia(
            diario,
            mensual,
            anual
    );
}
@GetMapping("/ahorro-inteligente")
public AhorroInteligente ahorroInteligente(

        @RequestParam Long usuarioId
) {

    List<ConsumoEnergia> consumos =
            repository.findByUsuarioId(
                    usuarioId
            );

    double total = 0;

    for (ConsumoEnergia c : consumos) {

        total += c.getConsumo();
    }

    // Simulación IA
    double ahorroConsumo =
            total * 0.25;

    double tarifa = 0.15;

    double ahorroDinero =
            ahorroConsumo * tarifa;

    double eficiencia =
            (ahorroConsumo / total) * 100;

    String mensaje;

    if (eficiencia >= 30) {

        mensaje =
                "Excelente eficiencia energética";

    } else if (eficiencia >= 15) {

        mensaje =
                "Buen ahorro energético";

    } else {

        mensaje =
                "Consumo mejorable";
    }

    return new AhorroInteligente(

            total,
            ahorroConsumo,
            ahorroDinero,
            eficiencia,
            mensaje
    );
}

@GetMapping("/historial-dispositivo")
public List<ConsumoEnergia> historialDispositivo(
        @RequestParam Long dispositivoId
) {

    List<ConsumoEnergia> lista =
            repository.findByDispositivoIoTIdOrderByFechaHoraAsc(
                    dispositivoId
            );

    LocalDateTime hace24Horas =
            LocalDateTime.now().minusHours(24);

    List<ConsumoEnergia> resultado =
            new ArrayList<>();

    for (ConsumoEnergia c : lista) {

        if (
            c.getFechaHora() != null &&
            c.getFechaHora().isAfter(hace24Horas)
        ) {
            resultado.add(c);
        }
    }

    resultado.sort((a, b) ->
            b.getFechaHora()
             .compareTo(a.getFechaHora())
    );

    return resultado;
}

}