
package com.dysight.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dysight.backend.model.ConsumoEnergia;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.Usuario;
import com.dysight.backend.repository.ConsumoRepository;
import com.dysight.backend.repository.DispositivoIoTRepository;
import com.dysight.backend.repository.UsuarioRepository;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShellyService {

    private final RestTemplate restTemplate;
    private final ConsumoRepository consumoRepository;
    private final DispositivoIoTRepository dispositivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MqttService mqttService;

    public ShellyService(
            ConsumoRepository consumoRepository,
            DispositivoIoTRepository dispositivoRepository,
            UsuarioRepository usuarioRepository,
            MqttService mqttService) {

        this.restTemplate = new RestTemplate();
        this.consumoRepository = consumoRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.mqttService = mqttService;
    }

    // ============================================================
    // OBTENER ESTADO DEL SHELLY
    // MQTT para dispositivos registrados con Shelly ID.
    // HTTP local como alternativa para dispositivos sin Shelly ID.
    // ============================================================

    public Map<String, Object> obtenerEstado(
            DispositivoIoT dispositivo) {

        String shellyId = dispositivo.getShellyId();

        if (shellyId != null && !shellyId.isBlank()) {
            return mqttService.ejecutarRpcShelly(
                    shellyId.trim(),
                    "Switch.GetStatus",
                    Map.of("id", 0)
            );
        }

        return obtenerEstadoLocal(dispositivo);
    }

    private Map<String, Object> obtenerEstadoLocal(
            DispositivoIoT dispositivo) {

        String ip = dispositivo.getIpShelly();

        if (ip == null || ip.isBlank()) {
            throw new IllegalStateException(
                    "El dispositivo no tiene una IP local configurada."
            );
        }

        String url = "http://" + ip.trim()
                + "/rpc/Switch.GetStatus?id=0";

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta =
                restTemplate.getForObject(url, Map.class);

        if (respuesta == null) {
            throw new IllegalStateException(
                    "El Shelly no devolvió su estado."
            );
        }

        return respuesta;
    }

    // ============================================================
    // CONTROL DEL SHELLY
    // ============================================================

    public Map<String, Object> cambiarEstado(
            DispositivoIoT dispositivo,
            boolean encender) {

        String shellyId = dispositivo.getShellyId();

        if (shellyId != null && !shellyId.isBlank()) {
            return mqttService.ejecutarRpcShelly(
                    shellyId.trim(),
                    "Switch.Set",
                    Map.of(
                            "id", 0,
                            "on", encender
                    )
            );
        }

        return cambiarEstadoLocal(dispositivo, encender);
    }

    private Map<String, Object> cambiarEstadoLocal(
            DispositivoIoT dispositivo,
            boolean encender) {

        String ip = dispositivo.getIpShelly();

        if (ip == null || ip.isBlank()) {
            throw new IllegalStateException(
                    "El dispositivo no tiene una IP local configurada."
            );
        }

        String url = "http://" + ip.trim()
                + "/rpc/Switch.Set?id=0&on=" + encender;

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta =
                restTemplate.getForObject(url, Map.class);

        if (respuesta == null) {
            throw new IllegalStateException(
                    "El Shelly no devolvió respuesta al control."
            );
        }

        return respuesta;
    }

    // ============================================================
    // GUARDAR LECTURA DEL SHELLY
    // ============================================================

    @SuppressWarnings("unchecked")
    public ConsumoEnergia guardarLecturaShelly(
            DispositivoIoT dispositivo) {

        Map<String, Object> estado = obtenerEstado(dispositivo);

        ConsumoEnergia consumo = new ConsumoEnergia();

        consumo.setDispositivo(
                dispositivo.getAparatoConectado()
        );

        consumo.setDispositivoIoT(dispositivo);
        consumo.setFechaHora(LocalDateTime.now());
        consumo.setUsuario(dispositivo.getUsuario());

        // Estado del interruptor
        Object output = estado.get("output");

        if (output instanceof Boolean encendido) {
            consumo.setEncendido(encendido);
        } else {
            consumo.setEncendido(false);
        }

        // Mediciones eléctricas
        Number apower = estado.get("apower") instanceof Number n
                ? n : null;

        Number voltage = estado.get("voltage") instanceof Number n
                ? n : null;

        Number current = estado.get("current") instanceof Number n
                ? n : null;

        Number freq = estado.get("freq") instanceof Number n
                ? n : null;

        consumo.setPotenciaW(
                apower != null ? apower.doubleValue() : null
        );

        consumo.setVoltajeV(
                voltage != null ? voltage.doubleValue() : null
        );

        consumo.setCorrienteA(
                current != null ? current.doubleValue() : null
        );

        consumo.setFrecuenciaHz(
                freq != null ? freq.doubleValue() : null
        );

        // Energía acumulada
        Map<String, Object> aenergy =
                estado.get("aenergy") instanceof Map<?, ?> mapa
                        ? (Map<String, Object>) mapa
                        : null;

        if (aenergy != null) {
            Object totalObj = aenergy.get("total");

            if (totalObj instanceof Number total) {
                consumo.setEnergiaWh(total.doubleValue());
            }
        }

        // Temperatura
        Map<String, Object> temperature =
                estado.get("temperature") instanceof Map<?, ?> mapa
                        ? (Map<String, Object>) mapa
                        : null;

        if (temperature != null) {
            Object tCObj = temperature.get("tC");

            if (tCObj instanceof Number tC) {
                consumo.setTemperaturaC(tC.doubleValue());
            }
        }

        return consumoRepository.save(consumo);
    }

    // ============================================================
    // BUSCAR DISPOSITIVOS SHELLY EN LA RED LOCAL
    // ============================================================

    public List<Map<String, Object>> buscarDispositivosShelly() {

        List<Map<String, Object>> dispositivosEncontrados =
                new ArrayList<>();

        try {
            InetAddress direccionLocal =
                    InetAddress.getByName("192.168.100.16");

            JmDNS jmdns = JmDNS.create(direccionLocal);

            Map<String, Map<String, Object>> encontradosPorIp =
                    new LinkedHashMap<>();

            jmdns.addServiceListener(
                    "_http._tcp.local.",
                    new ServiceListener() {

                        @Override
                        public void serviceAdded(ServiceEvent event) {
                        }

                        @Override
                        public void serviceRemoved(ServiceEvent event) {
                        }

                        @Override
                        public void serviceResolved(ServiceEvent event) {

                            ServiceInfo info = event.getInfo();

                            for (InetAddress direccion :
                                    info.getInet4Addresses()) {

                                String ip = direccion.getHostAddress();

                                if (encontradosPorIp.containsKey(ip)) {
                                    continue;
                                }

                                try {
                                    String url = "http://" + ip
                                            + "/rpc/Shelly.GetDeviceInfo";

                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> datosShelly =
                                            restTemplate.getForObject(
                                                    url, Map.class
                                            );

                                    if (datosShelly == null
                                            || datosShelly.get("id") == null
                                            || datosShelly.get("model") == null) {
                                        continue;
                                    }

                                    Map<String, Object> dispositivo =
                                            new LinkedHashMap<>();

                                    dispositivo.put("detectado", true);
                                    dispositivo.put("ip", ip);
                                    dispositivo.put("nombre", info.getName());
                                    dispositivo.put("id", datosShelly.get("id"));
                                    dispositivo.put("mac", datosShelly.get("mac"));
                                    dispositivo.put("modelo", datosShelly.get("model"));
                                    dispositivo.put("generacion", datosShelly.get("gen"));
                                    dispositivo.put("tipo", datosShelly.get("app"));
                                    dispositivo.put("matter", datosShelly.get("matter"));
                                    dispositivo.put("puerto", info.getPort());

                                    encontradosPorIp.put(ip, dispositivo);

                                } catch (Exception e) {
                                    System.out.println(
                                            "No se pudo validar el dispositivo en "
                                                    + ip + ": " + e.getMessage()
                                    );
                                }
                            }
                        }
                    }
            );

            Thread.sleep(5000);
            jmdns.close();

            dispositivosEncontrados.addAll(
                    encontradosPorIp.values()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo realizar la búsqueda de dispositivos Shelly: "
                            + e.getMessage(),
                    e
            );
        }

        return dispositivosEncontrados;
    }

    // ============================================================
    // CALCULAR CONSUMO DE UN DISPOSITIVO ESPECÍFICO
    // ============================================================

    public double calcularConsumoKWh(Long dispositivoIoTId) {

        DispositivoIoT dispositivo = dispositivoRepository
                .findById(dispositivoIoTId)
                .orElseThrow(() ->
                        new RuntimeException("Dispositivo no encontrado.")
                );

        List<ConsumoEnergia> lecturas =
                consumoRepository
                        .findByDispositivoIoTIdOrderByFechaHoraAsc(
                                dispositivoIoTId
                        );

        if (lecturas.isEmpty()) {
            return 0.0;
        }

        ConsumoEnergia ultima = lecturas.get(lecturas.size() - 1);

        if (ultima.getEnergiaWh() == null) {
            return 0.0;
        }

        double energiaInicial =
                dispositivo.getEnergiaInicialWh() != null
                        ? dispositivo.getEnergiaInicialWh()
                        : ultima.getEnergiaWh();

        double consumoWh = ultima.getEnergiaWh() - energiaInicial;

        if (consumoWh < 0) {
            return 0.0;
        }

        return consumoWh / 1000.0;
    }

    // ============================================================
    // DETECTAR SHELLY POR IP LOCAL
    // ============================================================

    @SuppressWarnings("unchecked")
    public Map<String, Object> detectarShelly(String ip) {

        String url = "http://" + ip.trim()
                + "/rpc/Shelly.GetDeviceInfo";

        Map<String, Object> info =
                restTemplate.getForObject(url, Map.class);

        if (info == null) {
            throw new RuntimeException(
                    "No se pudo obtener información del dispositivo."
            );
        }

        Map<String, Object> resultado = new java.util.HashMap<>();

        resultado.put("detectado", true);
        resultado.put("ip", ip);
        resultado.put("id", info.get("id"));
        resultado.put("mac", info.get("mac"));
        resultado.put("modelo", info.get("model"));
        resultado.put("generacion", info.get("gen"));
        resultado.put("tipo", info.get("app"));
        resultado.put("matter", info.get("matter"));

        return resultado;
    }

    // ============================================================
    // REGISTRAR SHELLY POR IP
    // ============================================================

    public DispositivoIoT registrarShelly(
            String ip,
            Long usuarioId) {

        Map<String, Object> info = detectarShelly(ip);

        String shellyId = (String) info.get("id");

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado.")
                );

        List<DispositivoIoT> existentes =
                dispositivoRepository.findAllByShellyIdAndUsuarioId(
                        shellyId, usuarioId
                );

        if (!existentes.isEmpty()) {
            return existentes.get(existentes.size() - 1);
        }

        DispositivoIoT dispositivo = new DispositivoIoT();

        dispositivo.setUsuario(usuario);
        dispositivo.setIpShelly((String) info.get("ip"));
        dispositivo.setShellyId(shellyId);
        dispositivo.setMac((String) info.get("mac"));
        dispositivo.setTipo((String) info.get("tipo"));
        dispositivo.setNombre(generarNombreShelly());
        dispositivo.setCategoria("Sin categoría");
        dispositivo.setEncendido(false);

        // La configuración inicial sigue utilizando la IP local.
        dispositivo.setEnergiaInicialWh(
                obtenerEnergiaActualWhLocal(dispositivo)
        );

        return dispositivoRepository.save(dispositivo);
    }

    // ============================================================
    // REGISTRAR SHELLY CON DATOS DE LA APLICACIÓN
    // ============================================================

        public DispositivoIoT registrarShellyPorId(
                String shellyId,
                String ip,
                String mac,
                String tipo,
                Double energiaInicialWh,
                String aparatoConectado,
                String categoria,
                Long usuarioId) {

        if (shellyId == null || shellyId.isBlank()) {
                throw new IllegalArgumentException(
                        "El ID del Shelly está vacío."
                );
        }

        if (ip == null || ip.isBlank()) {
                throw new IllegalArgumentException(
                        "La IP del Shelly está vacía."
                );
        }

        String id = shellyId.trim();
        String ipLocal = ip.trim();

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado.")
                );

        List<DispositivoIoT> existentes =
                dispositivoRepository.findAllByShellyIdAndUsuarioId(
                        id, usuarioId
                );

        if (!existentes.isEmpty()) {
                DispositivoIoT existente = existentes.get(0);

                existente.setIpShelly(ipLocal);
                existente.setMac(mac);
                existente.setTipo(tipo);
                existente.setAparatoConectado(aparatoConectado);
                existente.setCategoria(categoria);
                existente.setActivo(true);

                return dispositivoRepository.save(existente);
        }

        DispositivoIoT dispositivo = new DispositivoIoT();

        dispositivo.setUsuario(usuario);
        dispositivo.setShellyId(id);
        dispositivo.setIpShelly(ipLocal);
        dispositivo.setMac(mac);
        dispositivo.setTipo(tipo);
        dispositivo.setNombre(generarNombreShelly());
        dispositivo.setAparatoConectado(aparatoConectado);
        dispositivo.setCategoria(categoria);
        dispositivo.setEncendido(false);
        dispositivo.setActivo(true);

        dispositivo.setEnergiaInicialWh(
                energiaInicialWh != null && energiaInicialWh >= 0
                        ? energiaInicialWh
                        : 0.0
        );

        return dispositivoRepository.save(dispositivo);
        }


    // ============================================================
    // OBTENER ENERGÍA INICIAL POR IP LOCAL
    // ============================================================

    @SuppressWarnings("unchecked")
    private Double obtenerEnergiaActualWhLocal(
            DispositivoIoT dispositivo) {

        Map<String, Object> estado = obtenerEstadoLocal(dispositivo);

        Map<String, Object> aenergy =
                estado.get("aenergy") instanceof Map<?, ?> mapa
                        ? (Map<String, Object>) mapa
                        : null;

        if (aenergy == null) {
            return 0.0;
        }

        Object totalObj = aenergy.get("total");

        if (!(totalObj instanceof Number total)) {
            return 0.0;
        }

        return total.doubleValue();
    }

    // ============================================================
    // GENERAR NOMBRE GLOBAL DEL SHELLY
    // ============================================================

    private String generarNombreShelly() {

        List<DispositivoIoT> dispositivos =
                dispositivoRepository.findAll();

        int siguienteNumero = 1;

        for (DispositivoIoT dispositivo : dispositivos) {

            String nombre = dispositivo.getNombre();

            if (nombre == null) {
                continue;
            }

            if (nombre.matches("Shelly \\d+")) {

                String numeroTexto =
                        nombre.substring("Shelly ".length());

                int numero = Integer.parseInt(numeroTexto);

                if (numero >= siguienteNumero) {
                    siguienteNumero = numero + 1;
                }
            }
        }

        return "Shelly " + siguienteNumero;
    }
}
