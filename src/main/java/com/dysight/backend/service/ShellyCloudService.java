
package com.dysight.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Service
public class ShellyCloudService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${shelly.cloud.server}")
    private String server;

    @Value("${shelly.cloud.auth-key:}")
    private String authKey;

    public List<Map<String, Object>> obtenerEstado(String deviceId) {

    if (authKey == null || authKey.isBlank()) {
        throw new IllegalStateException(
                "No está configurada la clave de Shelly Cloud."
        );
    }

    if (deviceId == null || deviceId.isBlank()) {
        throw new IllegalArgumentException(
                "El identificador del Shelly está vacío."
        );
    }

    String url = server.replaceAll("/+$", "")
            + "/v2/devices/api/get?auth_key="
            + UriUtils.encodeQueryParam(
                    authKey,
                    StandardCharsets.UTF_8
            );

    Map<String, Object> body = Map.of(
            "ids", List.of(deviceId),
            "select", List.of("status")
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request =
            new HttpEntity<>(body, headers);

    ResponseEntity<List<Map<String, Object>>> response =
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<
                            List<Map<String, Object>>>() {}
            );

    List<Map<String, Object>> resultado = response.getBody();

if (resultado == null || resultado.isEmpty()) {
    throw new IllegalStateException(
            "Shelly Cloud no devolvió información del dispositivo."
    );
}

return resultado;
}

public boolean estaConfigurado() {
    return authKey != null && !authKey.isBlank();
}

public Map<String, Object> cambiarEstado(
        String deviceId,
        boolean encender) {

    if (!estaConfigurado()) {
        throw new IllegalStateException(
                "Shelly Cloud no está configurado."
        );
    }

    if (deviceId == null || deviceId.isBlank()) {
        throw new IllegalArgumentException(
                "El identificador del Shelly está vacío."
        );
    }

    String url = server.replaceAll("/+$", "")
            + "/v2/devices/api/set/switch?auth_key="
            + UriUtils.encodeQueryParam(
                    authKey,
                    StandardCharsets.UTF_8
            );

    Map<String, Object> body = Map.of(
            "id", deviceId,
            "channel", 0,
            "on", encender
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request =
            new HttpEntity<>(body, headers);

    ResponseEntity<Object> response =
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Object.class
            );

    Object respuesta = response.getBody();

    if (respuesta == null) {
        throw new IllegalStateException(
                "Shelly Cloud devolvió una respuesta vacía."
        );
    }

    Map<String, Object> resultado =
            new java.util.LinkedHashMap<>();

    resultado.put("output", encender);
    resultado.put("respuestaCloud", respuesta);

    return resultado;
}



}
