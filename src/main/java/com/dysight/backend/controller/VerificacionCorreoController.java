package com.dysight.backend.controller;

import com.dysight.backend.service.VerificacionCorreoService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/verificacion")
public class VerificacionCorreoController {

    private final VerificacionCorreoService verificacionService;

    public VerificacionCorreoController(
            VerificacionCorreoService verificacionService
    ) {
        this.verificacionService = verificacionService;
    }

    @PostMapping("/enviar")
    public Map<String, Object> enviarCodigo(
            @RequestParam String email
    ) {

        Map<String, Object> respuesta =
                new HashMap<>();

        try {

            verificacionService.enviarCodigo(email);

            respuesta.put("success", true);
            respuesta.put(
                    "mensaje",
                    "Código enviado correctamente"
            );

        } catch (Exception e) {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "No se pudo enviar el código"
            );
        }

        return respuesta;
    }

    @PostMapping("/verificar")
    public Map<String, Object> verificarCodigo(
            @RequestParam String email,
            @RequestParam String codigo
    ) {

        Map<String, Object> respuesta =
                new HashMap<>();

        boolean correcto =
                verificacionService.verificarCodigo(
                        email,
                        codigo
                );

        if (correcto) {

            respuesta.put("success", true);
            respuesta.put(
                    "mensaje",
                    "Código verificado correctamente"
            );

        } else {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "Código incorrecto o expirado"
            );
        }

        return respuesta;
    }
}