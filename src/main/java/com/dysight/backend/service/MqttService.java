
package com.dysight.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MqttService {

    private static final Logger log =
            LoggerFactory.getLogger(MqttService.class);

    private static final int RPC_TIMEOUT_SECONDS = 10;

    private final String brokerUrl;
    private final String username;
    private final String password;
    private final String clientId;
    private final ObjectMapper objectMapper;

    private final AtomicInteger requestCounter =
            new AtomicInteger(0);

    private MqttClient client;

    public MqttService(
            @Value("${mqtt.broker-url}") String brokerUrl,
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password,
            @Value("${mqtt.client-id:dysight-backend}") String clientId,
            ObjectMapper objectMapper) {

        this.brokerUrl = brokerUrl;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.objectMapper = objectMapper;
    }

    // ============================================================
    // CONEXIÓN CON EMQX
    // ============================================================

    public synchronized boolean conectar() {
        try {
            if (client != null && client.isConnected()) {
                return true;
            }

            if (username.isBlank() || password.isBlank()) {
                log.warn("MQTT no está configurado: faltan credenciales.");
                return false;
            }

            if (client != null) {
                try {
                    client.close();
                } catch (MqttException ignored) {
                    // Se intentará crear una conexión nueva.
                }
            }

            // El ID se mantiene estable para que las reglas de
            // autorización de EMQX puedan identificar al backend.
            client = new MqttClient(
                    brokerUrl,
                    clientId,
                    new MemoryPersistence()
            );

            MqttConnectOptions options =
                    new MqttConnectOptions();

            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            options.setAutomaticReconnect(true);

            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.connect(options);

            log.info("Conexión MQTT establecida con EMQX.");
            return true;

        } catch (MqttException e) {
            log.error(
                    "No se pudo conectar con EMQX mediante MQTT: {}",
                    e.getMessage()
            );
            return false;
        }
    }

    public synchronized boolean estaConectado() {
        return client != null && client.isConnected();
    }

    // ============================================================
    // EJECUTAR RPC REMOTO CONTRA UN SHELLY
    // ============================================================

    public synchronized Map<String, Object> ejecutarRpcShelly(
            String shellyId,
            String metodo,
            Map<String, Object> parametros) {

        if (shellyId == null || shellyId.isBlank()) {
            throw new IllegalArgumentException(
                    "El Shelly ID no está configurado."
            );
        }

        if (metodo == null || metodo.isBlank()) {
            throw new IllegalArgumentException(
                    "El método RPC está vacío."
            );
        }

        if (!conectar()) {
            throw new IllegalStateException(
                    "No hay conexión disponible con el broker MQTT."
            );
        }

        String topicDispositivo =
                shellyId.trim() + "/rpc";

        // El Shelly enviará la respuesta a este topic.
        String topicRespuesta =
                client.getClientId() + "/rpc";

        int requestId = requestCounter.incrementAndGet();

        Map<String, Object> solicitud =
                new LinkedHashMap<>();

        solicitud.put("id", requestId);
        solicitud.put("src", client.getClientId());
        solicitud.put("method", metodo);
        solicitud.put(
                "params",
                parametros != null ? parametros : Map.of()
        );

        final String payload;

        try {
            payload = objectMapper.writeValueAsString(solicitud);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo construir la solicitud RPC.",
                    e
            );
        }

        CountDownLatch respuestaRecibida =
                new CountDownLatch(1);

        AtomicReference<JsonNode> respuesta =
                new AtomicReference<>();

        AtomicReference<Exception> errorRecepcion =
                new AtomicReference<>();

        try {
            // Primero se escucha la respuesta y luego se envía
            // la solicitud para no perder una respuesta rápida.
            client.subscribe(topicRespuesta, 0, (topic, mensaje) -> {
                try {
                    JsonNode json = objectMapper.readTree(
                            mensaje.getPayload()
                    );

                    JsonNode idRespuesta = json.get("id");

                    if (idRespuesta != null
                            && idRespuesta.asInt(-1) == requestId) {

                        respuesta.set(json);
                        respuestaRecibida.countDown();
                    }

                } catch (Exception e) {
                    errorRecepcion.set(e);
                    respuestaRecibida.countDown();
                }
            });

            MqttMessage mensaje = new MqttMessage(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            mensaje.setQos(0);

            client.publish(topicDispositivo, mensaje);

            boolean recibida = respuestaRecibida.await(
                    RPC_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            if (!recibida) {
                throw new IllegalStateException(
                        "El Shelly " + shellyId
                                + " no respondió por MQTT en "
                                + RPC_TIMEOUT_SECONDS + " segundos."
                );
            }

            if (errorRecepcion.get() != null) {
                throw new IllegalStateException(
                        "No se pudo interpretar la respuesta MQTT.",
                        errorRecepcion.get()
                );
            }

            JsonNode jsonRespuesta = respuesta.get();

            if (jsonRespuesta == null) {
                throw new IllegalStateException(
                        "Se recibió una respuesta MQTT vacía."
                );
            }

            if (jsonRespuesta.has("error")) {
                throw new IllegalStateException(
                        "El Shelly devolvió un error RPC: "
                                + jsonRespuesta.get("error")
                );
            }

            JsonNode resultado = jsonRespuesta.get("result");

            if (resultado == null || !resultado.isObject()) {
                throw new IllegalStateException(
                        "La respuesta RPC no contiene un resultado válido."
                );
            }

            @SuppressWarnings("unchecked")
Map<String, Object> resultadoMap = objectMapper.convertValue(
        resultado,
        Map.class
);

return resultadoMap;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "La espera de la respuesta MQTT fue interrumpida.",
                    e
            );

        } catch (MqttException e) {
            throw new IllegalStateException(
                    "Error al comunicarse con el Shelly mediante MQTT: "
                            + e.getMessage(),
                    e
            );

        } finally {
            try {
                if (client != null && client.isConnected()) {
                    client.unsubscribe(topicRespuesta);
                }
            } catch (MqttException e) {
                log.warn(
                        "No se pudo cancelar la suscripción temporal de RPC."
                );
            }
        }
    }

    // ============================================================
    // COMPROBAR CONEXIÓN AL INICIAR SPRING BOOT
    // ============================================================

    @EventListener(ApplicationReadyEvent.class)
    public void comprobarConexionAlIniciar() {
        if (!username.isBlank() && !password.isBlank()) {
            conectar();
        } else {
            log.warn(
                    "MQTT no se conecta al iniciar: faltan las credenciales."
            );
        }
    }

    // ============================================================
    // CERRAR CONEXIÓN
    // ============================================================

    @PreDestroy
    public synchronized void desconectar() {
        if (client == null) {
            return;
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }

            client.close();
            log.info("Conexión MQTT cerrada.");

        } catch (MqttException e) {
            log.warn("No se pudo cerrar MQTT limpiamente.");
        }
    }
}
