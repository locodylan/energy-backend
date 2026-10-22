package com.dysight.backend.controller;

import com.dysight.backend.service.RegistroService;
import com.dysight.backend.service.VerificacionCorreoService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/registro")
public class RegistroController {

    private final RegistroService registroService;
    private final VerificacionCorreoService verificacionService;

    public RegistroController(
            RegistroService registroService,
            VerificacionCorreoService verificacionService
    ) {
        this.registroService = registroService;
        this.verificacionService = verificacionService;
    }

    @PostMapping("/iniciar")
    public Map<String, Object> iniciarRegistro(
            @RequestBody Map<String, String> datos
    ) {

        Map<String, Object> respuesta = new HashMap<>();

        String nombre = datos.get("nombre");
        String username = datos.get("username");
        String email = datos.get("email");
        String password = datos.get("password");

        if (nombre == null || username == null
                || email == null || password == null
                || nombre.isBlank()
                || username.isBlank()
                || email.isBlank()
                || password.isBlank()) {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "Todos los campos son obligatorios"
            );

            return respuesta;
        }

        boolean guardado =
                registroService.guardarRegistroPendiente(
                        nombre,
                        username,
                        email,
                        password
                );

        if (!guardado) {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "El usuario o correo ya está registrado"
            );

            return respuesta;
        }

        try {

            verificacionService.enviarCodigo(email);

            respuesta.put("success", true);
            respuesta.put(
                    "mensaje",
                    "Código enviado al correo"
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
    public Map<String, Object> verificarRegistro(
            @RequestParam String email,
            @RequestParam String codigo
    ) {

        Map<String, Object> respuesta = new HashMap<>();

        boolean codigoCorrecto =
                verificacionService.verificarCodigo(
                        email,
                        codigo
                );

        if (!codigoCorrecto) {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "Código incorrecto o expirado"
            );

            return respuesta;
        }

        verificacionService.desactivarCodigo(email);

        boolean registroCreado =
                registroService.confirmarRegistro(email);

        if (!registroCreado) {

            respuesta.put("success", false);
            respuesta.put(
                    "mensaje",
                    "No se pudo completar el registro"
            );

            return respuesta;
        }

        respuesta.put("success", true);
        respuesta.put(
                "mensaje",
                "Cuenta creada correctamente"
        );

        return respuesta;
    }
}