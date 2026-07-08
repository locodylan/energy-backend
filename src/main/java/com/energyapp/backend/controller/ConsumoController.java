package com.energyapp.backend.controller;

import com.energyapp.backend.model.DispositivoIoT;
import com.energyapp.backend.repository.DispositivoIoTRepository;
import com.energyapp.backend.model.ConsumoEnergia;
import com.energyapp.backend.repository.ConsumoRepository;
import com.energyapp.backend.dto.AhorroInteligente;
import com.energyapp.backend.dto.AnalisisDispositivo;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.energyapp.backend.dto.ConsumoPorDispositivo;
import com.energyapp.backend.dto.PrediccionConsumo;
import com.energyapp.backend.dto.PrediccionIA;
import com.energyapp.backend.dto.ConsumoPorFecha;
import com.energyapp.backend.dto.ConsumoPorHora;
import com.energyapp.backend.dto.AnomaliaConsumo;
import com.energyapp.backend.dto.DashboardEnergia;
import com.energyapp.backend.dto.GraficaConsumo;
import com.energyapp.backend.model.Usuario;
import com.energyapp.backend.repository.UsuarioRepository;
import com.energyapp.backend.dto.CostoEnergia;
import com.energyapp.backend.model.MetaEnergia;
import com.energyapp.backend.repository.MetaEnergiaRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/consumos")
public class ConsumoController {
    
private final UsuarioRepository usuarioRepository;
private final ConsumoRepository repository;
private final DispositivoIoTRepository iotRepository;
private final MetaEnergiaRepository metaRepository;

public ConsumoController(
ConsumoRepository repository,
UsuarioRepository usuarioRepository,
DispositivoIoTRepository iotRepository,
MetaEnergiaRepository metaRepository
) {

this.repository = repository;
this.usuarioRepository = usuarioRepository;
this.iotRepository = iotRepository;
this.metaRepository = metaRepository;

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

    String nombre =
            data.get("dispositivo").toString();

    Long usuarioId =
            Long.parseLong(
                    data.get("usuarioId").toString()
            );

    Usuario usuario =
            usuarioRepository.findById(usuarioId)
                    .orElse(null);

    DispositivoIoT existente =
            iotRepository.findByNombreAndUsuarioId(
                    nombre,
                    usuarioId
            );

    if (existente != null) {

        throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "El dispositivo ya está agregado"
        );
    }

    DispositivoIoT dispositivo =
            new DispositivoIoT();

    dispositivo.setNombre(nombre);

    dispositivo.setEncendido(true);

    dispositivo.setUsuario(usuario);

    return iotRepository.save(dispositivo);
}

@GetMapping("/listar-dispositivos")
public List<DispositivoIoT> listarDispositivos(

        @RequestParam Long usuarioId
) {

    return iotRepository.findByUsuarioId(usuarioId);
}

@GetMapping("/total")
public double consumoTotal(@RequestParam Long usuarioId) {

    List<ConsumoEnergia> consumos =
            repository.findByUsuarioId(usuarioId);

    double total = 0;

    for (ConsumoEnergia c : consumos) {
        total += c.getConsumo();
    }

    return total;
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

            repository.findByUsuarioId(
                    usuarioId
            );

    double total = 0;

    for (ConsumoEnergia c : consumos) {
        total += c.getConsumo();
    }

    double promedio = total / consumos.size();

    List<AnomaliaConsumo> resultado = new ArrayList<>();

    for (ConsumoEnergia c : consumos) {

        if (c.getConsumo() > promedio * 1.1) {

            resultado.add(
                    new AnomaliaConsumo(
                            c.getDispositivo(),
                            c.getConsumo(),
                            "Consumo anómalo detectado"
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

@PostMapping("/guardar")
public ConsumoEnergia guardarConsumo(
@RequestBody Map<String, Object> data
) {

    ConsumoEnergia consumo =
            new ConsumoEnergia();

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
    // SI YA ESTÁ APAGADO → NO GUARDAR
    if (!dispositivoIoT.isEncendido()) {

        throw new RuntimeException(
                "El dispositivo está apagado"
        );
    }
MetaEnergia meta =
        metaRepository.findByUsuarioId(
                usuarioId
        );

if (meta != null &&
        meta.isModoAhorro()) {

    List<ConsumoEnergia> lista =
            repository.findByUsuarioId(
                    usuarioId
            );

    double total = 0;

    for (ConsumoEnergia c : lista) {

        total += c.getConsumo();
    }

    total += consumo.getConsumo();

    if (total > meta.getLimiteMensual()) {

        if (dispositivoIoT != null) {

            dispositivoIoT.setEncendido(false);

            iotRepository.save(
                    dispositivoIoT
            );
        }

        throw new ResponseStatusException(

        HttpStatus.BAD_REQUEST,

        "⚠ Límite mensual excedido. "
        + "Modo ahorro activado."
);
    }
}
    // GUARDAR CONSUMO
    
   repository.saveAndFlush(consumo);
    // SI EL CONSUMO ES MUY ALTO → APAGAR
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

    @RequestParam Integer usuarioId,
    @RequestParam String dispositivo

) {

    List<ConsumoEnergia> lista =

            repository.findByUsuarioIdAndDispositivo(
                    usuarioId,
                    dispositivo
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

}